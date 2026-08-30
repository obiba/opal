/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.upgrade.v6_0_x;

import com.google.common.collect.Sets;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.tx.OTransaction;
import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.converter.DomainGson;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.domain.security.KeyStoreState;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.repository.*;
import org.obiba.opal.r.repository.RSessionActivityRepository;
import org.obiba.opal.r.service.RSessionActivity;
import org.obiba.runtime.Version;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Migrates a real OrientDB database - written here the way Opal wrote it - into a configuration database, and reads
 * the result back through the repositories the application uses.
 */
public class OrientDbToH2UpgradeStepTest {

  private static final String DB_NAME = "opal-config";

  private File opalHome;

  private AnnotationConfigApplicationContext context;

  @Before
  public void createOpalHome() throws Exception {
    opalHome = Files.createTempDirectory("opal-upgrade-test").toFile();
    new File(opalHome, "data/orientdb").mkdirs();
  }

  @After
  public void closeContext() {
    if(context != null) context.close();
  }

  @Test
  public void test_migrates_every_class_it_finds() {
    writeOrientDb(db -> {
      Project project = new Project("cohort");
      project.setTitle("Cohort");
      project.addTag("epidemiology");
      project.setCreated(date(2020, 1, 1));
      save(db, "Project", project);

      SqlSettings sqlSettings = new SqlSettings();
      sqlSettings.setDriverClass("org.h2.Driver");
      sqlSettings.setUrl("jdbc:h2:file:store");
      Database database = new Database();
      database.setName("store");
      database.setUsage(Database.Usage.STORAGE);
      database.setDefaultStorage(true);
      database.setSqlSettings(sqlSettings);
      save(db, "Database", database);

      save(db, "SubjectAcl", new SubjectAcl("opal", "/project/cohort",
          SubjectAcl.SubjectType.USER.subjectFor("alice"), "PROJECT_ALL"));

      SubjectCredentials credentials = new SubjectCredentials("alice");
      credentials.setAuthenticationType(SubjectCredentials.AuthenticationType.PASSWORD);
      credentials.setGroups(Sets.newHashSet("analysts"));
      save(db, "SubjectCredentials", credentials);

      KeyStoreState state = new KeyStoreState("cohort");
      state.setKeyStore(new byte[]{1, 2, 3, 4});
      save(db, "KeyStoreState", state);

      RSessionActivity activity = new RSessionActivity();
      activity.setId("session-1");
      activity.setUser("alice");
      activity.setContext("R");
      activity.setProfile("default");
      activity.setExecutionTimeMillis(1234L);
      save(db, "RSessionActivity", activity);
    });

    run();

    assertThat(context.getBean(ProjectRepository.class).count()).isEqualTo(1);
    Project project = context.getBean(ProjectRepository.class).findByName("cohort").orElse(null);
    assertThat(project).isNotNull();
    assertThat(project.getTitle()).isEqualTo("Cohort");
    assertThat(project.getTags()).containsOnly("epidemiology");
    // A migrated row keeps the date it was created on, rather than the date of the migration.
    assertThat(project.getCreated()).isEqualTo(date(2020, 1, 1));

    Database database = context.getBean(DatabaseRepository.class).findByName("store").orElse(null);
    assertThat(database).isNotNull();
    assertThat(database.getUsage()).isEqualTo(Database.Usage.STORAGE);
    assertThat(database.getSqlSettings().getUrl()).isEqualTo("jdbc:h2:file:store");

    assertThat(context.getBean(SubjectAclRepository.class).count()).isEqualTo(1);
    SubjectAcl acl = context.getBean(SubjectAclRepository.class).findAll().get(0);
    assertThat(acl.getType()).isEqualTo(SubjectAcl.SubjectType.USER);
    assertThat(acl.getNode()).isEqualTo("/project/cohort");

    SubjectCredentials credentials = context.getBean(SubjectCredentialsRepository.class)
        .findByName("alice").orElse(null);
    assertThat(credentials).isNotNull();
    assertThat(credentials.getGroups()).containsOnly("analysts");

    KeyStoreState state = context.getBean(KeyStoreStateRepository.class).findByName("cohort").orElse(null);
    assertThat(state).isNotNull();
    assertThat(state.getKeyStore()).isEqualTo(new byte[]{1, 2, 3, 4});

    RSessionActivity activity = context.getBean(RSessionActivityRepository.class).findById("session-1").orElse(null);
    assertThat(activity).isNotNull();
    assertThat(activity.getUser()).isEqualTo("alice");
    assertThat(activity.getExecutionTimeMillis()).isEqualTo(1234L);
  }

