package org.obiba.opal.core.runtime.database;

import java.util.ArrayList;
import java.util.Collection;

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
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.runtime.jdbc.DatabaseSessionFactoryProvider;
import org.obiba.opal.core.runtime.jdbc.SessionFactoryFactory;
import org.obiba.opal.core.service.impl.DefaultBeanValidator;
import org.obiba.opal.core.service.impl.OrientDbDocumentService;
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
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.tx.OTransaction;

@SuppressWarnings("OverlyCoupledClass")
@Component
public class DefaultDatabaseRegistry implements DatabaseRegistry {

  private static final Logger log = LoggerFactory.getLogger(DefaultDatabaseRegistry.class);

  private static final String UNIQUE_INDEX = "name";

  @Autowired
  private SessionFactoryFactory sessionFactoryFactory;

  @Autowired
  private DataSourceFactory dataSourceFactory;

  @Autowired
  private OrientDbDocumentService orientDbDocumentService;

  @Autowired
  private DefaultBeanValidator defaultBeanValidator;

  private final LoadingCache<String, DataSource> dataSourceCache = CacheBuilder.newBuilder()
      .removalListener(new DataSourceRemovalListener()) //
      .build(new DataSourceCacheLoader());

  private final LoadingCache<String, SessionFactory> sessionFactoryCache = CacheBuilder.newBuilder()
      .removalListener(new SessionFactoryRemovalListener()) //
      .build(new SessionFactoryCacheLoader());

  private final SetMultimap<String, String> registrations = Multimaps
      .synchronizedSetMultimap(HashMultimap.<String, String>create());

  @Override
  @PostConstruct
  public void start() {
    orientDbDocumentService.createUniqueStringIndex(Database.class, UNIQUE_INDEX);
  }

  @Override
  @PreDestroy
  public void stop() {
    sessionFactoryCache.invalidateAll();
    dataSourceCache.invalidateAll();
  }

  @Override
  public Iterable<Database> list() {
    return orientDbDocumentService
        .list(Database.class, "select from " + Database.class.getSimpleName() + " where usedForIdentifiers = ?", false);
  }

  @Override
  public Iterable<Database> list(@Nullable Database.Usage usage) {
    if(usage == null) {
      return list();
    }
    return orientDbDocumentService.list(Database.class,
        "select from " + Database.class.getSimpleName() + " where usedForIdentifiers = ? and usage = ?", false, usage);
  }

  @Override
  public Iterable<Database> listSqlDatabases() {
    return orientDbDocumentService.list(Database.class,
        "select from " + Database.class.getSimpleName() + " where usedForIdentifiers = ? and sqlSettings is not null",
        false);
  }

  @Override
  public Iterable<Database> listMongoDatabases() {
    return orientDbDocumentService.list(Database.class, "select from " + Database.class.getSimpleName() +
        " where usedForIdentifiers = ? and mongoDbSettings is not null", false);
  }

  @Override
  public boolean hasDatabases(@Nullable Database.Usage usage) {
    return Iterables.size(list(usage)) > 0;
  }

