package org.obiba.opal.core.cfg;

import javax.sql.DataSource;

import org.obiba.opal.core.runtime.jdbc.DataSourceFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("configDataSource")
public class QuartzDataSourceFactoryBean extends DataSourceFactoryBean {

  public static final String DB_PATH = System.getProperty("OPAL_HOME") + "/data/hsql/opal_config";

  public static final String USERNAME = "opal";

  @Autowired
  private OpalConfigurationService opalConfigurationService;

  public QuartzDataSourceFactoryBean() {
    setDriverClass("org.hsqldb.jdbcDriver");
    setUrl("jdbc:hsqldb:file:" + DB_PATH + ";shutdown=true;hsqldb.tx=mvcc");
    setUsername(USERNAME);
  }

  @Override
  public DataSource getObject() {
    if(password == null) {
      setPassword(opalConfigurationService.getOpalConfiguration().getDatabasePassword());
    }
    return super.getObject();
  }
}
