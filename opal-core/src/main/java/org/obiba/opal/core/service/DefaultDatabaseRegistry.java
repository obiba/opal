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
import jakarta.validation.constraints.NotNull;
import org.apache.commons.dbcp2.BasicDataSource;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.SocketFactoryProvider;
import org.obiba.magma.datasource.jdbc.JdbcDatasourceFactory;
import org.obiba.magma.support.EntitiesPredicate;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.repository.DatabaseRepository;
import org.obiba.opal.core.repository.ProjectRepository;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.event.DatasourceDeletedEvent;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.runtime.jdbc.H2DatabaseUrls;
import org.obiba.opal.core.runtime.jdbc.H2ProjectFolders;
import org.obiba.opal.core.service.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.io.File;
import java.sql.SQLException;

@Component
@SuppressWarnings("OverlyCoupledClass")
public class DefaultDatabaseRegistry implements DatabaseRegistry {

  private static final Logger log = LoggerFactory.getLogger(DefaultDatabaseRegistry.class);

  /**
   * Names beginning with this are Opal's own, so that a database it creates for a project can never collide with one
   * an operator registered. Opal already presents the identifiers database as {@code _identifiers}; what changes is
   * that the reservation is now enforced. Only new names are constrained: an installation that already holds a
   * database named this way keeps working, and stays editable.
   */
  private static final String RESERVED_NAME_PREFIX = "_";

  @Autowired
  private DataSourceFactory dataSourceFactory;

  @Autowired
  private DatabaseRepository databaseRepository;

  /**
   * Read to answer one question - does the project owning this database still exist - which decides whether an
   * operator may edit or delete it. A repository and not {@code ProjectService}, because the dependency runs the other
   * way round: the service that creates project databases is built on this registry.
   */
  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private SocketFactoryProvider socketFactoryProvider;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Value("${OPAL_HOME}/data/h2")
  private File h2Root;

  private final LoadingCache<String, DataSource> dataSourceCache = CacheBuilder.newBuilder() //
      .removalListener(new DataSourceRemovalListener()) //
      .build(new DataSourceCacheLoader());

  private final SetMultimap<String, String> registrations = Multimaps
      .synchronizedSetMultimap(HashMultimap.<String, String>create());

  @Override
  public void start() {
    processHibernate5Upgrade();
  }

  @Override
  public void stop() {
    dataSourceCache.invalidateAll();
    registrations.clear();
  }

  @Override
  public Iterable<Database> list() {
    return databaseRepository.findByUsedForIdentifiersOrderByName(false);
  }

  @Override
  public Iterable<Database> list(@Nullable Database.Usage usage) {
    if(usage == null) {
      return list();
    }
    return databaseRepository.findByUsedForIdentifiersAndUsageOrderByName(false, usage);
  }

  @Override
  public Iterable<Database> listSqlDatabases() {
    return databaseRepository.findByUsedForIdentifiersAndSqlSettingsIsNotNullOrderByName(false);
  }

  @Override
  public Iterable<Database> listMongoDatabases() {
    return databaseRepository.findByUsedForIdentifiersAndMongoDbSettingsIsNotNullOrderByName(false);
  }

  /**
   * The one listing that filters: this answers "has an operator provided storage", and drives the setup prompts. A
   * database Opal made for a project is not an answer to that - it would report storage on a server where none has
   * been configured at all. Every other listing includes project-owned databases, which is what the upgrade steps,
   * the Hibernate 5 sequence fix and the H2 checkpointer need.
   */
  @Override
  public boolean hasDatabases(@Nullable Database.Usage usage) {
    return Iterables.any(list(usage), database -> !database.isProjectOwned());
  }

  @Override
  public boolean hasDatabase(@org.jetbrains.annotations.Nullable String name) {
    return databaseRepository.findByName(name).isPresent();
  }

  @NotNull
  @Override
  public Database getDatabase(@NotNull String name) throws NoSuchDatabaseException {
    return databaseRepository.findByName(name).orElseThrow(() -> new NoSuchDatabaseException(name));
  }

  @Override
  @Transactional
  public DataSource getDataSource(@NotNull String name, @Nullable String usedByDatasource) {
    register(name, usedByDatasource);
    return dataSourceCache.getUnchecked(name);
  }

  @Override
  public Iterable<DataSource> getLoadedDataSources() {
    // asMap() is a view of what the cache holds, and reading it does not go through the CacheLoader: a database that
    // has never been used stays closed.
    return ImmutableList.copyOf(dataSourceCache.asMap().values());
  }

  @Override
  public void create(@NotNull Database database)
      throws ConstraintViolationException, MultipleIdentifiersDatabaseException {
    if(database.getName() != null && database.getName().startsWith(RESERVED_NAME_PREFIX)) {
      throw new IllegalArgumentException(
          "Database name '" + database.getName() + "' is reserved: names starting with '" + RESERVED_NAME_PREFIX +
              "' are Opal's own");
    }
    if(database.isProjectOwned()) {
      // ownership is Opal's to give, never something a payload can claim
      throw new IllegalArgumentException("A database cannot be registered as owned by a project");
    }
    createDatabase(database);
  }

  @Override
  public void createProjectOwned(@NotNull Database database) throws ConstraintViolationException {
    Preconditions.checkArgument(database.isProjectOwned(), "Not a project-owned database: " + database.getName());
    createDatabase(database);
  }

  private void createDatabase(Database database) {
    if(databaseRepository.findByName(database.getName()).isEmpty()) {
      persist(database);
    } else {
      throw new IllegalArgumentException("Database already exists");
    }
  }

