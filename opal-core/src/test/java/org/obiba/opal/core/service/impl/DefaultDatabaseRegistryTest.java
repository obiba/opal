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

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
  public void test_new_database() {
    Database database = createDatabase();
    databaseRegistry.save(database);

    List<Database> databases = Lists.newArrayList(databaseRegistry.list());
    assertEquals(1, databases.size());
    assertDatabaseEquals(database, databases.get(0));

    Database found = databaseRegistry.getDatabase(database.getName());
    assertDatabaseEquals(database, found);
  }

  @Test
  public void test_new_database_with_sql_and_mongo_settings() {

  }

  @Test
  public void test_get_identifiers_database() {
    Database database = Database.Builder.create().name("sql database").usedForIdentifiers(true).build();
    databaseRegistry.save(database);
    Database found = databaseRegistry.getIdentifiersDatabase();
    assertTrue(found.isUsedForIdentifiers());

    assertEquals(0, Lists.newArrayList(databaseRegistry.list()).size());
  }

  @Test(expected = IdentifiersDatabaseNotFoundException.class)
  public void test_get_null_identifiers_database() {
    databaseRegistry.getIdentifiersDatabase();
  }

  private Database createDatabase() {
    return Database.Builder.create() //
        .name("sql database") //
        .usedForIdentifiers(false) //
        .editable(true) //
        .description("description") //
        .defaultStorage(true) //
        .usage(Database.Usage.IMPORT) //
        .sqlSettings(SqlSettings.Builder.create() //
            .sqlSchema(SqlSettings.SqlSchema.HIBERNATE) //
            .driverClass("mysql") //
            .url("localhost") //
            .username("root") //
            .password("password") //
            .properties("props")) //
        .build();
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
