package org.obiba.opal.core.upgrade.v2_12_x;

import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class LimeSurveyPluginUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(LimeSurveyPluginUpgradeStep.class);

  @Autowired
  OrientDbService orientDbService;

  @Override
  public void execute(Version currentVersion) {
    Iterable<Database> limeSurveyDatabases = orientDbService
        .list(Database.class, "select * from Database where sqlSettings containsKey ? and sqlSettings containsValue ?",
            "sqlSchema", "LIMESURVEY");

    if (limeSurveyDatabases != null) {
      log.info("Deleting all registered LimeSurvey databases");

      for(Database limeSurveyDatabase : limeSurveyDatabases) {
        orientDbService.delete(limeSurveyDatabase);
      }
    }

  }
}
