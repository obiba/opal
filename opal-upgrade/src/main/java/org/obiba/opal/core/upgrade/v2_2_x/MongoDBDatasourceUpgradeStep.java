/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v2_2_x;

import org.mongeez.Mongeez;
import org.obiba.magma.datasource.mongodb.MongoDBDatasourceFactory;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

public class MongoDBDatasourceUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(MongoDBDatasourceUpgradeStep.class);

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Override
  public void execute(Version currentVersion) {
    for(Database database : databaseRegistry.listMongoDatabases()) {
      if(database.hasMongoDbSettings()) {
        upgradeMongoDBSchema(database.getMongoDbSettings());
      }
    }
    if (databaseRegistry.getIdentifiersDatabase().hasMongoDbSettings()) {
      upgradeMongoDBSchema(databaseRegistry.getIdentifiersDatabase().getMongoDbSettings());
    }
  }

  private void upgradeMongoDBSchema(MongoDbSettings settings) {
    try {
      MongoDBDatasourceFactory factory = settings.createMongoDBDatasourceFactory("_");
      Mongeez mongeez = new Mongeez();
      mongeez
          .setFile(new ClassPathResource("/META-INF/opal/upgrade-scripts/2.2.x/mongeez.xml"));
      mongeez.setMongo(factory.getMongoDBFactory().getMongoClient());
      mongeez.setDbName(factory.getMongoDbDatabaseName());
      mongeez.process();
    } catch(Exception e) {
      log.error("Failed upgrading datasource collection: {}", e.getMessage(), e);
    }
  }

}