  @Test
  public void test_migrates_more_records_than_one_batch() {
    // The R activity log is the one table that can be large, and the only one whose migration is worth batching.
    writeOrientDb(db -> {
      for(int i = 0; i < 1200; i++) {
        RSessionActivity activity = new RSessionActivity();
        activity.setId("session-" + i);
        activity.setUser("alice");
        activity.setContext("R");
        save(db, "RSessionActivity", activity);
      }
    });

    run();

    assertThat(context.getBean(RSessionActivityRepository.class).count()).isEqualTo(1200);
  }

  @Test
  public void test_running_it_twice_does_not_duplicate() {
    writeOrientDb(db -> {
      save(db, "SubjectAcl", new SubjectAcl("opal", "/project/cohort",
          SubjectAcl.SubjectType.USER.subjectFor("alice"), "PROJECT_ALL"));
      RSessionActivity activity = new RSessionActivity();
      activity.setId("session-1");
      activity.setUser("alice");
      save(db, "RSessionActivity", activity);
    });

    run();
    assertThat(context.getBean(SubjectAclRepository.class).count()).isEqualTo(1);

    // The upgrade manager applies a step whenever the installed version is below the one it applies to, and puts no
    // upper bound on that, so a later upgrade can reach this step a second time.
    context.getBean(OrientDbToH2UpgradeStep.class).execute(new Version("5.7.0"));

    assertThat(context.getBean(SubjectAclRepository.class).count()).isEqualTo(1);
    assertThat(context.getBean(RSessionActivityRepository.class).count()).isEqualTo(1);
  }

  @Test
  public void test_resumes_when_a_previous_run_got_part_way() {
    // What a run that failed half way leaves behind: some of the records written, the rest not. Skipping a class that
    // already holds rows would strand the remainder for good, so every record is written under its natural key and a
    // second run finishes the job.
    writeOrientDb(db -> {
      for(int i = 0; i < 40; i++) {
        RSessionActivity activity = new RSessionActivity();
        activity.setId("session-" + i);
        activity.setUser("alice");
        activity.setContext("R");
        save(db, "RSessionActivity", activity);
      }
    });

    run();
    RSessionActivityRepository repository = context.getBean(RSessionActivityRepository.class);
    assertThat(repository.count()).isEqualTo(40);

    // Throw away three quarters of it, as an interrupted run would have left it.
    repository.findAll().stream().limit(30).forEach(repository::delete);
    assertThat(repository.count()).isEqualTo(10);

    context.getBean(OrientDbToH2UpgradeStep.class).execute(new Version("5.7.0"));

    assertThat(repository.count()).isEqualTo(40);
  }

  @Test
  public void test_no_orientdb_database_is_not_a_failure() {
    // A new installation has nothing to migrate.
    run();

    assertThat(context.getBean(ProjectRepository.class).count()).isEqualTo(0);
  }

  @Test
  public void test_the_orientdb_folder_is_left_in_place() {
    writeOrientDb(db -> save(db, "Project", new Project("cohort")));

    run();

    assertThat(new File(opalHome, "data/orientdb/" + DB_NAME)).exists();
  }

  private void run() {
    context = openContext();
    context.getBean(OrientDbToH2UpgradeStep.class).execute(new Version("5.7.0"));
  }

  private Date date(int year, int month, int day) {
    java.util.Calendar calendar = java.util.Calendar.getInstance();
    calendar.clear();
    calendar.set(year, month - 1, day);
    return calendar.getTime();
  }

