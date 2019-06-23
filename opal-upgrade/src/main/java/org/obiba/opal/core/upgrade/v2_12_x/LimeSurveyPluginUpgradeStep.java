/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v2_12_x;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
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

    if (limeSurveyDatabases != null && limeSurveyDatabases.iterator().hasNext()) {
      log.info("Deleting all registered LimeSurvey databases");

      try {
        Path propertiesBackupDirectoryPath = getPropertiesBackupDirectoryPath();

        for (Database limeSurveyDatabase : limeSurveyDatabases) {
          SqlSettings sqlSettings = limeSurveyDatabase.getSqlSettings();

          if (limeSurveyDatabase.hasSqlSettings() && sqlSettings != null) {
            Properties properties = toProperties(sqlSettings);

            try {
              properties.store(new FileWriter(propertiesBackupDirectoryPath.resolve(limeSurveyDatabase.getName() + ".properties").toString()), limeSurveyDatabase.getName());
            } catch (IOException e) {
              log.error(e.getMessage());
            }
          }

          orientDbService.delete(limeSurveyDatabase);
        }
      } catch(Exception e) {
        log.error("Unable to migrate the Limesurvey database settings to opal-datasource-limesurvey plugin configuration", e);
      }
    }

  }

  private Path getPropertiesBackupDirectoryPath() {
    File installedDirectory;

    try {
      pluginsService.installPlugin(OPAL_DATASOURCE_LIMESURVEY, null);
      PluginResources installedPlugin = pluginsService.getInstalledPlugin(OPAL_DATASOURCE_LIMESURVEY);
      installedDirectory = installedPlugin != null ? installedPlugin.getDirectory() : new File(OpalRuntime.PLUGINS_DIR);

    } catch(NoSuchElementException e) {
      log.error(e.getMessage());
      installedDirectory = new File(OpalRuntime.PLUGINS_DIR);
    }

    Path path = installedDirectory.toPath().resolve("limesurvey-conf").toAbsolutePath();
    path.toFile().mkdirs();
    return path;
  }

  private Properties toProperties(SqlSettings sqlSettings) {
    Properties properties = new Properties();

    properties.setProperty("usage.IMPORT.url", sqlSettings.getUrl());
    properties.setProperty("usage.IMPORT.username", sqlSettings.getUsername());
    properties.setProperty("usage.IMPORT.password", sqlSettings.getPassword());
    properties.setProperty("usage.IMPORT.properties", sqlSettings.getProperties());

    return properties;
  }
}
