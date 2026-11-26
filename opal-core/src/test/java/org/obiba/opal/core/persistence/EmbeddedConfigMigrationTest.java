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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

/**
 * The copy of the embedded configuration database into an external one, through the same wiring as the server.
 * <p>
 * The external database is an in-memory H2 one by default. To run against PostgreSQL, which is what the migration is
 * for, point {@code -Dopal.config.datasource.url} (and {@code .username}, {@code .password}) at an empty database:
 * its {@code public} schema is dropped and created again before every test.
 */
public class EmbeddedConfigMigrationTest {

  private static final String DATASOURCE_URL = "opal.config.datasource.url";

  private static final String PASSWORD = "s3cr3t-generated-password";

  private static final byte[] KEY_STORE = { 0, 1, 2, (byte) 0xff, 0x7f, (byte) 0x80 };

  private static final Timestamp CREATED = Timestamp.valueOf("2026-01-02 03:04:05.123456");

  private File opalHome;

  private String targetUrl;

  private String targetUser;

  private String targetPassword;

  @Before
  public void setUp() throws Exception {
    opalHome = Files.createTempDirectory("opal-config-migration-test").toFile();
    targetUrl = System.getProperty(DATASOURCE_URL);
    if(targetUrl == null) {
      targetUrl = "jdbc:h2:mem:opal-config-target-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
      targetUser = "opal";
      targetPassword = "target-password";
    } else {
      targetUser = System.getProperty("opal.config.datasource.username", "");
      targetPassword = System.getProperty("opal.config.datasource.password", "");
      try(Connection connection = target(); Statement statement = connection.createStatement()) {
        statement.execute("DROP SCHEMA public CASCADE");
        statement.execute("CREATE SCHEMA public");
      }
    }
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.delete(opalHome);
  }

  @Test
  public void test_configuration_is_copied_with_its_ids_and_values() throws Exception {
    createEmbeddedConfiguration();

    try(AnnotationConfigApplicationContext context = openExternal(true)) {
      assertThat(context.getBean("configEmbeddedMigration")).isEqualTo(EmbeddedConfigMigration.Outcome.MIGRATED);
      // Hibernate validated the target schema once the copy was done: the context would not have started otherwise.
      assertThat(context.getBean(DataSource.class)).isNotNull();
    }

    try(Connection connection = target(); Statement statement = connection.createStatement()) {
      try(ResultSet rs = statement.executeQuery("SELECT id, name, sql_settings, default_storage, created " +
          "FROM databases ORDER BY id")) {
        assertThat(rs.next()).isTrue();
        assertThat(rs.getLong(1)).isEqualTo(2);
        assertThat(rs.getString(2)).isEqualTo("db2");
        assertThat(rs.getString(3)).isEqualTo("{\"url\":\"jdbc:h2:file:db2\"}");
        assertThat(rs.getBoolean(4)).isTrue();
        assertThat(rs.getTimestamp(5)).isEqualTo(CREATED);
        assertThat(rs.next()).isTrue();
        assertThat(rs.getLong(1)).isEqualTo(3);
        assertThat(rs.next()).isFalse();
      }
      try(ResultSet rs = statement.executeQuery("SELECT key_store FROM keystore_states")) {
        assertThat(rs.next()).isTrue();
        assertThat(rs.getBytes(1)).isEqualTo(KEY_STORE);
      }
      try(ResultSet rs = statement.executeQuery("SELECT tags FROM apps WHERE id = 'rock-1'")) {
        assertThat(rs.next()).isTrue();
        assertThat(rs.getString(1)).isEqualTo("[\"default\",\"élevé\"]");
      }
    }

    // The identity was moved past the copied ids, or this insert would take id 1, 2 or 3 and collide with id 2 or 3.
    try(Connection connection = target()) {
      connection.setAutoCommit(true);
      insertDatabase(connection, "db4");
      try(Statement statement = connection.createStatement();
          ResultSet rs = statement.executeQuery("SELECT id FROM databases WHERE name = 'db4'")) {
        assertThat(rs.next()).isTrue();
        assertThat(rs.getLong(1)).isEqualTo(4);
      }
    }
  }

  @Test
  public void test_the_second_run_of_bin_opal_skips_the_copy() throws Exception {
    createEmbeddedConfiguration();

    // bin/opal starts Opal twice, the upgrade run then the server run, with the same settings.
    try(AnnotationConfigApplicationContext upgrade = openExternal(true)) {
      assertThat(upgrade.getBean("configEmbeddedMigration")).isEqualTo(EmbeddedConfigMigration.Outcome.MIGRATED);
    }
    try(AnnotationConfigApplicationContext server = openExternal(true)) {
      assertThat(server.getBean("configEmbeddedMigration")).isEqualTo(EmbeddedConfigMigration.Outcome.SKIPPED);
    }
    assertThat(countInTarget("databases")).isEqualTo(2);
  }

