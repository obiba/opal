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

import com.google.common.base.Predicate;
import com.google.common.eventbus.EventBus;
import org.easymock.EasyMock;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.SocketFactoryProvider;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.repository.DatabaseRepository;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.service.database.CannotDeleteDatabaseLinkedToDatasourceException;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.database.IdentifiersDatabaseNotFoundException;
import org.obiba.opal.core.service.database.InvalidH2DatabaseException;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.repository.ProjectRepository;
import org.obiba.opal.core.runtime.jdbc.H2DatabaseUrls;
import org.obiba.opal.core.runtime.jdbc.H2ProjectFolders;
import org.obiba.opal.core.service.database.DatabaseOwnedByProjectException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import javax.net.ssl.SSLSocketFactory;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.obiba.opal.core.domain.database.Database.Usage;

@ContextConfiguration(classes = DefaultDatabaseRegistryTest.Config.class)
public class DefaultDatabaseRegistryTest extends AbstractConfigDbTest {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private DatabaseRepository databaseRepository;

  @Autowired
  private DataSourceFactory dataSourceFactory;

  @Autowired
  private ProjectRepository projectRepository;

  @Value("${OPAL_HOME}/data/h2")
  private File h2Root;

  @Override
  public void startDB() throws Exception {
    super.startDB();
    databaseRegistry.stop();
    databaseRepository.deleteAll();
    projectRepository.deleteAll();
  }

