/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v4_5_x;
import com.beust.jcommander.internal.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOneModel;
import org.bson.Document;
import org.obiba.magma.SocketFactoryProvider;
import org.obiba.magma.datasource.mongodb.MongoDBDatasourceFactory;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class MongoDBGridFSUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(MongoDBGridFSUpgradeStep.class);

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private SocketFactoryProvider socketFactoryProvider;

  @Override
  public void execute(Version currentVersion) {
    for(Database database : databaseRegistry.listMongoDatabases()) {
      if(database.hasMongoDbSettings()) {
        upgradeGridFSMetadata(database.getMongoDbSettings());
      }
    }
  }

  /**
   * In previous MongoDB java driver, it was possible to store a file with a null name. This makes the
   * new driver fail. The upgrade consists of setting an empty filename to the files metadata documents.
   *
   * @param settings
   */
  private void upgradeGridFSMetadata(MongoDbSettings settings) {
    try {
      MongoDBDatasourceFactory factory = settings.createMongoDBDatasourceFactory("_", socketFactoryProvider);
      String dbName = factory.getMongoDbDatabaseName();
      MongoDatabase db = factory.getMongoDBFactory().getMongoClient().getDatabase(dbName);
      MongoCollection<Document> filesColl = db.getCollection("fs.files");
      try (MongoCursor<Document> cursor = filesColl.find(Filters.eq("filename", null)).cursor()) {
        List<ReplaceOneModel<Document>> toReplace = Lists.newArrayList();
        while (cursor.hasNext()) {
          Document file = cursor.next();
          file.put("filename", "");
          toReplace.add(new ReplaceOneModel<>(Filters.eq("_id", file.getObjectId("_id")), file));
        }
        if (!toReplace.isEmpty()) {
          filesColl.bulkWrite(toReplace);
        }
      }
    } catch(Exception e) {
      log.error("Failed upgrading mongodb fs.files collection at [{}]: {}", settings.getUrl(), e.getMessage(), e);
    }
  }

}
