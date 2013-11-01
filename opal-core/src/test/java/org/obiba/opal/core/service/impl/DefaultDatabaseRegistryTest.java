package org.obiba.opal.core.service.impl;

import java.util.List;
import java.util.Properties;

import javax.transaction.TransactionManager;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.runtime.jdbc.SessionFactoryFactory;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.database.IdentifiersDatabaseNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.obiba.opal.core.domain.database.Database.Usage;

@ContextConfiguration(classes = DefaultDatabaseRegistryTest.Config.class)
public class DefaultDatabaseRegistryTest extends AbstractJUnit4SpringContextTests {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private OrientDbService orientDbService;

  @Before
  public void clear() throws Exception {
    orientDbService.deleteAll(Database.class);
  }

  @Test
  public void test_new_sql_database() {
    Database database = createSqlDatabase();
    databaseRegistry.save(database);

    List<Database> databases = newArrayList(databaseRegistry.list());
    assertEquals(1, databases.size());
    assertDatabaseEquals(database, databases.get(0));

    Database found = databaseRegistry.getDatabase(database.getName());
    assertDatabaseEquals(database, found);

    assertEquals(1, size(databaseRegistry.list(Usage.IMPORT)));
    assertEquals(0, size(databaseRegistry.list(Usage.STORAGE)));
    assertEquals(0, size(databaseRegistry.list(Usage.EXPORT)));
    assertEquals(1, size(databaseRegistry.listSqlDatabases()));
    assertEquals(0, size(databaseRegistry.listMongoDatabases()));
  }

  @Test
  public void test_new_mongo_database() {
    Database database = createMongoDatabase();
    databaseRegistry.save(database);

    List<Database> databases = newArrayList(databaseRegistry.list());
    assertEquals(1, databases.size());
    assertDatabaseEquals(database, databases.get(0));

    Database found = databaseRegistry.getDatabase(database.getName());
    assertDatabaseEquals(database, found);

    assertEquals(1, size(databaseRegistry.listMongoDatabases()));
    assertEquals(0, size(databaseRegistry.listSqlDatabases()));
  }

  @Test
  public void test_update_sql_database() {
    Database database = createSqlDatabase();
    databaseRegistry.save(database);

    database.setUsage(Usage.STORAGE);
    assertNotNull(database.getSqlSettings());
    database.getSqlSettings().setUsername("user2");
    database.getSqlSettings().setUrl("url2");
    databaseRegistry.save(database);

    List<Database> databases = newArrayList(databaseRegistry.list());
    assertEquals(1, databases.size());
    assertDatabaseEquals(database, databases.get(0));

    Database found = databaseRegistry.getDatabase(database.getName());
    assertDatabaseEquals(database, found);

    assertEquals(1, size(databaseRegistry.listSqlDatabases()));
    assertEquals(0, size(databaseRegistry.listMongoDatabases()));
  }

  @Test
  public void test_update_mongo_database() {
    Database database = createMongoDatabase();
    databaseRegistry.save(database);

    database.setUsage(Usage.STORAGE);
    assertNotNull(database.getMongoDbSettings());
    database.getMongoDbSettings().setUsername("user2");
    database.getMongoDbSettings().setUrl("url2");
    databaseRegistry.save(database);

    List<Database> databases = newArrayList(databaseRegistry.list());
    assertEquals(1, databases.size());
    assertDatabaseEquals(database, databases.get(0));

    Database found = databaseRegistry.getDatabase(database.getName());
    assertDatabaseEquals(database, found);

    assertEquals(1, size(databaseRegistry.listMongoDatabases()));
    assertEquals(0, size(databaseRegistry.listSqlDatabases()));
  }

  @Test
  public void test_get_identifiers_database() {
    Database database = Database.Builder.create().name("sql database").usage(Usage.STORAGE).usedForIdentifiers(true)
        .build();
    databaseRegistry.save(database);
    Database found = databaseRegistry.getIdentifiersDatabase();
    assertTrue(found.isUsedForIdentifiers());

    assertTrue(databaseRegistry.hasIdentifiersDatabase());

    assertEquals(0, size(databaseRegistry.list()));
    assertEquals(0, size(databaseRegistry.list(Usage.IMPORT)));
    assertEquals(0, size(databaseRegistry.list(Usage.STORAGE)));
    assertEquals(0, size(databaseRegistry.list(Usage.EXPORT)));
    assertEquals(0, size(databaseRegistry.listMongoDatabases()));
    assertEquals(0, size(databaseRegistry.listSqlDatabases()));
  }

