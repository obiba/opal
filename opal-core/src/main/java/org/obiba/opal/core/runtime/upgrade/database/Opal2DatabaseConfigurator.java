package org.obiba.opal.core.runtime.upgrade.database;

import javax.annotation.Nonnull;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class Opal2DatabaseConfigurator {

  private static final Logger log = LoggerFactory.getLogger(Opal2DatabaseConfigurator.class);

  private static final String URL = "org.obiba.opal.datasource.url";

  private static final String USERNAME = "org.obiba.opal.datasource.username";

  private static final String PASSWORD = "org.obiba.opal.datasource.password";

  private static final String DIALECT = "org.obiba.opal.datasource.dialect";

  private static final String VALIDATION_QUERY = "org.obiba.opal.datasource.validationQuery";

  private static final String DRIVER = "org.obiba.opal.datasource.driver";

  @Nonnull
  private final String propertiesFile;

  public Opal2DatabaseConfigurator() {
    propertiesFile = getPropertiesFile();
  }

  public void configureDatabase() {
    try {
      addOpalConfigProperties();
    } catch(ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  private String getPropertiesFile() {
    String opalHome = System.getenv().get("OPAL_HOME");
    if(Strings.isNullOrEmpty(opalHome)) {
      throw new RuntimeException("Environment variable OPAL_HOME is not defined, cannot upgrade Opal!");
    }
    return opalHome + "/conf/opal-config.properties";
  }

  private void addOpalConfigProperties() throws ConfigurationException {
    log.debug("Configure new opal-config database");
    PropertiesConfiguration config = new PropertiesConfiguration(propertiesFile);
    PropertiesConfigurationLayout layout = config.getLayout();
    config.setProperty(DRIVER, "org.hsqldb.jdbcDriver");
    config.setProperty(URL, "jdbc:hsqldb:file:opal_config_db;shutdown=true;hsqldb.tx=mvcc");
    config.setProperty(USERNAME, "sa");
    config.setProperty(PASSWORD, "");
    config.setProperty(DIALECT, "org.hibernate.dialect.HSQLDialect");
    config.setProperty(VALIDATION_QUERY, "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
    layout.setComment(DRIVER, "\nOpal internal database settings");
    config.save(propertiesFile);
  }
}
