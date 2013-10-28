package org.obiba.opal.core.upgrade;

import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.obiba.runtime.upgrade.support.jdbc.SqlScriptUpgradeStep;
import org.springframework.core.io.Resource;

public class AllSqlDatabasesScriptUpgradeStep extends AbstractUpgradeStep {

  private DatabaseRegistry databaseRegistry;

  private Resource scriptPath;

  private String scriptBasename;

  @Override
  public void execute(Version currentVersion) {
    for(Database database : databaseRegistry.listSqlDatabases()) {
      SqlScriptUpgradeStep step = new SqlScriptUpgradeStep(databaseRegistry.getDataSource(database.getName(), null),
          scriptBasename, scriptPath);
      step.setAppliesTo(getAppliesTo());
      step.execute(currentVersion);
    }
  }

  public void setDatabaseRegistry(DatabaseRegistry databaseRegistry) {
    this.databaseRegistry = databaseRegistry;
  }

  public void setScriptBasename(String scriptBasename) {
    this.scriptBasename = scriptBasename;
  }

  public void setScriptPath(Resource scriptPath) {
    this.scriptPath = scriptPath;
  }
}