  @Test
  public void test_a_target_holding_a_configuration_is_left_alone() throws Exception {
    createEmbeddedConfiguration();
    try(AnnotationConfigApplicationContext ignored = openExternal(false); Connection connection = target()) {
      connection.setAutoCommit(true);
      try(Statement statement = connection.createStatement()) {
        statement.execute("INSERT INTO apps (id, name) VALUES ('already-there', 'already-there')");
      }
    }

    try(AnnotationConfigApplicationContext context = openExternal(true)) {
      assertThat(context.getBean("configEmbeddedMigration")).isEqualTo(EmbeddedConfigMigration.Outcome.SKIPPED);
    }
    assertThat(countInTarget("apps")).isEqualTo(1);
    assertThat(countInTarget("databases")).isEqualTo(0);
  }

  @Test
  public void test_a_setting_left_in_place_does_not_need_the_embedded_password() throws Exception {
    createEmbeddedConfiguration();
    try(AnnotationConfigApplicationContext context = openExternal(true)) {
      assertThat(context.getBean("configEmbeddedMigration")).isEqualTo(EmbeddedConfigMigration.Outcome.MIGRATED);
    }

    // the <databasePassword> is gone from opal-config.xml while data/config is still there
    try(AnnotationConfigApplicationContext context = openExternal(true, "")) {
      assertThat(context.getBean("configEmbeddedMigration")).isEqualTo(EmbeddedConfigMigration.Outcome.SKIPPED);
    }
  }

  @Test
  public void test_other_tables_in_the_target_schema_are_ignored() throws Exception {
    createEmbeddedConfiguration();
    // What a project's data looks like in a schema shared with the configuration: a table of its own, with rows.
    try(AnnotationConfigApplicationContext ignored = openExternal(false); Connection connection = target()) {
      connection.setAutoCommit(true);
      try(Statement statement = connection.createStatement()) {
        statement.execute("CREATE TABLE variables (id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
            "name VARCHAR(255))");
        statement.execute("INSERT INTO variables (name) VALUES ('age'), ('sex')");
        // A name that apps_config matches as a pattern, where _ stands for any character.
        statement.execute("CREATE TABLE apps1config (unrelated VARCHAR(255))");
      }
    }

    try(AnnotationConfigApplicationContext context = openExternal(true)) {
      assertThat(context.getBean("configEmbeddedMigration")).isEqualTo(EmbeddedConfigMigration.Outcome.MIGRATED);
    }
    assertThat(countInTarget("databases")).isEqualTo(2);
    assertThat(countInTarget("variables")).isEqualTo(2);
  }

  @Test
  public void test_the_embedded_store_is_left_untouched() throws Exception {
    createEmbeddedConfiguration();
    File store = new File(opalHome, "data/config/opal-config.mv.db");
    byte[] before = sha256(store);

    try(AnnotationConfigApplicationContext ignored = openExternal(true)) {
      assertThat(sha256(store)).isEqualTo(before);
    }
    // The copy it read from is gone: only the original remains in data.
    assertThat(new File(opalHome, "data").list()).containsOnly("config");
  }

  @Test
  public void test_a_store_at_another_schema_version_is_not_copied() throws Exception {
    createEmbeddedConfiguration();
    try(Connection connection = embedded(); Statement statement = connection.createStatement()) {
      statement.execute("DELETE FROM opal_changelog WHERE id = '3-tasks'");
    }

    assertMigrationFails("not at the same schema version", "3-tasks");
    assertThat(countInTarget("databases")).isEqualTo(0);
  }

  @Test
  public void test_a_value_postgresql_cannot_store_is_named_and_nothing_is_written() throws Exception {
    createEmbeddedConfiguration();
    try(Connection connection = embedded(); Statement statement = connection.createStatement()) {
      statement.execute("INSERT INTO apps (id, name, tags) VALUES ('rock-nul', 'rock-nul', 'a' || CHAR(0) || 'b')");
    }

    assertMigrationFails("NUL character", "tags");
    assertThat(countInTarget("databases")).isEqualTo(0);
    assertThat(countInTarget("apps")).isEqualTo(0);
  }

  @Test
  public void test_a_store_created_with_the_credentials_the_target_has_now_is_migrated() throws Exception {
    // config.datasource.username and password set while embedded, then kept for the external database
    try(AnnotationConfigApplicationContext ignored = openEmbedded(targetUser, targetPassword)) {
      // only creates the store and its schema
    }
    try(AnnotationConfigApplicationContext context = openExternal(true)) {
      assertThat(context.getBean("configEmbeddedMigration")).isEqualTo(EmbeddedConfigMigration.Outcome.MIGRATED);
    }
  }

  @Test
  public void test_a_store_created_with_other_credentials_is_refused_and_nothing_is_written() throws Exception {
    try(AnnotationConfigApplicationContext ignored = openEmbedded("someone", "something-else")) {
      // only creates the store and its schema
    }
    assertMigrationFails("created with other credentials");
  }

