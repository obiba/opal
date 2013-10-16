package org.obiba.opal.core.runtime.database;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import javax.validation.ConstraintViolationException;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.datasource.hibernate.support.HibernateDatasourceFactory;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbDatabase;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.runtime.jdbc.DatabaseSessionFactoryProvider;
import org.obiba.opal.core.runtime.jdbc.SessionFactoryFactory;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.OrientDbTransactionCallbackWithoutResult;
import org.obiba.opal.core.service.impl.DefaultBeanValidator;
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
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

@SuppressWarnings("OverlyCoupledClass")
@Component
public class DefaultDatabaseRegistry implements DatabaseRegistry {

  private static final Logger log = LoggerFactory.getLogger(DefaultDatabaseRegistry.class);

  @Autowired
  private SessionFactoryFactory sessionFactoryFactory;

  @Autowired
  private DataSourceFactory dataSourceFactory;

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private DefaultBeanValidator defaultBeanValidator;

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
          SqlDatabase database = (SqlDatabase) getDatabase(databaseName);
          return dataSourceFactory.createDataSource(database);
        }
      });

  private final LoadingCache<String, SessionFactory> sessionFactoryCache = CacheBuilder.newBuilder()
      .removalListener(new RemovalListener<String, SessionFactory>() {
        @Override
        public void onRemoval(RemovalNotification<String, SessionFactory> notification) {
          try {
            log.info("Destroying SessionFactory {}", notification.getKey());
            SessionFactory sf = notification.getValue();
            if(sf != null) sf.close();
          } catch(HibernateException e) {
            log.warn("Ignoring exception during SessionFactory shutdown: ", e);
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
    orientDbService
        .registerEntityClass(Database.class, SqlDatabase.class, SqlDatabase.LimesurveyDatasourceSettings.class,
            SqlDatabase.JdbcDatasourceSettings.class, SqlDatabase.JdbcDatasourceSettings.JdbcValueTableSettings.class,
            MongoDbDatabase.class);
    // don't create index on abstract base class or it will fail
    orientDbService.createUniqueStringIndex(SqlDatabase.class, "name");
    orientDbService.createUniqueStringIndex(SqlDatabase.class, "url");
    orientDbService.createUniqueStringIndex(MongoDbDatabase.class, "name");
    orientDbService.createUniqueStringIndex(MongoDbDatabase.class, "url");
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
  public Iterable<Database> list(@Nullable Database.Usage usage) {
    if(usage == null) {
      return list();
    }
    return orientDbService.list("select from Database where usedForIdentifiers = ? and usage = ?", false, usage);
  }

  @Nonnull
  @Override
  public Database getDatabase(@Nonnull String name) throws NoSuchDatabaseException {
    Database database = orientDbService.uniqueResult("select from Database where name = ?", name);
    if(database == null) throw new NoSuchDatabaseException(name);
    return database;
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
  public void addOrReplaceDatabase(@Nonnull final Database database)
      throws ConstraintViolationException, MultipleIdentifiersDatabaseException {

    validUniqueIdentifiersDatabase(database);

    if(database.isDefaultStorage()) {
      final Database previousDefaultStorageDatabase = getDefaultStorageDatabase();
      if(previousDefaultStorageDatabase == null) {
        orientDbService.save(database);
      } else {
        defaultBeanValidator.validate(database);
        previousDefaultStorageDatabase.setDefaultStorage(false);
        orientDbService.execute(new OrientDbTransactionCallbackWithoutResult() {
          @Override
          protected void doInTransactionWithoutResult(OObjectDatabaseTx db) {
            db.save(database);
            db.save(previousDefaultStorageDatabase);
          }
        });
      }
    } else {
      orientDbService.save(database);
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

  @Nullable
  @Override
  public Database getDefaultStorageDatabase() {
    return orientDbService
        .uniqueResult("select from Database where usedForIdentifiers = ? and defaultStorage = ?", false, true);
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
    if(database.isEditable()) {
      database.setEditable(false);
      orientDbService.save(database);
    }
    registrations.put(databaseName, usedByDatasource);
  }

  @Override
  public void unregister(@Nonnull String databaseName, String usedByDatasource) {
    Database database = getDatabase(databaseName);
    database.setEditable(true);
    orientDbService.save(database);
    registrations.remove(databaseName, usedByDatasource);
  }

  @Nonnull
  @Override
  public Database getIdentifiersDatabase() throws IdentifiersDatabaseNotFoundException {
    Database database = orientDbService.uniqueResult("select from Database where usedForIdentifiers = ?", true);
    if(database == null) throw new IdentifiersDatabaseNotFoundException();
    return database;
  }

  @Nonnull
  @Override
  public DatasourceFactory createDataSourceFactory(@Nonnull String datasourceName, @Nonnull Database database) {
    String databaseName = database.getName();
    Preconditions.checkArgument(database.getUsage() == Database.Usage.STORAGE,
        "Cannot create DatasourceFactory for non storage database " + databaseName + ": " + database.getUsage());

    if(database instanceof SqlDatabase) {
      SqlDatabase sqlDatabase = (SqlDatabase) database;
      if(sqlDatabase.getSqlSchema() != SqlDatabase.SqlSchema.HIBERNATE) {
        throw new IllegalArgumentException(
            "Cannot create datasource for non Hibernate storage database " + databaseName + ": " +
                sqlDatabase.getSqlSchema());
      }
      return new HibernateDatasourceFactory(datasourceName,
          new DatabaseSessionFactoryProvider(datasourceName, this, database.getName()));
    }
    if(database instanceof MongoDbDatabase) {
      return ((MongoDbDatabase) database).createMongoDBDatasourceFactory();
    }
    throw new IllegalArgumentException("Unknown datasource config for database " + database.getClass());
  }

}
