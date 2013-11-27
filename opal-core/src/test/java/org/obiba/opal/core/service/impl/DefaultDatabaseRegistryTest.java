package org.obiba.opal.core.service.impl;

import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.easymock.EasyMock;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.runtime.jdbc.SessionFactoryFactory;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.database.CannotDeleteDatabaseLinkedToDatasourceException;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.database.IdentifiersDatabaseNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.google.common.base.Predicate;

import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
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

  @Autowired
  private DataSourceFactory dataSourceFactory;

  @Autowired
  private SessionFactoryFactory sessionFactoryFactory;

  @Before
  public void clear() throws Exception {
    ((DefaultDatabaseRegistry) databaseRegistry).clearCaches();
    orientDbService.deleteAll(Database.class);
  }

  @After
  public void shutdown() throws Exception {
    MagmaEngine.get().shutdown();
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

  @Test(expected = CannotDeleteDatabaseLinkedToDatasourceException.class)
  public void test_delete_database_with_entities() {
    Database database = createSqlDatabase();
    databaseRegistry.save(database);

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
    databaseRegistry.save(createSqlDatabase());
    assertEquals(1, size(databaseRegistry.list()));
    assertEquals(1, size(databaseRegistry.listSqlDatabases()));
    assertEquals(0, size(databaseRegistry.listMongoDatabases()));
  }

  @Test
  public void test_list_mongo_databases() {
    databaseRegistry.save(createMongoDatabase());
    assertEquals(1, size(databaseRegistry.list()));
    assertEquals(0, size(databaseRegistry.listSqlDatabases()));
    assertEquals(1, size(databaseRegistry.listMongoDatabases()));
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

  @Test
  public void test_get_datasource() {
    Database database = createSqlDatabase();
    databaseRegistry.save(database);

    DataSource mockDatasource = EasyMock.createMock(DataSource.class);

    reset(dataSourceFactory);
    expect(dataSourceFactory.createDataSource(database)).andReturn(mockDatasource).once();
    replay(dataSourceFactory);

    DataSource datasource = databaseRegistry.getDataSource(database.getName(), "jdbc-datasource");
    verify(dataSourceFactory);

    assertEquals(mockDatasource, datasource);
    assertTrue(databaseRegistry.hasDatasource(database));
  }

  @Test
  public void test_get_session_factory() {
    Database database = createSqlDatabase();
    databaseRegistry.save(database);

    DataSource mockDatasource = EasyMock.createMock(DataSource.class);
    SessionFactory mockSessionFactory = EasyMock.createMock(SessionFactory.class);

    reset(dataSourceFactory, sessionFactoryFactory);
    expect(dataSourceFactory.createDataSource(database)).andReturn(mockDatasource).once();
    expect(sessionFactoryFactory.getSessionFactory(mockDatasource)).andReturn(mockSessionFactory).once();
    replay(dataSourceFactory, sessionFactoryFactory);

    SessionFactory sessionFactory = databaseRegistry.getSessionFactory(database.getName(), "hibernate-datasource");
    verify(sessionFactoryFactory, dataSourceFactory);

    assertEquals(mockSessionFactory, sessionFactory);
    assertTrue(databaseRegistry.hasDatasource(database));
  }

  @Test
  public void test_unregister() {
    Database database = createSqlDatabase();
    databaseRegistry.save(database);

    DataSource mockDatasource = EasyMock.createMock(DataSource.class);

    reset(dataSourceFactory);
    expect(dataSourceFactory.createDataSource(database)).andReturn(mockDatasource).once();
    replay(dataSourceFactory);

    databaseRegistry.getDataSource(database.getName(), "jdbc-datasource");
    verify(dataSourceFactory);

    databaseRegistry.unregister(database.getName(), "jdbc-datasource");

    assertFalse(databaseRegistry.hasDatasource(database));
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
    assertNotNull(found);
    assertEquals(expected, found);
    assertEquals(expected.getName(), found.getName());
    assertEquals(expected.getUsage(), found.getUsage());
    assertEquals(expected.isDefaultStorage(), found.isDefaultStorage());
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

    @Bean
    public IdentifiersTableService identifiersTableService() {
      return EasyMock.createMock(IdentifiersTableService.class);
    }

  }
}
