package org.obiba.opal.core.upgrade;

import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

public class UpdateAllHibernateSchemasStep extends AbstractUpgradeStep {

  private DatabaseRegistry databaseRegistry;

  @Override
  public void execute(Version currentVersion) {
    for(Database database : databaseRegistry.listSqlDatabases()) {
      LocalSessionFactoryBean sessionFactory = (LocalSessionFactoryBean) databaseRegistry
          .getSessionFactory(database.getName(), null);
      new SchemaUpdate(sessionFactory.getConfiguration()).execute(false, true);
    }
  }

  public void setDatabaseRegistry(DatabaseRegistry databaseRegistry) {
    this.databaseRegistry = databaseRegistry;
  }

}