  @Test(expected = IdentifiersDatabaseNotFoundException.class)
  public void test_get_null_identifiers_database() {
    databaseRegistry.getIdentifiersDatabase();
  }

  @Test
  public void test_has_identifiers_database() {
    assertFalse(databaseRegistry.hasIdentifiersDatabase());
  }

  @Test
  public void test_delete_database() {
    Database database = createSqlDatabase();
    databaseRegistry.save(database);
    databaseRegistry.delete(database);

    assertEquals(0, size(databaseRegistry.list()));
  }

  @Test
  public void test_list_sql_databases() {
    databaseRegistry.save(createSqlDatabase());
    assertEquals(1, size(databaseRegistry.list()));
    assertEquals(1, size(databaseRegistry.listSqlDatabases()));
    assertEquals(0, size(databaseRegistry.listMongoDatabases()));
  }

  @Test
  public void test_list_mongo_databases() {

  }

  @Test
  public void test_change_default_storage() {
    Database database = createSqlDatabase();
    databaseRegistry.save(database);

    Database database2 = createSqlDatabase();
    database2.setName("default storage");
    databaseRegistry.save(database2);

    assertEquals(2, size(databaseRegistry.list()));

    assertFalse(databaseRegistry.getDatabase(database.getName()).isDefaultStorage());
    assertTrue(databaseRegistry.getDatabase(database2.getName()).isDefaultStorage());
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
        .editable(true) //
        .description("description") //
        .defaultStorage(true) //
        .usage(Usage.IMPORT);
  }

  private void assertDatabaseEquals(Database expected, Database found) {
    assertNotNull(found);
    assertEquals(expected, found);
    assertEquals(expected.getName(), found.getName());
    assertEquals(expected.getUsage(), found.getUsage());
    assertEquals(expected.getDescription(), found.getDescription());
    assertEquals(expected.isDefaultStorage(), found.isDefaultStorage());
    assertEquals(expected.isEditable(), found.isEditable());
    assertEquals(expected.isUsedForIdentifiers(), found.isUsedForIdentifiers());
    if(expected.hasSqlSettings()) {
      assertSqlSettingsEquals(expected.getSqlSettings(), found.getSqlSettings());
    }
    if(expected.hasMongoDbSettings()) {
      assertMongoDbSettingsEquals(expected.getMongoDbSettings(), found.getMongoDbSettings());
    }

    Asserts.assertCreatedTimestamps(expected, found);
  }

  private void assertSqlSettingsEquals(SqlSettings expected, SqlSettings found) {
    assertNotNull(expected);
    assertNotNull(found);
    assertEquals(expected.getSqlSchema(), found.getSqlSchema());
    assertEquals(expected.getDriverClass(), found.getDriverClass());
    assertEquals(expected.getUrl(), found.getUrl());
    assertEquals(expected.getUsername(), found.getUsername());
    assertEquals(expected.getPassword(), found.getPassword());
    assertEquals(expected.getProperties(), found.getProperties());

    //TODO
//    expected.getJdbcDatasourceSettings()
//    expected.getLimesurveyDatasourceSettings()
  }

  private void assertMongoDbSettingsEquals(MongoDbSettings expected, MongoDbSettings found) {
    assertNotNull(expected);
    assertNotNull(found);
    assertEquals(expected.getUrl(), found.getUrl());
    assertEquals(expected.getUsername(), found.getUsername());
    assertEquals(expected.getPassword(), found.getPassword());
    assertEquals(expected.getProperties(), found.getProperties());
  }

  @Configuration
  public static class Config extends AbstractOrientDbTestConfig {

    @Bean
    public DataSourceFactory dataSourceFactory() {
      return EasyMock.createMock(DataSourceFactory.class);
    }

    @Bean
    public SessionFactoryFactory sessionFactoryFactory() {
      return EasyMock.createMock(SessionFactoryFactory.class);
    }

    @Bean
    public TransactionManager transactionManager() {
      return EasyMock.createMock(TransactionManager.class);
    }

    @Bean(name = "hibernate")
    public Properties hibernateProperties() {
      return new Properties();
    }

    @Bean
    public DatabaseRegistry databaseRegistry() {
      return new DefaultDatabaseRegistry();
    }

  }
}
