/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.*;
import com.google.common.collect.*;
import com.google.common.eventbus.Subscribe;
import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import org.apache.commons.dbcp2.BasicDataSource;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.SocketFactoryProvider;
import org.obiba.magma.datasource.jdbc.JdbcDatasourceFactory;
import org.obiba.magma.support.EntitiesPredicate;
import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.event.DatasourceDeletedEvent;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.service.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;

@Component
@SuppressWarnings("OverlyCoupledClass")
public class DefaultDatabaseRegistry implements DatabaseRegistry {

  private static final Logger log = LoggerFactory.getLogger(DefaultDatabaseRegistry.class);

  @Autowired
  private DataSourceFactory dataSourceFactory;

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private SocketFactoryProvider socketFactoryProvider;

  @Autowired
  private TransactionTemplate transactionTemplate;

  private final LoadingCache<String, DataSource> dataSourceCache = CacheBuilder.newBuilder() //
      .removalListener(new DataSourceRemovalListener()) //
      .build(new DataSourceCacheLoader());

  private final SetMultimap<String, String> registrations = Multimaps
      .synchronizedSetMultimap(HashMultimap.<String, String>create());

  @Override
  public void start() {
    orientDbService.createUniqueIndex(Database.class);
    processHibernate5Upgrade();
  }

  @Override
  public void stop() {
    dataSourceCache.invalidateAll();
    registrations.clear();
  }

  @Override
  public Iterable<Database> list() {
    return orientDbService
        .list(Database.class, "select from " + Database.class.getSimpleName() + " where usedForIdentifiers = ? order by name", false);
  }

  @Override
  public Iterable<Database> list(@Nullable Database.Usage usage) {
    if(usage == null) {
      return list();
    }
    return orientDbService.list(Database.class,
        "select from " + Database.class.getSimpleName() + " where usedForIdentifiers = ? and usage = ? order by name", false, usage.toString());
  }

  @Override
  public Iterable<Database> listSqlDatabases() {
    return orientDbService.list(Database.class,
        "select from " + Database.class.getSimpleName() + " where usedForIdentifiers = ? and sqlSettings is not null order by name",
        false);
  }

  @Override
  public Iterable<Database> listMongoDatabases() {
    return orientDbService.list(Database.class, "select from " + Database.class.getSimpleName() +
        " where usedForIdentifiers = ? and mongoDbSettings is not null order by name", false);
  }

  @Override
  public boolean hasDatabases(@Nullable Database.Usage usage) {
    return !Iterables.isEmpty(list(usage));
  }

  @NotNull
  @Override
  public Database getDatabase(@NotNull String name) throws NoSuchDatabaseException {
    Database database = orientDbService.findUnique(Database.Builder.create().name(name).build());
    if(database == null) {
      throw new NoSuchDatabaseException(name);
    }
    return database;
  }

  @Override
  @Transactional
  public DataSource getDataSource(@NotNull String name, @Nullable String usedByDatasource) {
    register(name, usedByDatasource);
    return dataSourceCache.getUnchecked(name);
  }

  @Override
  public void create(@NotNull Database database)
      throws ConstraintViolationException, MultipleIdentifiersDatabaseException {
    if(orientDbService.findUnique(database) == null) {
      persist(database);
    } else {
      throw new IllegalArgumentException("Database already exists");
    }
  }

  @Override
  public void update(@NotNull Database database)
      throws ConstraintViolationException, MultipleIdentifiersDatabaseException {
    Preconditions.checkArgument(orientDbService.findUnique(database) != null,
        "Cannot update non existing Database " + database.getName());

    destroyCache(database.getName());
    persist(database);
  }

  private void persist(Database database) {
    validUniqueIdentifiersDatabase(database);

    if(database.isDefaultStorage()) {
      Database previousDefaultStorageDatabase = getDefaultStorageDatabase();
      if(previousDefaultStorageDatabase == null || previousDefaultStorageDatabase.equals(database)) {
        orientDbService.save(database, database);
      } else {
        previousDefaultStorageDatabase.setDefaultStorage(false);
        orientDbService.save(ImmutableMap
            .<HasUniqueProperties, HasUniqueProperties>of(database, database, previousDefaultStorageDatabase,
                previousDefaultStorageDatabase));
      }
    } else {
      orientDbService.save(database, database);
    }
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
    return orientDbService.uniqueResult(Database.class,
        "select from " + Database.class.getSimpleName() + " where usedForIdentifiers = ? and defaultStorage = ?", false,
        true);
  }