  @Nonnull
  @Override
  public Database getDatabase(@Nonnull final String name) throws NoSuchDatabaseException {
    return orientDbDocumentService.execute(new OrientDbDocumentService.WithinDocumentTxCallback<Database>() {
      @Override
      public Database withinDocumentTx(ODatabaseDocumentTx db) {
        OIndex<?> index = db.getMetadata().getIndexManager().getIndex("Database.name");
        OIdentifiable identifiable = (OIdentifiable) index.get(name);
        if(identifiable == null) throw new NoSuchDatabaseException(name);
        return orientDbDocumentService.fromDocument(Database.class, identifiable.<ODocument>getRecord());
      }
    });
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
  public void saveDatabase(@Nonnull Database database)
      throws ConstraintViolationException, MultipleIdentifiersDatabaseException {

    //TODO valid unique url
    validUniqueIdentifiersDatabase(database);

    if(database.isDefaultStorage()) {
      Database previousDefaultStorageDatabase = getDefaultStorageDatabase();
      if(previousDefaultStorageDatabase == null) {
        save(database);
      } else {
        previousDefaultStorageDatabase.setDefaultStorage(false);
        save(database, previousDefaultStorageDatabase);
      }
    } else {
      save(database);
    }
  }

  private void save(@Nonnull Database database) {
    orientDbDocumentService.save(database, UNIQUE_INDEX);
  }

  private void save(@Nonnull final Database... databases) {
    for(Database database : databases) {
      defaultBeanValidator.validate(database);
    }

    orientDbDocumentService.execute(new OrientDbDocumentService.WithinDocumentTxCallbackWithoutResult() {
      @Override
      protected void withinDocumentTxWithoutResult(ODatabaseDocumentTx db) {
        Collection<ODocument> documents = new ArrayList<ODocument>(databases.length);
        for(Database database : databases) {
          ODocument document = orientDbDocumentService
              .findUnique(db, database.getClass(), UNIQUE_INDEX, database.getName());
          if(document == null) {
            document = orientDbDocumentService.toDocument(database);
          } else {
            orientDbDocumentService.copyToDocument(database, document);
          }
          documents.add(document);
        }

        db.begin(OTransaction.TXTYPE.OPTIMISTIC);
        for(ODocument document : documents) {
          document.save();
        }
        db.commit();
      }
    });
  }

  private void validUniqueIdentifiersDatabase(Database database) throws MultipleIdentifiersDatabaseException {
    if(database.isUsedForIdentifiers()) {
      try {
        Database identifiersDatabase = getIdentifiersDatabase();
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
    return orientDbDocumentService.uniqueResult(Database.class,
        "select from " + Database.class.getSimpleName() + " where usedForIdentifiers = ? and defaultStorage = ?", false,
        true);
  }

  @Override
  public void deleteDatabase(@Nonnull Database database) throws CannotDeleteDatabaseWithDataException {
    //TODO check if this database has data
    orientDbDocumentService.deleteUnique(Database.class, UNIQUE_INDEX, database.getName());
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
      save(database);
    }
    registrations.put(databaseName, usedByDatasource);
  }

  @Override
  public void unregister(@Nonnull String databaseName, String usedByDatasource) {
    Database database = getDatabase(databaseName);
    database.setEditable(true);
    save(database);
    registrations.remove(databaseName, usedByDatasource);
  }

  @Override
  public boolean hasIdentifiersDatabase() {
    return orientDbDocumentService
        .uniqueResult(Database.class, "select from " + Database.class.getSimpleName() + " where usedForIdentifiers = ?",
            true) != null;
  }

  @Nonnull
  @Override
  public Database getIdentifiersDatabase() throws IdentifiersDatabaseNotFoundException {
    Database database = orientDbDocumentService
        .uniqueResult(Database.class, "select from " + Database.class.getSimpleName() + " where usedForIdentifiers = ?",
            true);
    if(database == null) throw new IdentifiersDatabaseNotFoundException();
    return database;
  }

  @Nonnull
  @Override
  public DatasourceFactory createDataSourceFactory(@Nonnull String datasourceName, @Nonnull Database database) {
    String databaseName = database.getName();
    Preconditions.checkArgument(database.getUsage() == Database.Usage.STORAGE,
        "Cannot create DatasourceFactory for non storage database " + databaseName + ": " + database.getUsage());
    register(database.getName(), datasourceName);

    SqlSettings sqlSettings = database.getSqlSettings();
    if(sqlSettings != null) {
      if(sqlSettings.getSqlSchema() != SqlSettings.SqlSchema.HIBERNATE) {
        throw new IllegalArgumentException(
            "Cannot create datasource for non Hibernate storage database " + databaseName + ": " +
                sqlSettings.getSqlSchema());
      }
      return new HibernateDatasourceFactory(datasourceName,
          new DatabaseSessionFactoryProvider(datasourceName, this, database.getName()));
    }
    MongoDbSettings mongoDbSettings = database.getMongoDbSettings();
    if(mongoDbSettings != null) {
      return mongoDbSettings.createMongoDBDatasourceFactory(datasourceName);
    }
    throw new IllegalArgumentException("Unknown datasource config for database " + database.getClass());
  }

  private class DataSourceCacheLoader extends CacheLoader<String, DataSource> {

    @Override
    public DataSource load(String databaseName) throws Exception {
      log.info("Building DataSource {}", databaseName);
      return dataSourceFactory.createDataSource(getDatabase(databaseName));
    }
  }

  private static class DataSourceRemovalListener implements RemovalListener<String, DataSource> {

    @Override
    public void onRemoval(RemovalNotification<String, DataSource> notification) {
      log.info("Destroying DataSource {}", notification.getKey());
      DataSource dataSource = notification.getValue();
      if(dataSource != null) ((AbstractDataSourceBean) dataSource).close();
    }
  }

  private static class SessionFactoryRemovalListener implements RemovalListener<String, SessionFactory> {
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
  }

  private class SessionFactoryCacheLoader extends CacheLoader<String, SessionFactory> {

    @Override
    public SessionFactory load(String databaseName) throws Exception {
      log.info("Building SessionFactory {}", databaseName);
      return sessionFactoryFactory.getSessionFactory(getDataSource(databaseName, null));
    }
  }

}
