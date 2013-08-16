package org.obiba.opal.core.runtime.upgrade.database;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.obiba.opal.core.domain.database.MongoDbDatabase;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.domain.unit.UnitKeyStoreState;
import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

public class MoveConfigTablesUpgradeStep extends AbstractUpgradeStep {

  private DatabaseRegistry databaseRegistry;

  private LocalSessionFactoryBean configSessionFactory;

  @Override
  public void execute(Version currentVersion) {
    configSessionFactory.createDatabaseSchema();
    copyConfigData();
    deleteTables();
  }

  private void copyConfigData() {
    SessionFactory dataSessionFactory = databaseRegistry.getSessionFactory("opal-data", null);
    Session dataSession = dataSessionFactory.getCurrentSession();
    Session configSession = configSessionFactory.getObject().getCurrentSession();
    copy(User.class, dataSession, configSession);
    copy(Group.class, dataSession, configSession);
    copy(SqlDatabase.class, dataSession, configSession);
    copy(MongoDbDatabase.class, dataSession, configSession);
    copy(UnitKeyStoreState.class, dataSession, configSession);
    copy(SubjectAcl.class, dataSession, configSession);
    dataSessionFactory.close();
  }

  @SuppressWarnings("TypeMayBeWeakened")
  private void copy(Class<?> clazz, Session dataSession, Session configSession) {
    for(Object o : dataSession.createCriteria(clazz).list()) {
      configSession.persist(o);
    }
  }

  private void deleteTables() {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    jdbcTemplate.execute("drop table user");
    jdbcTemplate.execute("drop table groups");
    jdbcTemplate.execute("drop table database_sql");
    jdbcTemplate.execute("drop table database_mongodb");
    jdbcTemplate.execute("drop table unit_key_store");
    jdbcTemplate.execute("drop table subject_acl");
  }

  public void setDatabaseRegistry(DatabaseRegistry databaseRegistry) {
    this.databaseRegistry = databaseRegistry;
  }

  public void setConfigSessionFactory(LocalSessionFactoryBean configSessionFactory) {
    this.configSessionFactory = configSessionFactory;
  }
}
