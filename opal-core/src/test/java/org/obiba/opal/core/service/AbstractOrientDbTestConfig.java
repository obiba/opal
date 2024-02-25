/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.easymock.EasyMock;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class AbstractOrientDbTestConfig {

  private static final File TEMP_FILE;

  static {
    try {
      TEMP_FILE = File.createTempFile("opal-test-", "");
      TEMP_FILE.delete();
      TEMP_FILE.mkdirs();
      TEMP_FILE.deleteOnExit();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Bean
  public PropertySourcesPlaceholderConfigurer propertiesResolver() {
    PropertySourcesPlaceholderConfigurer placeholderConfigurer = new PropertySourcesPlaceholderConfigurer();
    Properties properties = new Properties();
    properties.setProperty("OPAL_HOME", TEMP_FILE.getAbsolutePath());
    appendProperties(properties);
    placeholderConfigurer.setProperties(properties);
    return placeholderConfigurer;
  }

  @Bean
  public OrientDbServerFactory orientDbServerFactory() {
    OrientDbServerFactory factory = new LocalOrientDbServerFactory();
    factory.setUrl(LocalOrientDbServerFactory.URL.replace("${OPAL_HOME}", TEMP_FILE.getAbsolutePath()));
    return factory;
  }

  @Bean
  public OrientDbService orientDbService() {
    return new OrientDbServiceImpl();
  }

  @Bean
  public OpalConfigurationService opalConfigurationService() {
    OpalConfiguration configuration = new OpalConfiguration();
    configuration.setDatabasePassword("admin");
    OpalConfigurationService mock = EasyMock.createMock(OpalConfigurationService.class);
    mock.afterPropertiesSet();
    EasyMock.expect(mock.getOpalConfiguration()).andReturn(configuration).anyTimes();
    EasyMock.replay(mock);
    return mock;
  }

  protected void appendProperties(Properties properties) {

  }

}
