/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.google.common.eventbus.EventBus;
import org.apache.commons.dbcp2.BasicDataSource;
import org.easymock.EasyMock;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.SocketFactoryProvider;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.repository.DatabaseRepository;
import org.obiba.opal.core.repository.ProjectRepository;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.runtime.jdbc.H2DatabaseUrls;
import org.obiba.opal.core.runtime.jdbc.H2ProjectFolders;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import javax.net.ssl.SSLSocketFactory;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.Collection;

import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

@ContextConfiguration(classes = ProjectDatabaseServiceTest.Config.class)
public class ProjectDatabaseServiceTest extends AbstractConfigDbTest {

  @Autowired
  private ProjectDatabaseService projectDatabaseService;

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private DatabaseRepository databaseRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private DataSourceFactory dataSourceFactory;

  @Value("${OPAL_HOME}/data/h2")
  private File h2Root;

  @Override
  public void startDB() throws Exception {
    super.startDB();
    databaseRegistry.stop();
    databaseRepository.deleteAll();
    projectRepository.deleteAll();
    cleanH2Root();
  }

  @Override
  public void stopDB() {
    super.stopDB();
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_the_database_a_project_gets() {
    Database database = projectDatabaseService.ensureInternalDatabase("CLSA");

    assertThat(database.getName()).isEqualTo("_project_CLSA");
    assertThat(database.getOwnerProject()).isEqualTo("CLSA");
    assertThat(database.isProjectOwned()).isTrue();
    assertThat(database.getUsage()).isEqualTo(Database.Usage.STORAGE);
    assertThat(database.isDefaultStorage()).isFalse();
    assertThat(database.isUsedForIdentifiers()).isFalse();

    SqlSettings sqlSettings = database.getSqlSettings();
    assertThat(sqlSettings.getSqlSchema()).isEqualTo(SqlSettings.SqlSchema.JDBC);
    assertThat(sqlSettings.getDriverClass()).isEqualTo(H2DatabaseUrls.DRIVER_CLASS);
    assertThat(sqlSettings.getUrl()).isEqualTo("jdbc:h2:file:CLSA/data");
    assertThat(sqlSettings.getUsername()).isEqualTo("opal");
    assertThat(sqlSettings.getPassword()).hasSize(32);

    // the settings the administration UI sends when an operator registers a SQL database
    assertThat(sqlSettings.getJdbcDatasourceSettings().getDefaultEntityType()).isEqualTo("Participant");
    assertThat(sqlSettings.getJdbcDatasourceSettings().getDefaultEntityIdColumnName()).isEqualTo("opal_id");
    assertThat(sqlSettings.getJdbcDatasourceSettings().getDefaultCreatedTimestampColumnName())
        .isEqualTo("opal_created");
    assertThat(sqlSettings.getJdbcDatasourceSettings().getDefaultUpdatedTimestampColumnName())
        .isEqualTo("opal_updated");
    assertThat(sqlSettings.getJdbcDatasourceSettings().isUseMetadataTables()).isTrue();
    assertThat(sqlSettings.getJdbcDatasourceSettings().isMultipleDatasources()).isTrue();
    assertThat(sqlSettings.getJdbcDatasourceSettings().getBatchSize()).isEqualTo(100);

    assertThat(databaseRegistry.getDatabase("_project_CLSA").getOwnerProject()).isEqualTo("CLSA");
  }

  @Test
  public void test_two_passwords_are_not_the_same() {
    String one = projectDatabaseService.ensureInternalDatabase("One").getSqlSettings().getPassword();
    String other = projectDatabaseService.ensureInternalDatabase("Other").getSqlSettings().getPassword();
    assertThat(one).isNotEqualTo(other);
  }

  /**
   * A project recreated after an archiving deletion gets its data back rather than a second database.
   */
  @Test
  public void test_ensure_is_idempotent() {
    Database first = projectDatabaseService.ensureInternalDatabase("CLSA");
    Database again = projectDatabaseService.ensureInternalDatabase("CLSA");

    assertThat(again.getName()).isEqualTo(first.getName());
    assertThat(again.getSqlSettings().getPassword()).isEqualTo(first.getSqlSettings().getPassword());
    assertThat(databaseRegistry.list()).hasSize(1);
  }

  @Test
  public void test_get_internal_database() {
    assertThat(projectDatabaseService.getInternalDatabase("CLSA").isPresent()).isFalse();
    projectDatabaseService.ensureInternalDatabase("CLSA");
    assertThat(projectDatabaseService.getInternalDatabase("CLSA").get().getName()).isEqualTo("_project_CLSA");
  }

  @Test
  public void test_is_internal_is_read_from_the_project() {
    projectDatabaseService.ensureInternalDatabase("CLSA");

    assertThat(projectDatabaseService.isInternal(project("CLSA", "_project_CLSA"))).isTrue();
    assertThat(projectDatabaseService.isInternal(project("CLSA", "opal-data"))).isFalse();
    assertThat(projectDatabaseService.isInternal(project("CLSA", null))).isFalse();
  }

  /**
   * The prefix became Opal's in 6.0, so an upgraded server can hold a database an operator registered under it. It is
   * theirs: Opal did not make it, has no owner row for it, and must not delete it with the project or hide its name.
   */
  @Test
  public void test_a_database_registered_before_the_prefix_was_reserved_is_not_internal() {
    // straight to the store: the registry refuses this name now, which is exactly why only an upgrade can hold one
    databaseRepository.upsert(Database.Builder.create() //
        .name("_project_CLSA") //
        .usage(Database.Usage.STORAGE) //
        .sqlSettings(SqlSettings.Builder.create() //
            .sqlSchema(SqlSettings.SqlSchema.JDBC) //
            .driverClass(H2DatabaseUrls.DRIVER_CLASS) //
            .url("jdbc:h2:file:legacy") //
            .username("sa").password("sa")) //
        .build());

    assertThat(projectDatabaseService.isInternal(project("CLSA", "_project_CLSA"))).isFalse();
  }

  /**
   * Two names that differ only in case would be one folder on a case insensitive file system. The refusal stands on
   * every platform - including the case sensitive one this test runs on - because the rule is about the name: a pair
   * of projects that worked on Linux and broke when OPAL_HOME was copied to a Mac would be the worse failure.
   */
  @Test
  public void test_a_name_colliding_in_case_with_an_owner_is_refused() {
    projectDatabaseService.ensureInternalDatabase("Foo");

    try {
      projectDatabaseService.ensureInternalDatabase("foo");
      fail("Expected a ConflictingProjectDatabaseException");
    } catch(ConflictingProjectDatabaseException e) {
      assertThat(e.getProject()).isEqualTo("foo");
      assertThat(e.getConflictingProject()).isEqualTo("Foo");
      assertThat(e.getMessage()).contains("differ only in case");
    }
    assertThat(databaseRegistry.list()).hasSize(1);
  }

  @Test
  public void test_a_name_colliding_in_case_with_a_folder_is_refused() {
    // a folder somebody put there by hand, with no database row of its own
    assertThat(new File(h2Root, "Foo").mkdirs()).isTrue();

    try {
      projectDatabaseService.ensureInternalDatabase("foo");
      fail("Expected a ConflictingProjectDatabaseException");
    } catch(ConflictingProjectDatabaseException e) {
      assertThat(e.getConflictingProject()).isEqualTo("Foo");
    }
    assertThat(databaseRegistry.list()).isEmpty();
  }

  @Test
  public void test_a_folder_of_the_same_name_is_the_project_own_data() {
    // the leftovers of an archiving deletion, whose row is gone: the project takes its data back
    assertThat(new File(h2Root, "CLSA").mkdirs()).isTrue();

    assertThat(projectDatabaseService.ensureInternalDatabase("CLSA").getName()).isEqualTo("_project_CLSA");
  }

  /**
   * Closing the last connection is what closes the H2 store and releases the file, so it has to happen before the
   * files are removed and not after.
   */
  @Test
  public void test_delete_closes_the_database_before_it_deletes_the_files() throws Exception {
    projectDatabaseService.ensureInternalDatabase("CLSA");
    BasicDataSource dataSource = openTheProjectDatabase("CLSA");
    File folder = new File(h2Root, "CLSA");
    assertThat(new File(folder, "data.mv.db").isFile()).isTrue();

    projectDatabaseService.deleteInternalDatabase("CLSA");

    assertThat(dataSource.isClosed()).isTrue();
    assertThat(folder.exists()).isFalse();
    assertThat(pendingDeletionFolders()).isEmpty();
    assertThat(databaseRegistry.list()).isEmpty();
    assertThat(projectDatabaseService.getInternalDatabase("CLSA").isPresent()).isFalse();
  }

  @Test
  public void test_delete_of_a_project_that_owns_nothing_is_silent() {
    projectDatabaseService.deleteInternalDatabase("Nothing");
  }

  @Test
  public void test_list_orphan_folders_tells_the_three_kinds_apart() throws IOException {
    // a live project database
    projectDatabaseService.ensureInternalDatabase("Live");
    assertThat(new File(h2Root, "Live").mkdirs()).isTrue();
    // the leftovers of an archiving deletion: the row is still there, so the folder is not an orphan
    projectDatabaseService.ensureInternalDatabase("Archived");
    assertThat(new File(h2Root, "Archived").mkdirs()).isTrue();
    projectRepository.deleteAll();
    // a folder no row points at
    assertThat(new File(h2Root, "Orphan").mkdirs()).isTrue();
    // and a removal that stopped half way
    assertThat(new File(h2Root, "Gone" + H2ProjectFolders.DELETED_MARKER + "1757376000000").mkdirs()).isTrue();
    // a registered H2 database is a file, not a folder
    assertThat(new File(h2Root, "opal-data.mv.db").createNewFile()).isTrue();

    Collection<File> orphans = projectDatabaseService.listOrphanFolders();

    assertThat(orphans).hasSize(1);
    assertThat(orphans.iterator().next().getName()).isEqualTo("Orphan");
  }

  @Test
  public void test_delete_pending_folders_finishes_what_a_previous_run_began() throws IOException {
    File pending = new File(h2Root, "Gone" + H2ProjectFolders.DELETED_MARKER + "1757376000000");
    assertThat(pending.mkdirs()).isTrue();
    assertThat(new File(pending, "data.mv.db").createNewFile()).isTrue();
    projectDatabaseService.ensureInternalDatabase("Live");
    assertThat(new File(h2Root, "Live").mkdirs()).isTrue();

    projectDatabaseService.deletePendingFolders();

    assertThat(pending.exists()).isFalse();
    assertThat(new File(h2Root, "Live").isDirectory()).isTrue();
  }

  private Project project(String name, String database) {
    Project project = new Project(name);
    project.setTitle(name);
    project.setDatabase(database);
    return project;
  }

  /**
   * Open the database the way the registry would, so that H2 creates the folder and the store in it.
   */
  private BasicDataSource openTheProjectDatabase(String projectName) throws Exception {
    Database database = projectDatabaseService.getInternalDatabase(projectName).get();
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(H2DatabaseUrls.DRIVER_CLASS);
    dataSource.setUrl(H2DatabaseUrls.expandProject(database.getSqlSettings().getUrl(), h2Root));
    dataSource.setUsername(database.getSqlSettings().getUsername());
    dataSource.setPassword(database.getSqlSettings().getPassword());

    reset(dataSourceFactory);
    expect(dataSourceFactory.createDataSource(anyObject(Database.class))).andReturn(dataSource).once();
    replay(dataSourceFactory);

    DataSource opened = databaseRegistry.getDataSource(database.getName(), projectName);
    try(Connection connection = opened.getConnection()) {
      connection.createStatement().execute("create table if not exists probe (id int)");
    }
    return dataSource;
  }

  private File[] pendingDeletionFolders() {
    File[] folders = h2Root.listFiles(H2ProjectFolders::isPendingDeletion);
    return folders == null ? new File[0] : folders;
  }

  private void cleanH2Root() throws IOException {
    File[] files = h2Root.listFiles();
    if(files == null) return;
    for(File file : files) {
      org.obiba.core.util.FileUtil.delete(file);
    }
  }

  @Configuration
  @PropertySource("classpath:/META-INF/defaults.properties")
  public static class Config extends AbstractConfigDbTestConfig {

    @Bean
    public ProjectDatabaseService projectDatabaseService() {
      return new ProjectDatabaseServiceImpl();
    }

    @Bean
    public DatabaseRegistry databaseRegistry() {
      return new DefaultDatabaseRegistry();
    }

    @Bean
    public DataSourceFactory dataSourceFactory() {
      return EasyMock.createMock(DataSourceFactory.class);
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
      return EasyMock.createMock(TransactionTemplate.class);
    }

    @Bean
    public SocketFactoryProvider socketFactoryProvider() {
      return SSLSocketFactory::getDefault;
    }

    @Bean
    public IdentifiersTableService identifiersTableService() {
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
