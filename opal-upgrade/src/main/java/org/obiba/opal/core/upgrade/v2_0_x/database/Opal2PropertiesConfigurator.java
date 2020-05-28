/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v2_0_x.database;

import javax.validation.constraints.NotNull;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Opal2PropertiesConfigurator {

  private static final Logger log = LoggerFactory.getLogger(Opal2PropertiesConfigurator.class);

  private static final String URL = "org.obiba.opal.datasource.url";

  private static final String USERNAME = "org.obiba.opal.datasource.username";

  private static final String PASSWORD = "org.obiba.opal.datasource.password";

  private static final String DIALECT = "org.obiba.opal.datasource.dialect";

  private static final String VALIDATION_QUERY = "org.obiba.opal.datasource.validationQuery";

  private static final String DRIVER = "org.obiba.opal.datasource.driver";

  private static final String R_SERVER_ADMIN_PORT = "org.obiba.rserver.port";

  @NotNull
  private final String propertiesFile;

  public Opal2PropertiesConfigurator() {
    propertiesFile = getPropertiesFile();
  }

  public void upgrade() {
    try {
      addOpalDatabaseConfigProperties();
      addRServerConfigProperties();
    } catch(ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  private String getPropertiesFile() {
    return System.getenv().get("OPAL_HOME") + "/conf/opal-config.properties";
  }

  private void addRServerConfigProperties() throws ConfigurationException {
    log.debug("Configure R server");
    PropertiesConfiguration config = new PropertiesConfiguration(propertiesFile);
    PropertiesConfigurationLayout layout = config.getLayout();
    layout.setFooterComment("\n# R server administration\n" +
        "# Specify the port number of the R server controller.\n" +
        "#" + R_SERVER_ADMIN_PORT + "=6312");
    config.save(propertiesFile);
  }

  private void addOpalDatabaseConfigProperties() throws ConfigurationException {
    log.debug("Configure new opal-config database");
    PropertiesConfiguration config = new PropertiesConfiguration(propertiesFile);
    PropertiesConfigurationLayout layout = config.getLayout();
    config.setProperty(DRIVER, "org.hsqldb.jdbcDriver");
    config.setProperty(URL, "jdbc:hsqldb:file:data/hsql/opal_config;shutdown=true;hsqldb.tx=mvcc");
    config.setProperty(USERNAME, "sa");
    config.setProperty(PASSWORD, "");
    config.setProperty(DIALECT, "org.hibernate.dialect.HSQLDialect");
    config.setProperty(VALIDATION_QUERY, "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
    layout.setComment(DRIVER, "\nOpal internal database settings");
    config.save(propertiesFile);
  }
}
