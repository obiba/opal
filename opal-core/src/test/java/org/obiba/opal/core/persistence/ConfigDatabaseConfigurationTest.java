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
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;
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
  public void test_closing_the_context_closes_the_database_even_with_a_borrowed_connection() throws Exception {
    Connection borrowed;
    try(AnnotationConfigApplicationContext context = open(PASSWORD)) {
      borrowed = context.getBean(DataSource.class).getConnection();
    }
    // The pool leaves a borrowed connection open, and with it the database: only the SHUTDOWN closes it.
    assertThat(borrowed.isClosed()).isTrue();
  }

  @Test
  public void test_store_reopens_after_closing_with_a_borrowed_connection_that_wrote() throws Exception {
    try(AnnotationConfigApplicationContext context = open(PASSWORD)) {
      Connection borrowed = context.getBean(DataSource.class).getConnection();
      try(Statement statement = borrowed.createStatement()) {
        statement.execute("create table shutdown_check(x int)");
        // Enough rows to leave the background writer something to flush when the context closes.
        statement.execute("insert into shutdown_check select x from system_range(1, 100000)");
        borrowed.commit();
        // Still in flight when the context closes: the shutdown must roll it back, not half-write it.
        statement.execute("insert into shutdown_check select x from system_range(1, 50000)");
      }
    }
    // A store left half-written by the close would fail to open here, or come back with the wrong count.
    try(AnnotationConfigApplicationContext context = open(PASSWORD);
        Connection connection = context.getBean(DataSource.class).getConnection();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("select count(*) from shutdown_check")) {
      assertThat(rs.next()).isTrue();
      assertThat(rs.getInt(1)).isEqualTo(100000);
    }
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

  @Test
  public void test_undecryptable_password_is_regenerated_until_the_database_exists() {
    // What an Opal 5 installation may carry: a <databasePassword> that was never read, under a replaced secret key.
    try(AnnotationConfigApplicationContext context = openEncrypted("not-decryptable")) {
      assertThat(context.getBean(DataSource.class)).isNotNull();
      String regenerated = context.getBean(TestOpalConfigurationService.class).getOpalConfiguration().getDatabasePassword();
      assertThat(regenerated).startsWith("enc:");
    }

    try(AnnotationConfigApplicationContext ignored = openEncrypted("not-decryptable")) {
      fail("Expected an undecryptable password to be refused once the database exists");
    } catch(Exception e) {
      ConfigDatabaseException cause = findConfigDatabaseException(e);
      assertThat(cause).isNotNull();
      assertThat(cause.getMessage()).contains("Cannot decrypt the <databasePassword>");
    }
  }

  private ConfigDatabaseException findConfigDatabaseException(Throwable throwable) {
    for(Throwable t = throwable; t != null; t = t.getCause()) {
      if(t instanceof ConfigDatabaseException) return (ConfigDatabaseException) t;
    }
    return null;
  }

  private AnnotationConfigApplicationContext open(String password) {
    return openEncrypted("enc:" + password);
  }

  private AnnotationConfigApplicationContext openEncrypted(String encryptedPassword) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    Map<String, Object> properties = new HashMap<>();
    properties.put("OPAL_HOME", opalHome.getAbsolutePath());
    context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
    context.registerBean(PropertySourcesPlaceholderConfigurer.class);
    context.registerBean(TestOpalConfigurationService.class, () -> new TestOpalConfigurationService(encryptedPassword));
    context.register(ConfigDatabaseConfiguration.class);
    context.refresh();
    return context;
  }
}
