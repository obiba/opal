/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.xstream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Set;

import org.junit.Test;
import org.obiba.magma.MagmaEngineExtension;
import org.obiba.magma.support.MagmaEngineFactory;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.springframework.context.support.StaticApplicationContext;

/**
 * Unit tests for XStreamOpalConfigurationFactory.
 */
public class XStreamOpalConfigurationFactoryTest {

  /**
   * Test method for
   * {@link org.obiba.opal.core.xstream.XStreamOpalConfigurationFactory#newConfiguration(org.springframework.context.ApplicationContext, java.io.InputStream)}
   * .
   */
  @Test
  public void testNewConfiguration() {
    OpalConfiguration opalConfiguration = null;

    // Deserialize an OpalConfiguration.
    InputStream serializedConfiguration = ClassLoader.getSystemResourceAsStream("XStreamOpalConfigurationFactoryTest/opal-config.xml");
    opalConfiguration = XStreamOpalConfigurationFactory.fromXML(new StaticApplicationContext(), serializedConfiguration);

    // Verify OpalConfiguration was deserialized (not null).
    assertNotNull(opalConfiguration);

    // Verify configured engine class.
    MagmaEngineFactory magmaEngineFactory = opalConfiguration.getMagmaEngineFactory();
    assertEquals("org.obiba.magma.MagmaEngine", magmaEngineFactory.getEngineClass());

    // Verify configured engine extensions.
    assertEquals(2, magmaEngineFactory.extensions().size());
    if(!containsEngineExtension(magmaEngineFactory.extensions(), "magma-js") || !containsEngineExtension(magmaEngineFactory.extensions(), "magma-xstream")) {
      fail("Missing engine extensions");
    }
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
