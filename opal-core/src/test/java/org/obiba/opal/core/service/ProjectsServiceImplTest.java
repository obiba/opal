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
import org.easymock.EasyMock;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.SocketFactoryProvider;
import org.obiba.magma.ValueTable;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.repository.DatabaseRepository;
import org.obiba.opal.core.repository.ProjectRepository;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.runtime.jdbc.H2DatabaseUrls;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.fs.impl.DefaultOpalFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.obiba.opal.core.service.ProjectService.ProjectStorage;

@ContextConfiguration(classes = ProjectsServiceImplTest.Config.class)
public class ProjectsServiceImplTest extends AbstractConfigDbTest {

  @Autowired
  private ProjectService projectService;

  @Autowired
  private ProjectDatabaseService projectDatabaseService;

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private DatabaseRepository databaseRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private DatasourceLoaderService datasourceLoaderService;

  @Value("${OPAL_HOME}/data/h2")
  private File h2Root;

  @Override
  public void startDB() throws Exception {
    super.startDB();
    databaseRegistry.stop();
    databaseRepository.deleteAll();
    projectRepository.deleteAll();
    cleanH2Root();
    reset(datasourceLoaderService);
    datasourceLoaderService.registerDatasource(anyObject(Project.class));
    expectLastCall().anyTimes();
    replay(datasourceLoaderService);
  }