  @Test
  public void test_without_an_embedded_store_there_is_nothing_to_migrate() {
    try(AnnotationConfigApplicationContext context = openExternal(true)) {
      assertThat(context.getBean("configEmbeddedMigration"))
          .isEqualTo(EmbeddedConfigMigration.Outcome.NOTHING_TO_MIGRATE);
    }
  }

  //
  // Fixtures
  //

  /**
   * An embedded configuration database as Opal leaves it, with ids that do not start at 1 and do not follow each
   * other, a binary column, and text that is not ASCII.
   */
  private void createEmbeddedConfiguration() throws Exception {
    try(AnnotationConfigApplicationContext ignored = openEmbedded()) {
      // only creates the store and its schema
    }
    try(Connection connection = embedded(); Statement statement = connection.createStatement()) {
      insertDatabase(connection, "db1");
      insertDatabase(connection, "db2");
      insertDatabase(connection, "db3");
      statement.execute("DELETE FROM databases WHERE name = 'db1'");
      try(PreparedStatement ps = connection.prepareStatement(
          "INSERT INTO keystore_states (created, name, key_store) VALUES (?, 'system', ?)")) {
        ps.setTimestamp(1, CREATED);
        ps.setBytes(2, KEY_STORE);
        ps.executeUpdate();
      }
      statement.execute("INSERT INTO apps (id, name, tags) VALUES ('rock-1', 'rock', '[\"default\",\"élevé\"]')");
    }
  }

  private void insertDatabase(Connection connection, String name) throws Exception {
    try(PreparedStatement ps = connection.prepareStatement("INSERT INTO databases (default_storage, " +
        "used_for_identifiers, created, name, sql_settings) VALUES (?, false, ?, ?, ?)")) {
      ps.setBoolean(1, "db2".equals(name));
      ps.setTimestamp(2, CREATED);
      ps.setString(3, name);
      ps.setString(4, "{\"url\":\"jdbc:h2:file:" + name + "\"}");
      ps.executeUpdate();
    }
  }

  private void assertMigrationFails(String... expected) {
    try(AnnotationConfigApplicationContext ignored = openExternal(true)) {
      fail("Expected the migration to fail");
    } catch(Exception e) {
      ConfigDatabaseException cause = null;
      for(Throwable t = e; t != null; t = t.getCause()) {
        if(t instanceof ConfigDatabaseException) cause = (ConfigDatabaseException) t;
      }
      assertThat(cause).isNotNull();
      // Tables and columns are named as the target spells them: upper case on H2, lower case on PostgreSQL.
      for(String text : expected) assertThat(cause.getMessage().toLowerCase()).contains(text.toLowerCase());
      assertThat(cause.getMessage()).contains("Nothing was written");
    }
  }

  private long countInTarget(String table) throws Exception {
    try(Connection connection = target(); Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
      rs.next();
      return rs.getLong(1);
    }
  }

  private Connection target() throws Exception {
    return DriverManager.getConnection(targetUrl, targetUser, targetPassword);
  }

  /**
   * Straight to the store, with autocommit, as a fixture: the context that created it is closed.
   */
  private Connection embedded() throws Exception {
    return DriverManager.getConnection(
        "jdbc:h2:file:" + new File(opalHome, "data/config/opal-config").getAbsolutePath(), "opal", PASSWORD);
  }

  private static byte[] sha256(File file) throws Exception {
    return MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file.toPath()));
  }

  private AnnotationConfigApplicationContext openEmbedded() {
    return open(new HashMap<>());
  }

  private AnnotationConfigApplicationContext openEmbedded(String username, String password) {
    Map<String, Object> properties = new HashMap<>();
    properties.put("config.datasource.username", username);
    properties.put("config.datasource.password", password);
    return open(properties);
  }

  private AnnotationConfigApplicationContext openExternal(boolean migrateEmbedded) {
    return openExternal(migrateEmbedded, "enc:" + PASSWORD);
  }

  private AnnotationConfigApplicationContext openExternal(boolean migrateEmbedded, String encryptedPassword) {
    Map<String, Object> properties = new HashMap<>();
    properties.put("config.datasource.url", targetUrl);
    properties.put("config.datasource.driverClass",
        targetUrl.startsWith("jdbc:postgresql:") ? "org.postgresql.Driver" : "org.h2.Driver");
    properties.put("config.datasource.username", targetUser);
    properties.put("config.datasource.password", targetPassword);
    properties.put("config.datasource.migrateEmbedded", Boolean.toString(migrateEmbedded));
    return open(properties, encryptedPassword);
  }

  private AnnotationConfigApplicationContext open(Map<String, Object> properties) {
    return open(properties, "enc:" + PASSWORD);
  }

  private AnnotationConfigApplicationContext open(Map<String, Object> properties, String encryptedPassword) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    properties.put("OPAL_HOME", opalHome.getAbsolutePath());
    context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
    context.registerBean(PropertySourcesPlaceholderConfigurer.class);
    context.registerBean(TestOpalConfigurationService.class,
        () -> new TestOpalConfigurationService(encryptedPassword));
    context.register(ConfigDatabaseConfiguration.class);
    context.refresh();
    return context;
  }
}
