package org.obiba.opal.core.cfg;

import javax.sql.DataSource;

import org.obiba.opal.core.runtime.jdbc.DataSourceFactoryBean;
import org.obiba.opal.core.service.security.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("configDataSource")
public class QuartzDataSourceFactoryBean extends DataSourceFactoryBean {

  public static final String DB_PATH = System.getProperty("OPAL_HOME") + "/data/hsql/opal_config";

  public static final String USERNAME = "opal";

  @Autowired
  private OpalConfigurationService opalConfigurationService;

  @Autowired
  private CryptoService cryptoService;

  // use to skip password encryption during upgrade
  private boolean encryptNullPassword = true;

  public QuartzDataSourceFactoryBean() {
    setDriverClass("org.hsqldb.jdbcDriver");
    setUrl("jdbc:hsqldb:file:" + DB_PATH + ";shutdown=true;hsqldb.tx=mvcc");
    setUsername(USERNAME);
  }

  @Override
  public DataSource getObject() {
    if(password == null && encryptNullPassword) {
      setPassword(cryptoService.decrypt(opalConfigurationService.getOpalConfiguration().getDatabasePassword()));
    }
    return super.getObject();
  }

  public void setEncryptNullPassword(boolean encryptNullPassword) {
    this.encryptNullPassword = encryptNullPassword;
  }
}
