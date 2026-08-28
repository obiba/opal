/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.google.common.base.Predicate;
import com.google.common.eventbus.EventBus;
import org.easymock.EasyMock;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.SocketFactoryProvider;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactory;
import org.obiba.opal.core.service.database.CannotDeleteDatabaseLinkedToDatasourceException;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.database.IdentifiersDatabaseNotFoundException;
import org.obiba.opal.core.service.database.InvalidH2DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import javax.net.ssl.SSLSocketFactory;
import javax.sql.DataSource;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.obiba.opal.core.domain.database.Database.Usage;

@ContextConfiguration(classes = DefaultDatabaseRegistryTest.Config.class)
public class DefaultDatabaseRegistryTest extends AbstractOrientdbServiceTest {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private DataSourceFactory dataSourceFactory;

  @Override
  public void startDB() throws Exception {
    super.startDB();
    databaseRegistry.stop();
    orientDbService.deleteAll(Database.class);
  }

  @Override
  public void stopDB() {
    super.stopDB();
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

  @Test(expected = IllegalArgumentException.class)
  public void test_create_database_with_same_name() {
    Database database = createSqlDatabase();
    databaseRegistry.create(database);
    databaseRegistry.create(database);
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

  @Test
  public void test_new_h2_database() {
    Database database = createH2Database(Usage.STORAGE, "jdbc:h2:file:opal");
    databaseRegistry.create(database);

    assertDatabaseEquals(database, databaseRegistry.getDatabase(database.getName()));
    assertThat(databaseRegistry.listSqlDatabases()).hasSize(1);
  }

  @Test
  public void test_h2_database_is_storage_only() {
    for(Usage usage : new Usage[] { Usage.IMPORT, Usage.EXPORT }) {
      Database database = createH2Database(usage, "jdbc:h2:file:opal");
      try {
        databaseRegistry.create(database);
        fail("Expected an InvalidH2DatabaseException for usage: " + usage);
      } catch(InvalidH2DatabaseException ignored) {
      }
    }
    assertThat(databaseRegistry.listSqlDatabases()).isEmpty();
  }

  @Test
  public void test_h2_database_url_must_be_a_name() {
    for(String url : new String[] { "jdbc:h2:file:../escape", "jdbc:h2:file:/var/lib/opal", "jdbc:h2:mem:opal" }) {
      Database database = createH2Database(Usage.STORAGE, url);
      try {
        databaseRegistry.create(database);
        fail("Expected an InvalidH2DatabaseException for URL: " + url);
      } catch(InvalidH2DatabaseException ignored) {
      }
    }
    assertThat(databaseRegistry.listSqlDatabases()).isEmpty();
  }

  @Test
  public void test_h2_database_usage_is_validated_on_update() {
    Database database = createH2Database(Usage.STORAGE, "jdbc:h2:file:opal");
    databaseRegistry.create(database);

    database.setUsage(Usage.EXPORT);
    try {
      databaseRegistry.update(database);
      fail("Expected an InvalidH2DatabaseException");
    } catch(InvalidH2DatabaseException ignored) {
    }
    assertThat(databaseRegistry.getDatabase(database.getName()).getUsage()).isEqualTo(Usage.STORAGE);
  }

  private Database createH2Database(Usage usage, String url) {
    return createDatabase().usage(usage).defaultStorage(false).sqlSettings(SqlSettings.Builder.create() //
        .sqlSchema(SqlSettings.SqlSchema.JDBC) //
        .driverClass("org.h2.Driver") //
        .url(url) //
        .username("sa") //
        .password("password")) //
        .build();
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
    public TransactionTemplate transactionTemplate() {
      return EasyMock.createMock(TransactionTemplate.class);
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

    @Bean
    public EventBus eventBus() {
      return new EventBus();
    }

  }
}