  @Override
  public boolean hasDatasource(@NotNull Database database) {
    return registrations.containsKey(database.getName());
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("TypeMayBeWeakened")
  public boolean hasEntities(@NotNull Database database) {
    if(!hasDatasource(database)) return false;

    if(database.isUsedForIdentifiers()) {
      return identifiersTableService.hasEntities();
    }

    EntitiesPredicate.NonViewEntitiesPredicate predicate = new EntitiesPredicate.NonViewEntitiesPredicate();
    for(String datasourceName : registrations.get(database.getName())) {
      Datasource datasource = MagmaEngine.get().getDatasource(datasourceName);
      if(datasource.hasEntities(predicate)) return true;
    }
    return false;
  }

  @Override
  @Transactional(propagation = Propagation.NEVER)
  public void delete(@NotNull Database database)
      throws CannotDeleteDatabaseLinkedToDatasourceException, CannotDeleteDatabaseWithDataException {
    if(database.isUsedForIdentifiers()) {
      if(hasEntities(database)) {
        throw new CannotDeleteDatabaseWithDataException(database.getName());
      }
      unregister(database.getName(), identifiersTableService.getDatasourceName());
    } else {
      if(hasDatasource(database)) {
        throw new CannotDeleteDatabaseLinkedToDatasourceException(database.getName());
      }
      unregister(database.getName(), identifiersTableService.getDatasourceName());
    }
    orientDbService.delete(database);
    destroyCache(database.getName());
  }

  private void destroyCache(String name) {
    dataSourceCache.invalidate(name);
  }

  private void register(String databaseName, @Nullable String usedByDatasource) {
    if(Strings.isNullOrEmpty(usedByDatasource)) return;
    registrations.put(databaseName, usedByDatasource);
  }

  @Override
  @Transactional(propagation = Propagation.NEVER)
  public void unregister(@NotNull String databaseName, String usedByDatasource) {
    // close SessionFactory or JDBC dataSource
    registrations.remove(databaseName, usedByDatasource);
    if(!registrations.containsKey(databaseName)) destroyCache(databaseName);
  }

  @Override
  public boolean hasIdentifiersDatabase() {
    return orientDbService
        .uniqueResult(Database.class, "select from " + Database.class.getSimpleName() + " where usedForIdentifiers = ?",
            true) != null;
  }

  @NotNull
  @Override
  public Database getIdentifiersDatabase() throws IdentifiersDatabaseNotFoundException {
    Database database = orientDbService
        .uniqueResult(Database.class, "select from " + Database.class.getSimpleName() + " where usedForIdentifiers = ?",
            true);
    if(database == null) throw new IdentifiersDatabaseNotFoundException();
    return database;
  }

  @NotNull
  @Override
  public DatasourceFactory createDatasourceFactory(@NotNull String datasourceName, @NotNull Database database) {
    String databaseName = database.getName();
    register(databaseName, datasourceName);

    SqlSettings sqlSettings = database.getSqlSettings();

    if(sqlSettings != null) {
      switch(sqlSettings.getSqlSchema()) {
        case JDBC:
          DataSource datasource = getDataSource(databaseName, datasourceName);
          JdbcDatasourceFactory dsFactory = new JdbcDatasourceFactory();
          dsFactory.setName(datasourceName);
          dsFactory.setDataSource(datasource);
          dsFactory.setDatasourceSettings(sqlSettings.getJdbcDatasourceSettings());
          dsFactory.setDataSourceTransactionManager(new DataSourceTransactionManager(datasource));
          return dsFactory;

        default:
          unregister(databaseName, datasourceName);
          throw new IllegalArgumentException(
              "Cannot create datasource for non SQL storage database " + databaseName + ": " +
                  sqlSettings.getSqlSchema());
      }
    }

    MongoDbSettings mongoDbSettings = database.getMongoDbSettings();

    if(mongoDbSettings != null) {
      return mongoDbSettings.createMongoDBDatasourceFactory(datasourceName, socketFactoryProvider);
    }

    unregister(databaseName, datasourceName);
    throw new IllegalArgumentException("Unknown datasource config for database " + database.getClass());
  }

  @Subscribe
  public void onDatasourceDeleted(DatasourceDeletedEvent event) {
    //Remove from registrations
    ImmutableList<String> keys = ImmutableList.copyOf(registrations.keySet());
    for(String key : keys) {
      registrations.remove(key, event.getDatasource().getName());
    }
  }

  //
  // Private methods and classes
  //


  /**
   * Hibernate5 has changed the GeneratedValue AUTO strategy used for identifiers.
   * See <a href="https://github.com/obiba/opal/issues/3777">Problem with hibernate_sequence in Opal 4.5.2/3 </a>.
   */
  private void processHibernate5Upgrade() {
    try {
      listSqlDatabases().forEach(database -> {
        if (database.getSqlSettings().getSqlSchema().equals(SqlSettings.SqlSchema.HIBERNATE)) {
          String driverClass = database.getSqlSettings().getDriverClass();
          if ("com.mysql.jdbc.Driver".equals(driverClass) || "org.mariadb.jdbc.Driver".equals(driverClass)) {
            processHibernate5Upgrade(database);
          }
        }
      });
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.warn("Hibernate5 upgrade failure", e);
      else
        log.warn("Hibernate5 upgrade failure: {}", e.getMessage());
    }
  }

  private void processHibernate5Upgrade(Database database) {
    log.info("Checking if database {} is to be upgraded...", database.getName());

    JdbcOperations jdbcTemplate = new JdbcTemplate(getDataSource(database.getName(), null));

    long nextVal = queryForLong(jdbcTemplate, "select next_val from hibernate_sequence");
    if (nextVal >= 0) {
      // obiba/opal#3777 check nextVal is big enough after hibernate5 upgrade
      long max = queryForLong(jdbcTemplate, "select max(id) from datasource");
      max = Math.max(max, queryForLong(jdbcTemplate, "select max(id) from value_table"));
      max = Math.max(max, queryForLong(jdbcTemplate, "select max(id) from value_table"));
      max = Math.max(max, queryForLong(jdbcTemplate, "select max(id) from variable"));
      max = Math.max(max, queryForLong(jdbcTemplate, "select max(id) from category"));
      max = Math.max(max, queryForLong(jdbcTemplate, "select max(id) from variable_entity"));
      max = Math.max(max, queryForLong(jdbcTemplate, "select max(id) from value_set"));
      max = Math.max(max, queryForLong(jdbcTemplate, "select max(id) from value_set_binary_value"));

      if (max > nextVal) {
        log.info("Updating hibernate_sequence.next_val of database {}", database.getName());
        nextVal = max + 1;
        execute(jdbcTemplate, "update hibernate_sequence set next_val = " + nextVal);
      }
    }
  }

  private long queryForLong(final JdbcOperations jdbcTemplate, final String sql) {
    Long rval = transactionTemplate.execute(status -> {
      try {
        return jdbcTemplate.queryForObject(sql, Long.class);
      } catch(Exception e) {
        log.warn("SQL execution error '{}': {}", sql, e.getMessage());
      }
      return null;
    });
    return rval == null ? -1 : rval;
  }

  private void execute(final JdbcOperations jdbcTemplate, final String sql) {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          jdbcTemplate.execute(sql);
          log.info("SQL executed '{}'", sql);
        } catch(Exception e) {
          if (log.isDebugEnabled())
            log.warn("SQL execution error '{}'", sql, e);
          else
            log.warn("SQL execution error '{}': {}", sql, e.getMessage());
        }
      }
    });
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
      String database = notification.getKey();
      log.info("Destroying DataSource {}", database);
      DataSource dataSource = notification.getValue();
      if(dataSource == null) {
        log.info("Cannot close null DataSource {}", database);
      } else if (dataSource instanceof BasicDataSource) {
        try {
          ((BasicDataSource) dataSource).close();
        } catch(SQLException e) {
          //noinspection StringConcatenationArgumentToLogCall
          log.warn("Ignoring exception during DataSource " + database + " shutdown: ", e);
        }
      }
    }
  }

}
