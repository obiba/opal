package org.obiba.opal.core.runtime.database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.datasource.hibernate.HibernateDatasource;
import org.obiba.magma.datasource.mongodb.MongoDBDatasource;
import org.obiba.opal.core.cfg.OrientDbService;
import org.obiba.opal.core.cfg.OrientDbTransactionCallback;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbDatabase;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.runtime.jdbc.SessionFactoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

@Component
public class DefaultDatabaseRegistry implements DatabaseRegistry {

  private static final Logger log = LoggerFactory.getLogger(DefaultDatabaseRegistry.class);

  @Autowired
  private SessionFactoryFactory sessionFactoryFactory;

  @Autowired
  private DataSourceFactory dataSourceFactory;

  @Autowired
  private OrientDbService orientDbService;

  private final LoadingCache<String, BasicDataSource> dataSourceCache = CacheBuilder.newBuilder()
      .removalListener(new RemovalListener<String, BasicDataSource>() {

        @Override
        public void onRemoval(RemovalNotification<String, BasicDataSource> notification) {
          try {
            log.info("Destroying DataSource {}", notification.getKey());
            BasicDataSource dataSource = notification.getValue();
            if(dataSource != null) dataSource.close();
          } catch(SQLException e) {
            log.warn("Ignoring exception during shutdown: ", e);
          }
        }
      }) //
      .build(new CacheLoader<String, BasicDataSource>() {

        @Override
        public BasicDataSource load(String databaseName) throws Exception {
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
  public Iterable<Database> list() {
    return list(Database.class);
  }

  @Override
  public <T extends Database> Iterable<T> list(@Nonnull Class<T> databaseClass) {
    return orientDbService
        .list("select from " + databaseClass.getSimpleName() + " where usedForIdentifiers = :usedForIdentifiers",
            "usedForIdentifiers", false);
  }

  @Override
  public Iterable<Database> list(@Nullable String type) {
    if(Strings.isNullOrEmpty(type)) {
      return list();
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("usedForIdentifiers", false);
    params.put("type", type);
    return orientDbService.list("select from " + Database.class.getSimpleName() +
        " where usedForIdentifiers = :usedForIdentifiers and type = :type", params);
  }

  @Override
  public Database getDatabase(@Nonnull String name) {
    return orientDbService
        .uniqueResult("select from " + Database.class.getSimpleName() + " where name = :name", "name", name);
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
  public void addOrReplaceDatabase(@Nonnull final Database database) throws MultipleIdentifiersDatabaseException {
    validUniqueIdentifiersDatabase(database);
    orientDbService.execute(new OrientDbTransactionCallback<Object>() {
      @Override
      public Object doInTransaction(OObjectDatabaseTx db) {
        return db.save(database);
      }
    });
  }

  private void validUniqueIdentifiersDatabase(Database database) throws MultipleIdentifiersDatabaseException {
    if(database.isUsedForIdentifiers()) {
      Database identifiersDatabase = getIdentifiersDatabase();
      if(identifiersDatabase != null && !Objects.equal(identifiersDatabase.getName(), database.getName())) {
        throw new MultipleIdentifiersDatabaseException(identifiersDatabase, database);
      }
    }
  }

  @Override
  public void deleteDatabase(@Nonnull final Database database) {
    //TODO check if this database has data 
    orientDbService.execute(new OrientDbTransactionCallback<Object>() {
      @Override
      public Object doInTransaction(OObjectDatabaseTx db) {
        return db.delete(database);
      }
    });
    destroyDataSource(database.getName());
  }

  private void destroyDataSource(String name) {
    sessionFactoryCache.invalidate(name);
    dataSourceCache.invalidate(name);
  }

  private void register(String databaseName, String usedByDatasource) {
    if(Strings.isNullOrEmpty(usedByDatasource)) return;
    Database database = getDatabase(databaseName);
    database.setEditable(false);
    addOrReplaceDatabase(database);
    registrations.put(databaseName, usedByDatasource);
  }

  @Override
  public void unregister(@Nonnull String databaseName, String usedByDatasource) {
    Database database = getDatabase(databaseName);
    database.setEditable(true);
    addOrReplaceDatabase(database);
    registrations.remove(databaseName, usedByDatasource);
  }

  @Nullable
  @Override
  public Database getIdentifiersDatabase() {
    return orientDbService.uniqueResult(
        "select from " + Database.class.getSimpleName() + " where usedForIdentifiers = :usedForIdentifiers",
        "usedForIdentifiers", true);
  }

  @Override
  public Datasource createStorageMagmaDatasource(String datasourceName, Database database) {
    Preconditions.checkArgument(database.getType() == Database.Type.STORAGE,
        "Cannot create datasource for non storage database " + database.getName());

    if(database instanceof SqlDatabase) {
      SqlDatabase sqlDatabase = (SqlDatabase) database;
      if(sqlDatabase.isHibernateDatasourceType()) {
        return new HibernateDatasource(datasourceName, getSessionFactory(database.getName(), datasourceName));
      }
    }
    if(database instanceof MongoDbDatabase) {
      return new MongoDBDatasource(datasourceName, ((MongoDbDatabase) database).createMongoClientURI());
    }
    throw new IllegalArgumentException("Unknown datasource config for database " + database);
  }

  @PostConstruct
  public void start() {
    orientDbService.registerEntityClass(Database.class, SqlDatabase.class, MongoDbDatabase.class);
  }

  @PreDestroy
  public void stop() {
    sessionFactoryCache.invalidateAll();
    dataSourceCache.invalidateAll();
  }

}
