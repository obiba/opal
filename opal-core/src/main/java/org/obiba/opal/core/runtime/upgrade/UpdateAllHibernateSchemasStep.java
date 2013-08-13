package org.obiba.opal.core.runtime.upgrade;

import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

public class UpdateAllHibernateSchemasStep extends AbstractUpgradeStep {

  private DatabaseRegistry databaseRegistry;

  @Override
  public void execute(Version currentVersion) {
    for(SqlDatabase database : databaseRegistry.list(SqlDatabase.class)) {
      LocalSessionFactoryBean sessionFactory = (LocalSessionFactoryBean) databaseRegistry
          .getSessionFactory(database.getName(), null);
      sessionFactory.updateDatabaseSchema();
    }
  }

  public void setDatabaseRegistry(DatabaseRegistry databaseRegistry) {
    this.databaseRegistry = databaseRegistry;
  }

}
