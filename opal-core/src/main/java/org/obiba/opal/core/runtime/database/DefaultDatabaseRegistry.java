package org.obiba.opal.core.runtime.database;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import javax.validation.ConstraintViolationException;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.datasource.hibernate.HibernateDatasource;
import org.obiba.magma.datasource.mongodb.MongoDBDatasource;
import org.obiba.opal.core.cfg.OrientDbService;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbDatabase;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.runtime.jdbc.SessionFactoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atomikos.jdbc.AbstractDataSourceBean;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.orientechnologies.orient.core.index.OIndexException;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;

@Component
public class DefaultDatabaseRegistry implements DatabaseRegistry {

  private static final Logger log = LoggerFactory.getLogger(DefaultDatabaseRegistry.class);

  @Autowired
  private SessionFactoryFactory sessionFactoryFactory;

  @Autowired
  private DataSourceFactory dataSourceFactory;

  @Autowired
  private OrientDbService orientDbService;

  private final LoadingCache<String, DataSource> dataSourceCache = CacheBuilder.newBuilder()
      .removalListener(new RemovalListener<String, DataSource>() {

        @Override
        public void onRemoval(RemovalNotification<String, DataSource> notification) {
          log.info("Destroying DataSource {}", notification.getKey());
          DataSource dataSource = notification.getValue();
          if(dataSource != null) ((AbstractDataSourceBean) dataSource).close();
        }
      }) //
      .build(new CacheLoader<String, DataSource>() {

        @Override
        public DataSource load(String databaseName) throws Exception {
          log.info("Building DataSource {}", databaseName);
          return dataSourceFactory.createDataSource((SqlDatabase) getDatabase(databaseName));
        }
      });

  private final LoadingCache<String, SessionFactory> sessionFactoryCache = CacheBuilder.newBuilder()
      .removalListener(new RemovalListener<String, SessionFactory>() {
        @Override
        public void onRemoval(RemovalNotification<String, SessionFactory> notification) {
          try {
            log.info("Destroying session factory {}", notification.getKey());
            SessionFactory sf = notification.getValue();
            if(sf != null) sf.close();
          } catch(HibernateException e) {
            log.warn("Ignoring exception during shutdown: ", e);
          }
        }
      }) //
      .build(new CacheLoader<String, SessionFactory>() {

        @Override
        public SessionFactory load(String databaseName) throws Exception {
          log.info("Building SessionFactory {}", databaseName);
          return sessionFactoryFactory.getSessionFactory(getDataSource(databaseName, null));
        }
      });

  private final SetMultimap<String, String> registrations = Multimaps
      .synchronizedSetMultimap(HashMultimap.<String, String>create());

  @Override
  @PostConstruct
  public void start() {
    orientDbService.registerEntityClass(Database.class, SqlDatabase.class, MongoDbDatabase.class);
    // don't create index on abstract base class or it will fail
    orientDbService.createUniqueStringIndex(SqlDatabase.class, "name");
    orientDbService.createUniqueStringIndex(MongoDbDatabase.class, "name");
  }

  @Override
  @PreDestroy
  public void stop() {
    sessionFactoryCache.invalidateAll();
    dataSourceCache.invalidateAll();
  }

  @Override
  public Iterable<Database> list() {
    return list(Database.class);
  }

  @Override
  public <T extends Database> Iterable<T> list(@Nonnull Class<T> databaseClass) {
    return orientDbService
        .list("select from " + databaseClass.getSimpleName() + " where usedForIdentifiers = ?", false);
  }

  @Override
  public Iterable<Database> list(@Nullable String type) {
    if(Strings.isNullOrEmpty(type)) {
      return list();
    }
    return orientDbService.list("select from Database where usedForIdentifiers = ? and type = ?", false, type);
  }

  @Nullable
  @Override
  public Database getDatabase(@Nonnull String name) {
    return orientDbService.uniqueResult("select from Database where name = ?", name);
  }

  @Override
  public DataSource getDataSource(@Nonnull String name, @Nullable String usedByDatasource) {
    register(name, usedByDatasource);
    return dataSourceCache.getUnchecked(name);
  }

  @Override
  public SessionFactory getSessionFactory(@Nonnull String name, @Nullable String usedByDatasource) {
    register(name, usedByDatasource);
    return sessionFactoryCache.getUnchecked(name);
  }

  @Override
  public void addOrReplaceDatabase(@Nonnull Database database)
      throws ConstraintViolationException, MultipleIdentifiersDatabaseException, DatabaseAlreadyExistsException {

    validUniqueIdentifiersDatabase(database);
    try {
      orientDbService.save(database);
    } catch(OIndexException e) {
      throw new DatabaseAlreadyExistsException(database.getName());
    } catch(ORecordDuplicatedException e) {
      throw new DatabaseAlreadyExistsException(database.getName());
    }
  }

  private void validUniqueIdentifiersDatabase(Database database) throws MultipleIdentifiersDatabaseException {
    if(database.isUsedForIdentifiers()) {
      Database identifiersDatabase = null;
      try {
        identifiersDatabase = getIdentifiersDatabase();
        if(!Objects.equal(identifiersDatabase.getName(), database.getName())) {
          throw new MultipleIdentifiersDatabaseException(identifiersDatabase.getName(), database.getName());
        }
      } catch(IdentifiersDatabaseNotFoundException ignored) {
      }
    }
  }

  @Override
  public void deleteDatabase(@Nonnull Database database) throws CannotDeleteDatabaseWithDataException {
    //TODO check if this database has data
    orientDbService.delete(database);
    destroyDataSource(database.getName());
  }

  private void destroyDataSource(String name) {
    sessionFactoryCache.invalidate(name);
    dataSourceCache.invalidate(name);
  }

  private void register(String databaseName, @Nullable String usedByDatasource) {
    if(Strings.isNullOrEmpty(usedByDatasource)) return;
    Database database = getDatabase(databaseName);
    if(database == null) {
      throw new IllegalArgumentException("Cannot find database " + databaseName);
    }
    if(database.isEditable()) {
      database.setEditable(false);
      addOrReplaceDatabase(database);
    }
    registrations.put(databaseName, usedByDatasource);
  }

  @Override
  public void unregister(@Nonnull String databaseName, String usedByDatasource) {
    Database database = getDatabase(databaseName);
    if(database == null) {
      throw new IllegalArgumentException("Cannot find database " + databaseName);
    }
    database.setEditable(true);
    addOrReplaceDatabase(database);
    registrations.remove(databaseName, usedByDatasource);
  }

  @Nonnull
  @Override
  public Database getIdentifiersDatabase() throws IdentifiersDatabaseNotFoundException {
    Database database = orientDbService.uniqueResult("select from Database where usedForIdentifiers = ?", true);
    if(database == null) throw new IdentifiersDatabaseNotFoundException();
    return database;
  }

  @Override
  public Datasource createStorageMagmaDatasource(String datasourceName, Database database) {
    Preconditions.checkArgument(database.getUsage() == Database.Usage.STORAGE,
        "Cannot create datasource for non storage database " + database.getName() + " (" + database.getUsage() + ")");

    if(database instanceof SqlDatabase) {
      SqlDatabase sqlDatabase = (SqlDatabase) database;
      if(sqlDatabase.isHibernateDatasourceType()) {
        return new HibernateDatasource(datasourceName, getSessionFactory(database.getName(), datasourceName));
      }
    }
    if(database instanceof MongoDbDatabase) {
      return new MongoDBDatasource(datasourceName, ((MongoDbDatabase) database).createMongoDBFactory());
    }
    throw new IllegalArgumentException("Unknown datasource config for database " + database);
  }

}