  private void save(ODatabaseDocument db, String className, Object entity) {
    // Exactly how Opal wrote it: a document of the class named after the type, holding the Gson rendering of it.
    ODocument document = new ODocument(className);
    document.fromJSON(DomainGson.get().toJson(entity));
    document.save();
  }

  /**
   * The classes Opal declared in OrientDB. They are created before the transaction opens because OrientDB refuses a
   * schema change inside one, which is also why the services created them at startup rather than on first write.
   */
  private static final String[] CONFIG_CLASSES = {
      "Project", "Database", "SubjectAcl", "SubjectCredentials", "SubjectProfile", "SubjectToken", "Group",
      "ResourceReference", "VCFSamplesMapping", "OpalAnalysis", "OpalAnalysisResult", "OpalGeneralConfig",
      "AppsConfig", "App", "PodSpec", "KeyStoreState", "DataShieldProfile", "RSessionActivity"
  };

  private void writeOrientDb(java.util.function.Consumer<ODatabaseDocument> writer) {
    String url = "plocal:" + new File(opalHome, "data/orientdb/" + DB_NAME).getAbsolutePath();
    try(ODatabaseDocument db = new ODatabaseDocumentTx(url)) {
      db.create();
      for(String className : CONFIG_CLASSES) {
        db.getMetadata().getSchema().createClass(className);
      }
      db.begin(OTransaction.TXTYPE.OPTIMISTIC);
      writer.accept(db);
      db.commit();
    }
  }

  private AnnotationConfigApplicationContext openContext() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    Map<String, Object> properties = new HashMap<>();
    properties.put("OPAL_HOME", opalHome.getAbsolutePath());
    context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
    context.registerBean(PropertySourcesPlaceholderConfigurer.class);
    context.register(Config.class);
    context.refresh();
    return context;
  }

  @Configuration
  @EnableJpaRepositories(
      basePackages = {
          "org.obiba.opal.core.repository",
          "org.obiba.opal.datashield.repository",
          "org.obiba.opal.r.repository"
      },
      entityManagerFactoryRef = "configEntityManagerFactory",
      transactionManagerRef = "configTransactionManager")
  public static class Config {

    @Bean
    public DataSource configDataSource() throws Exception {
      SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
      //noinspection unchecked
      dataSource.setDriverClass((Class) Class.forName("org.h2.Driver"));
      dataSource.setUrl("jdbc:h2:mem:opal-upgrade-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
      dataSource.setUsername("sa");
      dataSource.setPassword("");
      return dataSource;
    }

    @Bean
    public SpringLiquibase configLiquibase(DataSource configDataSource) {
      SpringLiquibase liquibase = new SpringLiquibase();
      liquibase.setDataSource(configDataSource);
      liquibase.setChangeLog("classpath:db/changelog/config/db.changelog-master.xml");
      liquibase.setDatabaseChangeLogTable("opal_changelog");
      liquibase.setDatabaseChangeLogLockTable("opal_changelog_lock");
      return liquibase;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean configEntityManagerFactory(DataSource configDataSource,
                                                                            SpringLiquibase configLiquibase) {
      LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
      factoryBean.setDataSource(configDataSource);
      factoryBean.setPersistenceUnitName("opal-config-upgrade-test");
      factoryBean.setPackagesToScan("org.obiba.opal.core.domain", "org.obiba.opal.core.runtime",
          "org.obiba.opal.datashield.cfg", "org.obiba.opal.r.service");
      factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
      Properties properties = new Properties();
      properties.setProperty("hibernate.hbm2ddl.auto", "validate");
      properties.setProperty("hibernate.jdbc.batch_size", "50");
      factoryBean.setJpaProperties(properties);
      return factoryBean;
    }

    @Bean
    public PlatformTransactionManager configTransactionManager(EntityManagerFactory configEntityManagerFactory) {
      return new JpaTransactionManager(configEntityManagerFactory);
    }

    @Bean
    public OrientDbToH2UpgradeStep orientDbToH2UpgradeStep() {
      return new OrientDbToH2UpgradeStep();
    }
  }
}
