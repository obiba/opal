/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.persistence;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.service.security.CryptoService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

public class ConfigDatabaseConfigurationTest {

  private static final String PASSWORD = "s3cr3t-generated-password";

  private File opalHome;

  @Before
  public void createOpalHome() throws Exception {
    opalHome = Files.createTempDirectory("opal-config-db-test").toFile();
  }

  @After
  public void deleteOpalHome() throws Exception {
    FileUtil.delete(opalHome);
  }

  @Test
  public void test_embedded_database_is_created_with_the_generated_password() {
    try(AnnotationConfigApplicationContext context = open(PASSWORD)) {
      assertThat(context.getBean(DataSource.class)).isNotNull();
      assertThat(context.getBean(EntityManagerFactory.class)).isNotNull();
    }

    // The configuration database has its own folder, so that a database registered by a user cannot collide with it.
    assertThat(new File(opalHome, "data/config/opal-config.mv.db")).exists();
    assertThat(new File(opalHome, "data/h2")).doesNotExist();
  }

  @Test
  public void test_liquibase_runs_against_the_configuration_database() throws Exception {
    try(AnnotationConfigApplicationContext context = open(PASSWORD)) {
      try(Connection connection = context.getBean(DataSource.class).getConnection();
          Statement statement = connection.createStatement();
          ResultSet rs = statement.executeQuery("select count(*) from opal_changelog")) {
        // The changelog is still empty, but its bookkeeping table proves Liquibase owns this schema.
        assertThat(rs.next()).isTrue();
      }
    }
  }

  @Test
  public void test_transaction_manager_is_not_the_application_one() {
    try(AnnotationConfigApplicationContext context = open(PASSWORD)) {
      PlatformTransactionManager transactionManager =
          context.getBean("configTransactionManager", PlatformTransactionManager.class);
      assertThat(transactionManager).isInstanceOf(JpaTransactionManager.class);
      // Configuration writes must not enrol in the Atomikos transactions of the Magma datasources.
      assertThat(context.containsBean("transactionManager")).isFalse();
    }
  }

  @Test
  public void test_hibernate_does_not_bind_itself_to_atomikos() {
    // Atomikos is on the classpath, and Hibernate adopts it as its JTA platform unless told otherwise.
    try(AnnotationConfigApplicationContext context = open(PASSWORD)) {
      Object platform = context.getBean(EntityManagerFactory.class).getProperties()
          .get("hibernate.transaction.jta.platform");
      assertThat(platform).isEqualTo(NoJtaPlatform.class.getName());
    }
  }

  @Test
  public void test_wrong_password_explains_which_files_disagree() {
    try(AnnotationConfigApplicationContext context = open(PASSWORD)) {
      assertThat(context.getBean(DataSource.class)).isNotNull();
    }

    // Same database folder, a different generated password: what an administrator gets by restoring data/config and
    // data/opal-config.xml from different installations.
    try(AnnotationConfigApplicationContext ignored = open("a-different-password")) {
      fail("Expected the configuration database to refuse a password it was not created with");
    } catch(Exception e) {
      ConfigDatabaseException cause = findConfigDatabaseException(e);
      assertThat(cause).isNotNull();
      assertThat(cause.getMessage()).contains("the password does not match");
      assertThat(cause.getMessage()).contains("opal-config.xml");
      assertThat(cause.getMessage()).contains("<secretKey>");
    }
  }

  private ConfigDatabaseException findConfigDatabaseException(Throwable throwable) {
    for(Throwable t = throwable; t != null; t = t.getCause()) {
      if(t instanceof ConfigDatabaseException) return (ConfigDatabaseException) t;
    }
    return null;
  }

  private AnnotationConfigApplicationContext open(String password) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    Map<String, Object> properties = new HashMap<>();
    properties.put("OPAL_HOME", opalHome.getAbsolutePath());
    context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
    context.registerBean(PropertySourcesPlaceholderConfigurer.class);
    context.registerBean(TestOpalConfigurationService.class, () -> new TestOpalConfigurationService(password));
    context.register(ConfigDatabaseConfiguration.class);
    context.refresh();
    return context;
  }

  /**
   * Stands in for {@code DefaultOpalConfigurationService}, which is both the holder of the generated database password
   * and the service that decrypts it.
   */
  private static class TestOpalConfigurationService implements OpalConfigurationService, CryptoService {

    private final OpalConfiguration configuration = new OpalConfiguration();

    private TestOpalConfigurationService(String password) {
      configuration.setSecretKey("testsecretkey1234");
      configuration.setDatabasePassword(encrypt(password));
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void readOpalConfiguration() {
    }

    @Override
    public OpalConfiguration getOpalConfiguration() {
      return configuration;
    }

    @Override
    public void modifyConfiguration(ConfigModificationTask task) {
      task.doWithConfig(configuration);
    }

    @Override
    public String generateSecretKey() {
      return "testsecretkey1234";
    }

    @Override
    public String encrypt(String plain) {
      return "enc:" + plain;
    }

    @Override
    public String decrypt(String encrypted) {
      return encrypted.substring("enc:".length());
    }

    @Override
    public InputStream newCipherInputStream(InputStream in) {
      return in;
    }

    @Override
    public OutputStream newCipherOutputStream(OutputStream out) {
      return out;
    }
  }
}
