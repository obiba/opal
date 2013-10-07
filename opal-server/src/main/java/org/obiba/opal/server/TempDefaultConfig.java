package org.obiba.opal.server;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.datasource.hibernate.support.HibernateDatasourceFactory;
import org.obiba.magma.datasource.mongodb.MongoDBDatasourceFactory;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbDatabase;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.opal.core.runtime.jdbc.DatabaseSessionFactoryProvider;
import org.obiba.opal.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

//TODO to be deleted once UI is fully working
@Component
@Deprecated
@Transactional
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class TempDefaultConfig {

  public static final String USERNAME = "root";

  public static final String PASSWORD = "1234";

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private ProjectService projectService;

  @Autowired
  private OpalConfigurationService configService;

  public void createDefaultConfig() {

    if(databaseRegistry.getDatabase("opal-data") != null) return;

    System.out.println("Create default config...");

    SqlDatabase opalData = new SqlDatabase.Builder() //
        .name("opal-data") //
        .url("jdbc:mysql://localhost:3306/opal_dev?characterEncoding=UTF-8") //
        .driverClass("com.mysql.jdbc.Driver") //
        .username(USERNAME) //
        .password(PASSWORD) //
        .editable(false) //
        .usage(Database.Usage.STORAGE) //
        .sqlSchema(SqlDatabase.SqlSchema.HIBERNATE) //
        .build();
    databaseRegistry.addOrReplaceDatabase(opalData);

    SqlDatabase opalKey = new SqlDatabase.Builder() //
        .name("opal-key") //
        .url("jdbc:mysql://localhost:3306/key_dev?characterEncoding=UTF-8") //
        .driverClass("com.mysql.jdbc.Driver") //
        .username(USERNAME) //
        .password(PASSWORD) //
        .editable(false) //
        .usedForIdentifiers(true) //
        .usage(Database.Usage.STORAGE) //
        .sqlSchema(SqlDatabase.SqlSchema.HIBERNATE) //
        .build();
    databaseRegistry.addOrReplaceDatabase(opalKey);

    createDatasourceAndProject("opal-data", opalData);
    createDatasourceAndProject("mica_demo", opalData);

    MongoDbDatabase opalMongo = new MongoDbDatabase.Builder() //
        .name("mongo-data") //
        .url("mongodb://localhost:27017/opal_data") //
        .editable(false) //
        .usage(Database.Usage.STORAGE) //
        .build();

    createDatasourceAndProject("mongo", opalMongo);
  }

  private void createDatasourceAndProject(String datasourceName, final SqlDatabase opalData) {
    final Datasource datasource = databaseRegistry.createStorageMagmaDatasource(datasourceName, opalData);
    MagmaEngine.get().addDatasource(datasource);
    configService.modifyConfiguration(new OpalConfigurationService.ConfigModificationTask() {

      @Override
      public void doWithConfig(OpalConfiguration config) {
        config.getMagmaEngineFactory().withFactory(new HibernateDatasourceFactory(datasource.getName(),
            new DatabaseSessionFactoryProvider(datasource.getName(), databaseRegistry, opalData.getName())));
      }
    });
    projectService.getOrCreateProject(datasource);
  }

  private void createDatasourceAndProject(String datasourceName, final MongoDbDatabase opalData) {
    final Datasource datasource = databaseRegistry.createStorageMagmaDatasource(datasourceName, opalData);
    MagmaEngine.get().addDatasource(datasource);
    configService.modifyConfiguration(new OpalConfigurationService.ConfigModificationTask() {

      @Override
      public void doWithConfig(OpalConfiguration config) {
        MongoDBDatasourceFactory factory = new MongoDBDatasourceFactory();
        factory.setName(datasource.getName());
        factory.setConnectionURI(opalData.getUrl());
        config.getMagmaEngineFactory().withFactory(factory);
      }
    });
    projectService.getOrCreateProject(datasource);
  }

}