  @Override
  public void stopDB() {
    super.stopDB();
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_new_sql_database() {
    Database database = createSqlDatabase();
    databaseRegistry.create(database);

    List<Database> databases = newArrayList(databaseRegistry.list());
    assertThat(databases).hasSize(1);
    assertDatabaseEquals(database, databases.get(0));

    Database found = databaseRegistry.getDatabase(database.getName());
    assertDatabaseEquals(database, found);

    assertThat(databaseRegistry.list(Usage.IMPORT)).hasSize(1);
    assertThat(databaseRegistry.list(Usage.STORAGE)).isEmpty();
    assertThat(databaseRegistry.list(Usage.EXPORT)).isEmpty();
    assertThat(databaseRegistry.listSqlDatabases()).hasSize(1);
    assertThat(databaseRegistry.listMongoDatabases()).isEmpty();
  }

  @Test
  public void test_new_mongo_database() {
    Database database = createMongoDatabase();
    databaseRegistry.create(database);

    List<Database> databases = newArrayList(databaseRegistry.list());
    assertThat(databases).hasSize(1);
    assertDatabaseEquals(database, databases.get(0));

    Database found = databaseRegistry.getDatabase(database.getName());
    assertDatabaseEquals(database, found);

    assertThat(databaseRegistry.listMongoDatabases()).hasSize(1);
    assertThat(databaseRegistry.listSqlDatabases()).isEmpty();
  }

  @Test
  public void test_update_sql_database() {
    Database database = createSqlDatabase();
    databaseRegistry.create(database);

    database.setUsage(Usage.STORAGE);
    assertThat(database.getSqlSettings()).isNotNull();
    database.getSqlSettings().setUsername("user2");
    database.getSqlSettings().setUrl("url2");
    databaseRegistry.update(database);

    List<Database> databases = newArrayList(databaseRegistry.list());
    assertThat(databases).hasSize(1);
    assertDatabaseEquals(database, databases.get(0));

    Database found = databaseRegistry.getDatabase(database.getName());
    assertDatabaseEquals(database, found);

    assertThat(databaseRegistry.listSqlDatabases()).hasSize(1);
    assertThat(databaseRegistry.listMongoDatabases()).isEmpty();
  }

  @Test
  public void test_update_mongo_database() {
    Database database = createMongoDatabase();
    databaseRegistry.create(database);

    database.setUsage(Usage.STORAGE);
    assertThat(database.getMongoDbSettings()).isNotNull();
    database.getMongoDbSettings().setUsername("user2");
    database.getMongoDbSettings().setUrl("url2");
    databaseRegistry.update(database);

    List<Database> databases = newArrayList(databaseRegistry.list());
    assertThat(databases).hasSize(1);
    assertDatabaseEquals(database, databases.get(0));

    Database found = databaseRegistry.getDatabase(database.getName());
    assertDatabaseEquals(database, found);

    assertThat(databaseRegistry.listMongoDatabases()).hasSize(1);
    assertThat(databaseRegistry.listSqlDatabases()).isEmpty();
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_create_database_with_same_name() {
    Database database = createSqlDatabase();
    databaseRegistry.create(database);
    databaseRegistry.create(database);
  }

  @Test
  public void test_get_identifiers_database() {
    Database database = Database.Builder.create().name("sql database").usage(Usage.STORAGE).usedForIdentifiers(true)
        .build();
    databaseRegistry.create(database);
    Database found = databaseRegistry.getIdentifiersDatabase();
    assertThat(found.isUsedForIdentifiers()).isTrue();
    assertThat(databaseRegistry.hasIdentifiersDatabase()).isTrue();
    assertThat(databaseRegistry.list()).isEmpty();
    assertThat(databaseRegistry.list(Usage.IMPORT)).isEmpty();
    assertThat(databaseRegistry.list(Usage.STORAGE)).isEmpty();
    assertThat(databaseRegistry.list(Usage.EXPORT)).isEmpty();
    assertThat(databaseRegistry.listMongoDatabases()).isEmpty();
    assertThat(databaseRegistry.listSqlDatabases()).isEmpty();
  }

  @Test(expected = IdentifiersDatabaseNotFoundException.class)
  public void test_get_null_identifiers_database() {
    databaseRegistry.getIdentifiersDatabase();
  }

  @Test
  public void test_has_identifiers_database() {
    assertThat(databaseRegistry.hasIdentifiersDatabase()).isFalse();
  }

  @Test
  public void test_delete_database() {
    Database database = createSqlDatabase();
    databaseRegistry.create(database);
    databaseRegistry.delete(database);

    assertThat(databaseRegistry.list()).isEmpty();
  }

  @Test(expected = CannotDeleteDatabaseLinkedToDatasourceException.class)
  public void test_delete_database_with_entities() {
    Database database = createSqlDatabase();
    databaseRegistry.create(database);

    DataSource mockDataSource = EasyMock.createMock(DataSource.class);
    reset(dataSourceFactory);
    expect(dataSourceFactory.createDataSource(database)).andReturn(mockDataSource).once();
    replay(dataSourceFactory);

    Datasource mockDatasource = EasyMock.createMock(Datasource.class);
    expect(mockDatasource.getName()).andReturn("jdbc-datasource").atLeastOnce();
    expect(mockDatasource.hasEntities(EasyMock.<Predicate<ValueTable>>anyObject())).andReturn(true).once();
    mockDatasource.initialise();
    EasyMock.expectLastCall().once();
    mockDatasource.dispose();
    EasyMock.expectLastCall().once();
    replay(mockDatasource);
    MagmaEngine.get().addDatasource(mockDatasource);

    databaseRegistry.getDataSource(database.getName(), "jdbc-datasource");
    databaseRegistry.delete(database);
  }

  @Test
  public void test_list_sql_databases() {
    databaseRegistry.create(createSqlDatabase());
    assertThat(databaseRegistry.list()).hasSize(1);
    assertThat(databaseRegistry.listSqlDatabases()).hasSize(1);
    assertThat(databaseRegistry.listMongoDatabases()).isEmpty();
  }

  @Test
  public void test_list_mongo_databases() {
    databaseRegistry.create(createMongoDatabase());
    assertThat(databaseRegistry.list()).hasSize(1);
    assertThat(databaseRegistry.listSqlDatabases()).isEmpty();
    assertThat(databaseRegistry.listMongoDatabases()).hasSize(1);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void test_change_default_storage() {
    Database database = createSqlDatabase();
    databaseRegistry.create(database);

    Database database2 = createSqlDatabase();
    database2.setName("default storage");
    database2.getSqlSettings().setUrl("new url");
    databaseRegistry.create(database2);

    assertThat(databaseRegistry.list()).hasSize(2);

    assertThat(databaseRegistry.getDatabase(database.getName()).isDefaultStorage()).isFalse();
    assertThat(databaseRegistry.getDatabase(database2.getName()).isDefaultStorage()).isTrue();
  }

  @Test
  public void test_get_datasource() {
    Database database = createSqlDatabase();
    databaseRegistry.create(database);

    DataSource mockDatasource = EasyMock.createMock(DataSource.class);

    reset(dataSourceFactory);
    expect(dataSourceFactory.createDataSource(database)).andReturn(mockDatasource).once();
    replay(dataSourceFactory);

    DataSource datasource = databaseRegistry.getDataSource(database.getName(), "jdbc-datasource");
    verify(dataSourceFactory);

    assertThat(mockDatasource).isEqualTo(datasource);
    assertThat(databaseRegistry.hasDatasource(database)).isTrue();
  }

  @Test
  public void test_unregister() {
    Database database = createSqlDatabase();
    databaseRegistry.create(database);

    DataSource mockDatasource = EasyMock.createMock(DataSource.class);

    reset(dataSourceFactory);
    expect(dataSourceFactory.createDataSource(database)).andReturn(mockDatasource).once();
    replay(dataSourceFactory);

    databaseRegistry.getDataSource(database.getName(), "jdbc-datasource");
    verify(dataSourceFactory);

    databaseRegistry.unregister(database.getName(), "jdbc-datasource");

    assertThat(databaseRegistry.hasDatasource(database)).isFalse();
  }

  @Test
  public void test_new_h2_database() {
    Database database = createH2Database(Usage.STORAGE, "jdbc:h2:file:opal");
    databaseRegistry.create(database);

    assertDatabaseEquals(database, databaseRegistry.getDatabase(database.getName()));
    assertThat(databaseRegistry.listSqlDatabases()).hasSize(1);
  }

  @Test
  public void test_h2_database_is_storage_only() {
    for(Usage usage : new Usage[] { Usage.IMPORT, Usage.EXPORT }) {
      Database database = createH2Database(usage, "jdbc:h2:file:opal");
      try {
        databaseRegistry.create(database);
        fail("Expected an InvalidH2DatabaseException for usage: " + usage);
      } catch(InvalidH2DatabaseException ignored) {
      }
    }
    assertThat(databaseRegistry.listSqlDatabases()).isEmpty();
  }

  @Test
  public void test_h2_database_url_must_be_a_name() {
    for(String url : new String[] { "jdbc:h2:file:../escape", "jdbc:h2:file:/var/lib/opal", "jdbc:h2:mem:opal" }) {
      Database database = createH2Database(Usage.STORAGE, url);
      try {
        databaseRegistry.create(database);
        fail("Expected an InvalidH2DatabaseException for URL: " + url);
      } catch(InvalidH2DatabaseException ignored) {
      }
    }
    assertThat(databaseRegistry.listSqlDatabases()).isEmpty();
  }

  @Test
  public void test_h2_database_rejects_an_init_connection_property() {
    Database database = createH2Database(Usage.STORAGE, "jdbc:h2:file:opal");
    database.getSqlSettings().setProperties("INIT=RUNSCRIPT FROM 'https://elsewhere.example/payload.sql'");
    try {
      databaseRegistry.create(database);
      fail("Expected an InvalidH2DatabaseException");
    } catch(InvalidH2DatabaseException ignored) {
    }
    assertThat(databaseRegistry.listSqlDatabases()).isEmpty();
  }

  @Test
  public void test_h2_database_file_is_registered_once() {
    databaseRegistry.create(createH2Database(Usage.STORAGE, "jdbc:h2:file:opal"));

    for(String url : new String[] { "jdbc:h2:file:opal", "jdbc:h2:file:OPAL" }) {
      Database duplicate = createH2Database(Usage.STORAGE, url);
      duplicate.setName("another-" + url);
      try {
        databaseRegistry.create(duplicate);
        fail("Expected an InvalidH2DatabaseException for URL: " + url);
      } catch(InvalidH2DatabaseException ignored) {
      }
    }
    assertThat(databaseRegistry.listSqlDatabases()).hasSize(1);
  }

  @Test
  public void test_h2_database_can_be_updated_in_place() {
    Database database = createH2Database(Usage.STORAGE, "jdbc:h2:file:opal");
    databaseRegistry.create(database);

    // the database is not a duplicate of itself
    database.getSqlSettings().setUsername("opal");
    databaseRegistry.update(database);

    assertThat(databaseRegistry.getDatabase(database.getName()).getSqlSettings().getUsername()).isEqualTo("opal");
  }

  @Test
  public void test_h2_database_usage_is_validated_on_update() {
    Database database = createH2Database(Usage.STORAGE, "jdbc:h2:file:opal");
    databaseRegistry.create(database);

    database.setUsage(Usage.EXPORT);
    try {
      databaseRegistry.update(database);
      fail("Expected an InvalidH2DatabaseException");
    } catch(InvalidH2DatabaseException ignored) {
    }
    assertThat(databaseRegistry.getDatabase(database.getName()).getUsage()).isEqualTo(Usage.STORAGE);
  }

  @Test
  public void test_owner_project_is_persisted_and_found() {
    Database registered = createSqlDatabase();
    databaseRegistry.create(registered);

    Database owned = createH2Database(Usage.STORAGE, "jdbc:h2:file:CLSA/data");
    owned.setName("_project_CLSA");
    owned.setDefaultStorage(false);
    owned.setOwnerProject("CLSA");
    databaseRepository.save(owned);

    assertThat(databaseRepository.findByName("_project_CLSA").get().isProjectOwned()).isTrue();
    assertThat(databaseRepository.findByName(registered.getName()).get().isProjectOwned()).isFalse();

    assertThat(databaseRepository.findByOwnerProject("CLSA").get().getName()).isEqualTo("_project_CLSA");
    assertThat(databaseRepository.findByOwnerProject("clsa")).isEqualTo(Optional.empty());
    assertThat(databaseRepository.findByOwnerProjectIgnoreCase("clsa").get().getName()).isEqualTo("_project_CLSA");
  }

  @Test
  public void test_a_project_owns_at_most_one_database() {
    Database first = createH2Database(Usage.STORAGE, "jdbc:h2:file:CLSA/data");
    first.setName("_project_CLSA");
    first.setOwnerProject("CLSA");
    databaseRepository.save(first);

    Database second = createH2Database(Usage.STORAGE, "jdbc:h2:file:CLSA2/data");
    second.setName("_project_CLSA_2");
    second.setOwnerProject("CLSA");
    try {
      databaseRepository.saveAndFlush(second);
      fail("Expected uk_databases_owner_project to be violated");
    } catch(DataIntegrityViolationException ignored) {
    }
  }

  //
  // Project-owned databases
  //

  @Test
  public void test_a_project_owned_database_is_listed_like_any_other() {
    createProjectOwnedDatabase("CLSA");

    assertThat(databaseRegistry.list()).hasSize(1);
    assertThat(databaseRegistry.listSqlDatabases()).hasSize(1);
    assertThat(databaseRegistry.list(Usage.STORAGE)).hasSize(1);
    assertThat(databaseRegistry.getDatabase("_project_CLSA").getOwnerProject()).isEqualTo("CLSA");
    assertThat(databaseRegistry.hasDatabase("_project_CLSA")).isTrue();
  }

  /**
   * "Has an operator provided storage" is what drives the setup prompts, and a database Opal made for a project is
   * not an answer to it.
   */
  @Test
  public void test_a_project_owned_database_is_not_storage_an_operator_provided() {
    createProjectOwnedDatabase("CLSA");
    assertThat(databaseRegistry.hasDatabases(Usage.STORAGE)).isFalse();
    assertThat(databaseRegistry.hasDatabases(null)).isFalse();

    databaseRegistry.create(createH2Database(Usage.STORAGE, "jdbc:h2:file:opal-data"));
    assertThat(databaseRegistry.hasDatabases(Usage.STORAGE)).isTrue();
  }

  @Test
  public void test_create_rejects_a_reserved_name() {
    Database database = createSqlDatabase();
    database.setName("_project_CLSA");
    try {
      databaseRegistry.create(database);
      fail("Expected an IllegalArgumentException for a name starting with '_'");
    } catch(IllegalArgumentException ignored) {
    }
    assertThat(databaseRegistry.list()).isEmpty();
  }

  @Test
  public void test_create_rejects_an_ownership_it_was_handed() {
    // ownership is Opal's to give: a payload naming an owner project would otherwise take a database out of an
    // operator's hands
    Database database = createSqlDatabase();
    database.setOwnerProject("CLSA");
    try {
      databaseRegistry.create(database);
      fail("Expected an IllegalArgumentException for a payload-supplied owner project");
    } catch(IllegalArgumentException ignored) {
    }
    assertThat(databaseRegistry.list()).isEmpty();
  }

  @Test
  public void test_update_is_refused_while_the_owner_project_exists() {
    Database database = createProjectOwnedDatabase("CLSA");

    database.getSqlSettings().setUsername("someone-else");
    try {
      databaseRegistry.update(database);
      fail("Expected a DatabaseOwnedByProjectException");
    } catch(DatabaseOwnedByProjectException e) {
      assertThat(e.getProject()).isEqualTo("CLSA");
    }
    assertThat(databaseRegistry.getDatabase("_project_CLSA").getSqlSettings().getUsername()).isEqualTo("opal");
  }

  @Test
  public void test_update_of_a_leftover_keeps_its_owner() {
    // the row an archiving deletion left behind re-attaches when a project of that name is created again, so editing
    // it in the meantime must not silently take its owner away
    Database database = createProjectOwnedDatabase("CLSA");
    projectRepository.deleteAll();

    database.setOwnerProject(null);
    database.getSqlSettings().setUsername("someone-else");
    databaseRegistry.update(database);

    assertThat(databaseRegistry.getDatabase("_project_CLSA").getOwnerProject()).isEqualTo("CLSA");
  }

  @Test
  public void test_delete_is_refused_while_the_owner_project_exists() {
    Database database = createProjectOwnedDatabase("CLSA");
    File folder = projectFolder("CLSA");

    try {
      databaseRegistry.delete(database);
      fail("Expected a DatabaseOwnedByProjectException");
    } catch(DatabaseOwnedByProjectException e) {
      assertThat(e.getProject()).isEqualTo("CLSA");
    }
    assertThat(databaseRegistry.list()).hasSize(1);
    assertThat(folder.isDirectory()).isTrue();
  }

  /**
   * A project that failed to load still exists: what makes a leftover an operator's to remove is the project being
   * gone, not its datasource being absent.
   */
  @Test
  public void test_delete_of_a_leftover_takes_its_files_with_it() {
    Database database = createProjectOwnedDatabase("CLSA");
    File folder = projectFolder("CLSA");
    projectRepository.deleteAll();

    databaseRegistry.delete(database);

    assertThat(databaseRegistry.list()).isEmpty();
    assertThat(folder.exists()).isFalse();
    assertThat(pendingDeletionCount()).isEqualTo(0);
  }

  @Test
  public void test_delete_project_owned_takes_row_and_files_whatever_the_project_is_doing() {
    Database database = createProjectOwnedDatabase("CLSA");
    File folder = projectFolder("CLSA");

    databaseRegistry.deleteProjectOwned(database);

    assertThat(databaseRegistry.list()).isEmpty();
    assertThat(folder.exists()).isFalse();
    assertThat(pendingDeletionCount()).isEqualTo(0);
  }

  @Test
  public void test_delete_project_owned_of_a_database_with_no_files_yet() {
    // the folder is only created at the first connection, so a project deleted right after it was created has a row
    // and nothing on disk
    Database database = createProjectOwnedDatabase("Untouched", false);
    assertThat(projectFolder("Untouched").exists()).isFalse();

    databaseRegistry.deleteProjectOwned(database);

    assertThat(databaseRegistry.list()).isEmpty();
  }

  /**
   * A project database is a folder, a registered one a file beside it: the two coexist, so the check that stops two
   * registrations naming one file must not look at project-owned rows - nor try to read a name out of their URL.
   */
  @Test
  public void test_a_project_may_be_named_after_a_registered_database() {
    databaseRegistry.create(createH2Database(Usage.STORAGE, "jdbc:h2:file:opal-data"));

    createProjectOwnedDatabase("opal-data");

    assertThat(databaseRegistry.listSqlDatabases()).hasSize(2);
  }

  @Test
  public void test_a_registered_database_may_be_named_after_a_project() {
    createProjectOwnedDatabase("opal-data");

    databaseRegistry.create(createH2Database(Usage.STORAGE, "jdbc:h2:file:opal-data"));

    assertThat(databaseRegistry.listSqlDatabases()).hasSize(2);
  }

  private Database createProjectOwnedDatabase(String projectName) {
    return createProjectOwnedDatabase(projectName, true);
  }

  private Database createProjectOwnedDatabase(String projectName, boolean withFiles) {
    Project project = new Project(projectName);
    project.setTitle(projectName);
    projectRepository.save(project);

    Database database = Database.Builder.create() //
        .name("_project_" + projectName) //
        .ownerProject(projectName) //
        .usage(Usage.STORAGE) //
        .defaultStorage(false) //
        .usedForIdentifiers(false) //
        .sqlSettings(SqlSettings.Builder.create() //
            .sqlSchema(SqlSettings.SqlSchema.JDBC) //
            .driverClass(H2DatabaseUrls.DRIVER_CLASS) //
            .url(H2DatabaseUrls.projectUrl(projectName)) //
            .username("opal") //
            .password("generated")) //
        .build();
    databaseRegistry.createProjectOwned(database);

    if(!withFiles) return database;

    // what H2 would leave in the folder it creates at the first connection
    File folder = projectFolder(projectName);
    folder.mkdirs();
    try {
      new File(folder, "data.mv.db").createNewFile();
      new File(folder, "data.trace.db").createNewFile();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
    assertThat(new File(folder, "data.mv.db").isFile()).isTrue();
    return database;
  }

  private File projectFolder(String projectName) {
    return H2DatabaseUrls.projectFolder(projectName, h2Root);
  }

  private int pendingDeletionCount() {
    File[] folders = h2Root.listFiles(H2ProjectFolders::isPendingDeletion);
    return folders == null ? 0 : folders.length;
  }

  private Database createH2Database(Usage usage, String url) {
    return createDatabase().usage(usage).defaultStorage(false).sqlSettings(SqlSettings.Builder.create() //
        .sqlSchema(SqlSettings.SqlSchema.JDBC) //
        .driverClass("org.h2.Driver") //
        .url(url) //
        .username("sa") //
        .password("password")) //
        .build();
  }

  private Database createSqlDatabase() {
    return createDatabase().sqlSettings(SqlSettings.Builder.create() //
        .sqlSchema(SqlSettings.SqlSchema.HIBERNATE) //
        .driverClass("mysql") //
        .url("jdbc") //
        .username("root") //
        .password("password") //
        .properties("props")) //
        .build();
  }

  private Database createMongoDatabase() {
    return createDatabase().mongoDbSettings(MongoDbSettings.Builder.create() //
        .url("mongodb") //
        .username("admin") //
        .password("password") //
        .properties("props")) //
        .build();
  }

  private Database.Builder createDatabase() {
    return Database.Builder.create() //
        .name("sql database") //
        .usedForIdentifiers(false) //
        .defaultStorage(true) //
        .usage(Usage.IMPORT);
  }

  private void assertDatabaseEquals(Database expected, Database found) {
    assertThat(found).isNotNull();
    assertThat(expected).isEqualTo(found);
    assertThat(expected.getName()).isEqualTo(found.getName());
    assertThat(expected.getUsage()).isEqualTo(found.getUsage());
    assertThat(expected.isDefaultStorage()).isEqualTo(found.isDefaultStorage());
    assertThat(expected.isUsedForIdentifiers()).isEqualTo(found.isUsedForIdentifiers());
    if(expected.hasSqlSettings()) {
      assertSqlSettingsEquals(expected.getSqlSettings(), found.getSqlSettings());
    }
    if(expected.hasMongoDbSettings()) {
      assertMongoDbSettingsEquals(expected.getMongoDbSettings(), found.getMongoDbSettings());
    }

    Asserts.assertCreatedTimestamps(expected, found);
  }

  private void assertSqlSettingsEquals(SqlSettings expected, SqlSettings found) {
    assertThat(expected).isNotNull();
    assertThat(found).isNotNull();
    assertThat(expected.getSqlSchema()).isEqualTo(found.getSqlSchema());
    assertThat(expected.getDriverClass()).isEqualTo(found.getDriverClass());
    assertThat(expected.getUrl()).isEqualTo(found.getUrl());
    assertThat(expected.getUsername()).isEqualTo(found.getUsername());
    assertThat(expected.getPassword()).isEqualTo(found.getPassword());
    assertThat(expected.getProperties()).isEqualTo(found.getProperties());

    //TODO
//    expected.getJdbcDatasourceSettings()
  }

  private void assertMongoDbSettingsEquals(MongoDbSettings expected, MongoDbSettings found) {
    assertThat(expected).isNotNull();
    assertThat(found).isNotNull();
    assertThat(expected.getUrl()).isEqualTo(found.getUrl());
    assertThat(expected.getUsername()).isEqualTo(found.getUsername());
    assertThat(expected.getPassword()).isEqualTo(found.getPassword());
    assertThat(expected.getProperties()).isEqualTo(found.getProperties());
  }

  @Configuration
  @PropertySource("classpath:/META-INF/defaults.properties")
  public static class Config extends AbstractConfigDbTestConfig {

    @Bean
    public DataSourceFactory dataSourceFactory() {
      return EasyMock.createMock(DataSourceFactory.class);
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
      return EasyMock.createMock(TransactionTemplate.class);
    }

    @Bean
    public DatabaseRegistry databaseRegistry() {
      return new DefaultDatabaseRegistry();
    }

    @Bean
    public SocketFactoryProvider socketFactoryProvider() {
      return () -> SSLSocketFactory.getDefault();
    }

    @Bean
    public IdentifiersTableService identifiersTableService() {
      // nice and replayed rather than left recording: the tests that delete a database all reach getDatasourceName(),
      // and a recording mock only tolerates the first of them
      IdentifiersTableService mock = EasyMock.createNiceMock(IdentifiersTableService.class);
      EasyMock.expect(mock.getDatasourceName()).andReturn("opal-identifiers").anyTimes();
      EasyMock.replay(mock);
      return mock;
    }

    @Bean
    public EventBus eventBus() {
      return new EventBus();
    }

  }
}