  @Override
  public void update(@NotNull Database database)
      throws ConstraintViolationException, MultipleIdentifiersDatabaseException {
    Database stored = databaseRepository.findByName(database.getName())
        .orElseThrow(() -> new IllegalArgumentException("Cannot update non existing Database " + database.getName()));
    assertNotOwnedByALivingProject(stored);
    // an owner survives an update: a row left behind by an archived deletion re-attaches when a project of that name
    // is created again, and an operator editing it in the meantime does not silently take that away
    if(!database.isProjectOwned()) database.setOwnerProject(stored.getOwnerProject());

    destroyCache(database.getName());
    persist(database);
  }

  /**
   * @throws DatabaseOwnedByProjectException if a project owns this database and still exists. A project that failed to
   * load still exists: what makes a leftover row an operator's to act on is the project being gone, not its datasource
   * being absent.
   */
  private void assertNotOwnedByALivingProject(Database database) {
    String ownerProject = database.getOwnerProject();
    if(ownerProject != null && projectRepository.findByName(ownerProject).isPresent()) {
      throw new DatabaseOwnedByProjectException(database.getName(), ownerProject);
    }
  }

  private void persist(Database database) {
    validUniqueIdentifiersDatabase(database);
    validH2Database(database);

    if(database.isDefaultStorage()) {
      Database previousDefaultStorageDatabase = getDefaultStorageDatabase();
      if(previousDefaultStorageDatabase == null || previousDefaultStorageDatabase.equals(database)) {
        databaseRepository.upsert(database);
      } else {
        previousDefaultStorageDatabase.setDefaultStorage(false);
        databaseRepository.upsert(previousDefaultStorageDatabase);
        databaseRepository.upsert(database);
      }
    } else {
      databaseRepository.upsert(database);
    }
  }

  /**
   * H2 is an embedded database: it is registered by name only, its file lives in the Opal H2 folder, and it can only
   * be used for storage as there is no pre-existing database to import from or export to.
   */
  private void validH2Database(Database database) throws InvalidH2DatabaseException {
    SqlSettings sqlSettings = database.getSqlSettings();
    if(sqlSettings == null || !H2DatabaseUrls.isH2(sqlSettings.getDriverClass())) return;

    if(database.getUsage() != Database.Usage.STORAGE) {
      throw new InvalidH2DatabaseException("H2 databases can only be used for storage");
    }
    H2DatabaseUrls.validateProperties(sqlSettings.getProperties());

    if(database.isProjectOwned()) {
      // a project database is a folder of its own, named after the project: it collides with nothing, and the one
      // name it could collide with - another project's - is refused where projects are named
      H2DatabaseUrls.validateProject(sqlSettings.getUrl(), h2Root);
      return;
    }
    H2DatabaseUrls.validate(sqlSettings.getUrl(), h2Root);
    validUniqueH2DatabaseFile(database, H2DatabaseUrls.getDatabaseName(sqlSettings.getUrl()));
  }

  /**
   * Two registrations naming the same file would be two Opal databases sharing one set of tables. The comparison
   * ignores case: on a case insensitive file system 'opal' and 'Opal' are the same file, and a name that only holds on
   * Linux would not survive a move of the H2 folder.
   */
  private void validUniqueH2DatabaseFile(Database database, String name) throws InvalidH2DatabaseException {
    // the identifiers database is an H2 candidate too, so look at every SQL database and not just the listed ones
    for(Database other : databaseRepository.findBySqlSettingsIsNotNull()) {
      if(other.getName().equals(database.getName())) continue;
      // a project database is a folder, not a file directly under the H2 folder, so it is not what this compares
      if(other.isProjectOwned()) continue;
      SqlSettings otherSettings = other.getSqlSettings();
      if(otherSettings == null || !H2DatabaseUrls.isH2(otherSettings.getDriverClass())) continue;
      if(name.equalsIgnoreCase(H2DatabaseUrls.getDatabaseName(otherSettings.getUrl()))) {
        throw new InvalidH2DatabaseException(
            "H2 database '" + name + "' is already registered as '" + other.getName() + "'");
      }
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
    return databaseRepository.findByUsedForIdentifiersAndDefaultStorage(false, true).orElse(null);
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
    Database stored = databaseRepository.findByName(database.getName()).orElse(database);
    assertNotOwnedByALivingProject(stored);

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
    databaseRepository.deleteByKey(database);
    destroyCache(database.getName());
    // nobody owns it any more, so its files are nobody's: an archived deletion is reclaimed here, files and all
    if(stored.isProjectOwned()) deleteProjectFiles(stored);
  }

  @Override
  @Transactional(propagation = Propagation.NEVER)
  public void deleteProjectOwned(@NotNull Database database) {
    Preconditions.checkArgument(database.isProjectOwned(), "Not a project-owned database: " + database.getName());

    // closing the last connection is what closes the H2 store and releases the file lock, so the cache goes first
    destroyCache(database.getName());
    databaseRepository.deleteByKey(database);
    deleteProjectFiles(database);
  }

  private void deleteProjectFiles(Database database) {
    SqlSettings sqlSettings = database.getSqlSettings();
    if(sqlSettings == null || !H2DatabaseUrls.isH2(sqlSettings.getDriverClass())) return;
    H2ProjectFolders.delete(H2DatabaseUrls.projectFolder(database.getOwnerProject(), h2Root));
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
    return databaseRepository.findByUsedForIdentifiers(true).isPresent();
  }

  @NotNull
  @Override
  public Database getIdentifiersDatabase() throws IdentifiersDatabaseNotFoundException {
    return databaseRepository.findByUsedForIdentifiers(true)
        .orElseThrow(IdentifiersDatabaseNotFoundException::new);
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
