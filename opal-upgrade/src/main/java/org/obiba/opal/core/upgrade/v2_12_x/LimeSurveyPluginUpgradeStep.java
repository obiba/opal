package org.obiba.opal.core.upgrade.v2_12_x;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.plugins.PluginResources;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class LimeSurveyPluginUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(LimeSurveyPluginUpgradeStep.class);

  private static final String OPAL_DATASOURCE_LIMESURVEY = "opal-datasource-limesurvey";

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private PluginsService pluginsService;

  @Override
  public void execute(Version currentVersion) {
    Iterable<Database> limeSurveyDatabases = orientDbService
        .list(Database.class, "select * from Database where sqlSettings containsKey ? and sqlSettings containsValue ?",
            "sqlSchema", "LIMESURVEY");

    if (limeSurveyDatabases != null) {
      log.info("Deleting all registered LimeSurvey databases");

      Path propertiesBackupDirectoryPath = getPropertiesBackupDirectoryPath();

      for(Database limeSurveyDatabase : limeSurveyDatabases) {
        SqlSettings sqlSettings = limeSurveyDatabase.getSqlSettings();

        if (limeSurveyDatabase.hasSqlSettings() && sqlSettings != null) {
          Properties properties = toProperties(sqlSettings);

          try {
            properties.store(new FileWriter(propertiesBackupDirectoryPath.resolve(limeSurveyDatabase.getName() + ".properties").toString()), limeSurveyDatabase.getName());
          } catch(IOException e) {
            log.error(e.getMessage());
          }
        }

        orientDbService.delete(limeSurveyDatabase);
      }
    }

  }

  private Path getPropertiesBackupDirectoryPath() {
    File installedDirectory;

    PluginResources installedPlugin = pluginsService.getInstalledPlugin(OPAL_DATASOURCE_LIMESURVEY);
    if (installedPlugin != null) {
      installedDirectory = installedPlugin.getDirectory();
    } else {
      pluginsService.installPlugin(OPAL_DATASOURCE_LIMESURVEY, null);
      installedPlugin = pluginsService.getInstalledPlugin(OPAL_DATASOURCE_LIMESURVEY);
      installedDirectory = installedPlugin != null ? installedPlugin.getDirectory() : new File(OpalRuntime.PLUGINS_DIR);
    }

    Path path = installedDirectory.toPath().resolve("limesurvey-conf").toAbsolutePath();
    path.toFile().mkdirs();
    return path;
  }

  private Properties toProperties(SqlSettings sqlSettings) {
    Properties properties = new Properties();

    properties.setProperty("url", sqlSettings.getUrl());
    properties.setProperty("username", sqlSettings.getUsername());
    properties.setProperty("password", sqlSettings.getPassword());
    properties.setProperty("properties", sqlSettings.getProperties());

    return properties;
  }
}
