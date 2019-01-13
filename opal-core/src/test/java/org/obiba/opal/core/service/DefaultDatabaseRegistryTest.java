/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.sql.DataSource;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.easymock.EasyMock;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.SocketFactoryProvider;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.runtime.jdbc.SessionFactoryFactory;
import org.obiba.opal.core.service.database.CannotDeleteDatabaseLinkedToDatasourceException;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.database.IdentifiersDatabaseNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.jta.JtaTransactionManager;

import com.google.common.base.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
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
    databaseRegistry.stop();
    orientDbService.deleteAll(Database.class);
  }

  @After
  public void shutdown() throws Exception {
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

  @Test
  public void test_create_database_with_same_name() {
    Database database = createSqlDatabase();
    databaseRegistry.create(database);

    try {
      databaseRegistry.create(database);
      fail("Should throw ConstraintViolationException");
    } catch(ConstraintViolationException e) {
      Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
      assertThat(violations).hasSize(1);
      ConstraintViolation<?> violation = violations.iterator().next();
      assertThat(violation.getMessage()).isEqualTo("must be unique");
      assertThat(violation.getMessageTemplate()).isEqualTo("{org.obiba.opal.core.validator.Unique.message}");
      assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
    }
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
  public void test_get_session_factory() {
    Database database = createSqlDatabase();
    databaseRegistry.create(database);

    DataSource mockDatasource = EasyMock.createMock(DataSource.class);
    SessionFactory mockSessionFactory = EasyMock.createMock(SessionFactory.class);

    reset(dataSourceFactory, sessionFactoryFactory);
    expect(dataSourceFactory.createDataSource(database)).andReturn(mockDatasource).once();
    expect(sessionFactoryFactory.getSessionFactory(mockDatasource)).andReturn(mockSessionFactory).once();
    replay(dataSourceFactory, sessionFactoryFactory);

    SessionFactory sessionFactory = databaseRegistry.getSessionFactory(database.getName(), "hibernate-datasource");
    verify(sessionFactoryFactory, dataSourceFactory);

    assertThat(mockSessionFactory).isEqualTo(sessionFactory);
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
    public JtaTransactionManager jtaTransactionManager() {
      return EasyMock.createMock(JtaTransactionManager.class);
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
    public SocketFactoryProvider socketFactoryProvider() {
      return () -> SSLSocketFactory.getDefault();
    }

    @Bean
    public IdentifiersTableService identifiersTableService() {
      return EasyMock.createMock(IdentifiersTableService.class);
    }

  }
}
