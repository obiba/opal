package org.obiba.opal.core.cfg;

import javax.sql.DataSource;

import org.obiba.opal.core.runtime.jdbc.DataSourceFactoryBean;
import org.obiba.opal.core.service.security.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("configDataSource")
public class ConfigDataSourceFactoryBean extends DataSourceFactoryBean {

  private static final Logger log = LoggerFactory.getLogger(ConfigDataSourceFactoryBean.class);

  public static final String DB_PATH = System.getProperty("OPAL_HOME") + "/data/hsql/opal_config";

  public static final String USERNAME = "opal";

  @Autowired
  private OpalConfigurationService opalConfigurationService;

  @Autowired
  private CryptoService cryptoService;

  public ConfigDataSourceFactoryBean() {
    setDriverClass("org.hsqldb.jdbcDriver");
    setUrl("jdbc:hsqldb:file:" + DB_PATH + ";shutdown=true;hsqldb.tx=mvcc");
    setUsername(USERNAME);
  }

  @Override
  public DataSource getObject() {
    log.debug("Get DataSource {}", getUrl());
    if(password == null) {
      setPassword(cryptoService.decrypt(opalConfigurationService.getOpalConfiguration().getDatabasePassword()));
    }
    return super.getObject();
  }

}
