/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.obiba.opal.core.service.OrientDbServerFactory;
import org.obiba.opal.core.service.OrientDbService;
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
//      TEMP_FILE.deleteOnExit();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertiesResolver() {
    PropertySourcesPlaceholderConfigurer placeholderConfigurer = new PropertySourcesPlaceholderConfigurer();
    Properties properties = new Properties();
    properties.setProperty("OPAL_HOME", TEMP_FILE.getAbsolutePath());
    placeholderConfigurer.setProperties(properties);
    return placeholderConfigurer;
  }

  @Bean
  public DefaultBeanValidator defaultBeanValidator() {
    return new DefaultBeanValidator();
  }

  @Bean
  public OrientDbServerFactory orientDbServerFactory() {
    String url = LocalOrientDbServerFactory.URL.replace("${OPAL_HOME}", TEMP_FILE.getAbsolutePath());
    LocalOrientDbServerFactory.start(url);
    return new LocalOrientDbServerFactory();
  }

  @Bean
  public OrientDbService orientDbService() {
    return new OrientDbServiceImpl();
  }

}