  @Override
  public void stopDB() {
    super.stopDB();
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_create_with_internal_storage_gives_the_project_a_database_of_its_own() {
    projectService.save(project("CLSA"), ProjectStorage.internal());

    Project stored = projectService.getProject("CLSA");
    assertThat(stored.getDatabase()).isEqualTo("_project_CLSA");
    assertThat(projectDatabaseService.isInternal(stored)).isTrue();

    Database database = projectDatabaseService.getInternalDatabase("CLSA").get();
    assertThat(database.getOwnerProject()).isEqualTo("CLSA");
    assertThat(database.getSqlSettings().getUrl()).isEqualTo("jdbc:h2:file:CLSA/data");
  }

  @Test
  public void test_create_with_a_registered_database_or_with_none() {
    databaseRegistry.create(registeredDatabase("opal-data"));

    projectService.save(project("Registered"), ProjectStorage.registered("opal-data"));
    projectService.save(project("Nothing"), ProjectStorage.none());

    assertThat(projectService.getProject("Registered").getDatabase()).isEqualTo("opal-data");
    assertThat(projectService.getProject("Nothing").hasDatabase()).isFalse();
    assertThat(databaseRepository.findByOwnerProject("Registered").isPresent()).isFalse();
  }

  /**
   * Saving a project for any other reason - a table was added, an identifiers mapping was removed - must not touch
   * its storage.
   */
  @Test
  public void test_saving_a_project_again_leaves_its_storage_alone() {
    projectService.save(project("CLSA"), ProjectStorage.internal());
    String password = projectDatabaseService.getInternalDatabase("CLSA").get().getSqlSettings().getPassword();

    Project stored = projectService.getProject("CLSA");
    stored.setTitle("A new title");
    projectService.save(stored);

    assertThat(projectService.getProject("CLSA").getDatabase()).isEqualTo("_project_CLSA");
    // the same database, not a second one made because the first went unrecognised
    assertThat(projectDatabaseService.getInternalDatabase("CLSA").get().getSqlSettings().getPassword())
        .isEqualTo(password);
  }

  @Test
  public void test_delete_without_archive_takes_the_database_and_its_files() throws Exception {
    projectService.save(project("CLSA"), ProjectStorage.internal());
    File folder = givenTheDatabaseFilesExist("CLSA");

    projectService.delete("CLSA", false);

    assertThat(projectRepository.findByName("CLSA").isPresent()).isFalse();
    assertThat(projectDatabaseService.getInternalDatabase("CLSA").isPresent()).isFalse();
    assertThat(folder.exists()).isFalse();
  }

  /**
   * Archiving keeps the data. The row and the folder stay, referred to by nobody, and a project created again under
   * the same name takes them back - which is what recreating a project against a registered database does today.
   */
  @Test
  public void test_delete_with_archive_keeps_the_database_and_a_new_project_takes_it_back() throws Exception {
    projectService.save(project("CLSA"), ProjectStorage.internal());
    File folder = givenTheDatabaseFilesExist("CLSA");
    String password = projectDatabaseService.getInternalDatabase("CLSA").get().getSqlSettings().getPassword();

    projectService.delete("CLSA", true);

    assertThat(projectRepository.findByName("CLSA").isPresent()).isFalse();
    assertThat(projectDatabaseService.getInternalDatabase("CLSA").isPresent()).isTrue();
    assertThat(folder.isDirectory()).isTrue();

    projectService.save(project("CLSA"), ProjectStorage.internal());

    assertThat(projectService.getProject("CLSA").getDatabase()).isEqualTo("_project_CLSA");
    assertThat(projectDatabaseService.getInternalDatabase("CLSA").get().getSqlSettings().getPassword())
        .isEqualTo(password);
    assertThat(databaseRegistry.list()).hasSize(1);
  }

  @Test
  public void test_a_failed_creation_leaves_no_database_behind() {
    reset(datasourceLoaderService);
    datasourceLoaderService.registerDatasource(anyObject(Project.class));
    expectLastCall().andThrow(new IllegalStateException("datasource registration failed")).once();
    replay(datasourceLoaderService);

    try {
      projectService.save(project("CLSA"), ProjectStorage.internal());
      fail("Expected the registration failure to be reported");
    } catch(IllegalStateException ignored) {
    }

    assertThat(projectRepository.findByName("CLSA").isPresent()).isFalse();
    assertThat(projectDatabaseService.getInternalDatabase("CLSA").isPresent()).isFalse();
    assertThat(new File(h2Root, "CLSA").exists()).isFalse();
  }

  /**
   * A failed save of a project that already owned a database must not take that database with it: it was not created
   * here, and it holds the project's data.
   */
  @Test
  public void test_a_failed_save_keeps_a_database_it_did_not_create() {
    projectService.save(project("CLSA"), ProjectStorage.internal());

    reset(datasourceLoaderService);
    datasourceLoaderService.registerDatasource(anyObject(Project.class));
    expectLastCall().andThrow(new IllegalStateException("datasource registration failed")).anyTimes();
    replay(datasourceLoaderService);

    Project stored = projectService.getProject("CLSA");
    stored.setDatabase(null);
    try {
      projectService.save(stored, ProjectStorage.internal());
    } catch(IllegalStateException ignored) {
    }

    assertThat(projectDatabaseService.getInternalDatabase("CLSA").isPresent()).isTrue();
  }

  @Test
  public void test_changing_storage_is_refused_while_the_project_holds_data() {
    databaseRegistry.create(registeredDatabase("opal-data"));
    projectService.save(project("CLSA"), ProjectStorage.internal());
    givenTheDatasourceHasATable("CLSA");

    Project stored = projectService.getProject("CLSA");
    try {
      projectService.save(stored, ProjectStorage.registered("opal-data"));
      fail("Expected an InvalidProjectStorageException");
    } catch(InvalidProjectStorageException e) {
      assertThat(e.getProject()).isEqualTo("CLSA");
    }

    assertThat(projectService.getProject("CLSA").getDatabase()).isEqualTo("_project_CLSA");
    assertThat(projectDatabaseService.getInternalDatabase("CLSA").isPresent()).isTrue();
  }

  @Test
  public void test_a_project_cannot_be_pointed_at_another_project_database() {
    projectService.save(project("CLSA"), ProjectStorage.internal());

    try {
      projectService.save(project("Other"), ProjectStorage.registered("_project_CLSA"));
      fail("Expected an InvalidProjectStorageException");
    } catch(InvalidProjectStorageException e) {
      assertThat(e.getProject()).isEqualTo("Other");
      assertThat(e.getMessage()).contains("belongs to project 'CLSA'");
    }

    assertThat(projectRepository.findByName("Other").isPresent()).isFalse();
  }

  @Test
  public void test_leaving_internal_storage_deletes_the_database_it_owned() throws Exception {
    databaseRegistry.create(registeredDatabase("opal-data"));
    projectService.save(project("CLSA"), ProjectStorage.internal());
    File folder = givenTheDatabaseFilesExist("CLSA");

    projectService.save(projectService.getProject("CLSA"), ProjectStorage.registered("opal-data"));

    assertThat(projectService.getProject("CLSA").getDatabase()).isEqualTo("opal-data");
    assertThat(projectDatabaseService.getInternalDatabase("CLSA").isPresent()).isFalse();
    assertThat(folder.exists()).isFalse();
  }

  @Test
  public void test_entering_internal_storage_creates_a_database() {
    databaseRegistry.create(registeredDatabase("opal-data"));
    projectService.save(project("CLSA"), ProjectStorage.registered("opal-data"));

    projectService.save(projectService.getProject("CLSA"), ProjectStorage.internal());

    assertThat(projectService.getProject("CLSA").getDatabase()).isEqualTo("_project_CLSA");
    assertThat(projectDatabaseService.getInternalDatabase("CLSA").isPresent()).isTrue();
    // an operator's database is left exactly as it is
    assertThat(databaseRegistry.hasDatabase("opal-data")).isTrue();
  }

  /**
   * {@code _project_} became Opal's in 6.0, so an upgraded server can hold a project stored in a database an operator
   * registered under that name. It is theirs: saving the project must not try to make Opal's own database of that
   * name, and the project must not be reported as owning one.
   */
  @Test
  public void test_a_project_stored_in_a_database_registered_before_the_prefix_was_reserved() {
    databaseRepository.upsert(registeredDatabase("_project_CLSA"));
    Project project = project("CLSA");
    project.setDatabase("_project_CLSA");
    projectRepository.upsert(project);

    Project stored = projectService.getProject("CLSA");
    stored.setTitle("CLSA, again");
    projectService.save(stored);

    assertThat(projectService.getProject("CLSA").getDatabase()).isEqualTo("_project_CLSA");
    assertThat(projectService.getStorage(projectService.getProject("CLSA")).kind())
        .isEqualTo(ProjectStorage.Kind.REGISTERED);
    // Opal never took ownership of it, so it is not Opal's to delete with the project
    assertThat(projectDatabaseService.getInternalDatabase("CLSA").isPresent()).isFalse();
  }

  //
  // Private methods
  //

  private Project project(String name) {
    Project project = new Project(name);
    project.setTitle(name);
    return project;
  }

  private Database registeredDatabase(String name) {
    return Database.Builder.create() //
        .name(name) //
        .usage(Database.Usage.STORAGE) //
        .defaultStorage(false) //
        .usedForIdentifiers(false) //
        .sqlSettings(SqlSettings.Builder.create() //
            .sqlSchema(SqlSettings.SqlSchema.JDBC) //
            .driverClass("org.postgresql.Driver") //
            .url("jdbc:postgresql://localhost:5432/" + name) //
            .username("opal") //
            .password("password")) //
        .build();
  }

  /**
   * What H2 leaves in the folder it creates at the first connection.
   */
  private File givenTheDatabaseFilesExist(String projectName) throws IOException {
    File folder = H2DatabaseUrls.projectFolder(projectName, h2Root);
    folder.mkdirs();
    new File(folder, "data.mv.db").createNewFile();
    return folder;
  }

  private void givenTheDatasourceHasATable(String projectName) {
    Datasource datasource = EasyMock.createNiceMock(Datasource.class);
    ValueTable table = EasyMock.createNiceMock(ValueTable.class);
    EasyMock.expect(datasource.getName()).andReturn(projectName).anyTimes();
    EasyMock.expect(datasource.getValueTables()).andReturn(Collections.singleton(table)).anyTimes();
    EasyMock.replay(datasource, table);
    MagmaEngine.get().addDatasource(datasource);
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
    public ProjectService projectService(OpalFileSystemService opalFileSystemService,
                                         ProjectRepository projectRepository, DatabaseRegistry databaseRegistry,
                                         ProjectDatabaseService projectDatabaseService,
                                         ProjectsKeyStoreService projectsKeyStoreService,
                                         IdentifiersTableService identifiersTableService, ViewManager viewManager,
                                         TransactionTemplate transactionTemplate, EventBus eventBus,
                                         ResourceReferenceService resourceReferenceService, ProjectsState projectsState,
                                         DatasourceLoaderService datasourceLoaderService) {
      return new ProjectsServiceImpl(opalFileSystemService, projectRepository, databaseRegistry, projectDatabaseService,
          projectsKeyStoreService, identifiersTableService, viewManager, transactionTemplate, eventBus,
          resourceReferenceService, projectsState, datasourceLoaderService);
    }

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
      return EasyMock.createNiceMock(DataSourceFactory.class);
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager configTransactionManager) {
      return new TransactionTemplate(configTransactionManager);
    }

    @Bean
    public OpalFileSystemService opalFileSystemService() throws IOException {
      File root = File.createTempFile("opal-projects-test-", "");
      root.delete();
      root.mkdirs();
      root.deleteOnExit();
      OpalFileSystem fileSystem = new DefaultOpalFileSystem(root.getAbsolutePath());
      return new OpalFileSystemService() {
        @Override
        public boolean hasFileSystem() {
          return true;
        }

        @Override
        public OpalFileSystem getFileSystem() {
          return fileSystem;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }
      };
    }

    @Bean
    public ViewManager viewManager() {
      return EasyMock.createNiceMock(ViewManager.class);
    }

    @Bean
    public ProjectsKeyStoreService projectsKeyStoreService() {
      return EasyMock.createNiceMock(ProjectsKeyStoreService.class);
    }

    @Bean
    public ResourceReferenceService resourceReferenceService() {
      return EasyMock.createNiceMock(ResourceReferenceService.class);
    }

    @Bean
    public ProjectsState projectsState() {
      return new ProjectsState();
    }

    @Bean
    public DatasourceLoaderService datasourceLoaderService() {
      return EasyMock.createMock(DatasourceLoaderService.class);
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
