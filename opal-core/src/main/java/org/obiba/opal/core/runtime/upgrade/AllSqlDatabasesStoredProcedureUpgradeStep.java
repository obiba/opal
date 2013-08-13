package org.obiba.opal.core.runtime.upgrade;

import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.core.io.Resource;

public class AllSqlDatabasesStoredProcedureUpgradeStep extends AbstractUpgradeStep {

  private DatabaseRegistry databaseRegistry;

  private Resource scriptPath;

  private String scriptBasename;

  private String procedureName;

  @Override
  public void execute(Version currentVersion) {
    for(SqlDatabase database : databaseRegistry.list(SqlDatabase.class)) {
      StoredProcedureUpgradeStep step = new StoredProcedureUpgradeStep(
          databaseRegistry.getDataSource(database.getName(), null), scriptBasename, scriptPath, procedureName);
      step.setAppliesTo(getAppliesTo());
      step.execute(currentVersion);
    }
  }

  public void setDatabaseRegistry(DatabaseRegistry databaseRegistry) {
    this.databaseRegistry = databaseRegistry;
  }

  public void setProcedureName(String procedureName) {
    this.procedureName = procedureName;
  }

  public void setScriptBasename(String scriptBasename) {
    this.scriptBasename = scriptBasename;
  }

  public void setScriptPath(Resource scriptPath) {
    this.scriptPath = scriptPath;
  }
}
