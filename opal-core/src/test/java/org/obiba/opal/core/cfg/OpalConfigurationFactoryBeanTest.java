/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngineExtension;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.obiba.magma.datasource.hibernate.support.HibernateDatasourceFactory;
import org.obiba.magma.datasource.hibernate.support.SpringBeanSessionFactoryProvider;
import org.obiba.magma.support.MagmaEngineFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Unit tests for XStreamOpalConfigurationFactory.
 */
public class OpalConfigurationFactoryBeanTest {
  //
  // Instance Variables
  //

  private ApplicationContext applicationContext;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() throws Exception {
    applicationContext = new ClassPathXmlApplicationContext("classpath*:test-spring-context.xml");
  }

  //
  // Test Methods
  //

  /**
   * Test method for
   * {@link org.obiba.opal.core.xstream.XStreamOpalConfigurationFactory#newConfiguration(org.springframework.context.ApplicationContext, java.io.InputStream)}
   * .
   * @throws Exception
   */
  @Test
  public void testFromXML() throws Exception {

    OpalConfigurationFactoryBean factoryBean = new OpalConfigurationFactoryBean();

    factoryBean.setConfigFile(new File("src/test/resources/XStreamOpalConfigurationFactoryTest/opal-config.xml"));
    factoryBean.setApplicationContext(applicationContext);
    OpalConfiguration opalConfiguration = (OpalConfiguration) factoryBean.getObject();

    // Verify OpalConfiguration was deserialized (not null).
    assertNotNull(opalConfiguration);

    // Verify configured engine class.
    MagmaEngineFactory magmaEngineFactory = opalConfiguration.getMagmaEngineFactory();
    assertNotNull(magmaEngineFactory);
    assertEquals("org.obiba.magma.MagmaEngine", magmaEngineFactory.getEngineClass());

    // Verify configured engine extensions.
    assertEquals(2, magmaEngineFactory.extensions().size());
    if(!containsEngineExtension(magmaEngineFactory.extensions(), "magma-js") || !containsEngineExtension(magmaEngineFactory.extensions(), "magma-xstream")) {
      fail("Missing engine extensions");
    }

    // Verify configured datasource factories.
    assertEquals(1, magmaEngineFactory.factories().size());
    DatasourceFactory datasourceFactory = magmaEngineFactory.factories().iterator().next();
    assertTrue(datasourceFactory instanceof HibernateDatasourceFactory);
    HibernateDatasourceFactory hibernateDatasourceFactory = (HibernateDatasourceFactory) datasourceFactory;
    SessionFactoryProvider sessionFactoryProvider = hibernateDatasourceFactory.getSessionFactoryProvider();
    assertNotNull(sessionFactoryProvider);
    assertEquals("my-datasource", hibernateDatasourceFactory.getName());
    assertTrue(sessionFactoryProvider instanceof SpringBeanSessionFactoryProvider);
    SpringBeanSessionFactoryProvider springBeanSessionFactoryProvider = (SpringBeanSessionFactoryProvider) sessionFactoryProvider;
    assertNotNull(springBeanSessionFactoryProvider.getBeanFactory());
    assertEquals("hibernateSessionFactoryBeanName", springBeanSessionFactoryProvider.getBeanName());
  }

  //
  // Helper Methods
  //

  private boolean containsEngineExtension(Set<MagmaEngineExtension> extensions, String name) {
    for(MagmaEngineExtension extension : extensions) {
      if(extension.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }
}
