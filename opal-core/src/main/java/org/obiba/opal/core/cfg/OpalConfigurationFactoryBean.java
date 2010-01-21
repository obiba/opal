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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.obiba.opal.core.xstream.XStreamOpalConfigurationFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring FactoryBean that returns a singleton {@link OpalConfiguration}.
 */
public class OpalConfigurationFactoryBean implements FactoryBean, ApplicationContextAware {
  //
  // Instance Variables
  //

  private ApplicationContext applicationContext;

  private OpalConfiguration opalConfiguration;

  private File configFile;

  //
  // FactoryBean Methods
  //

  public Object getObject() throws Exception {
    if(opalConfiguration == null) {
      initObject();
    }
    return opalConfiguration;
  }

  @SuppressWarnings("unchecked")
  public Class getObjectType() {
    return OpalConfiguration.class;
  }

  public boolean isSingleton() {
    return true;
  }

  //
  // ApplicationContextAware Methods
  //

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  //
  // Methods
  //

  public void setConfigFile(File configFile) {
    this.configFile = configFile;
  }

  private void initObject() throws IOException {
    InputStream serializedConfiguration = null;
    try {
      serializedConfiguration = new FileInputStream(configFile);
      opalConfiguration = (OpalConfiguration) XStreamOpalConfigurationFactory.fromXML(applicationContext, serializedConfiguration);
    } finally {
      if(serializedConfiguration != null) {
        try {
          serializedConfiguration.close();
        } catch(IOException ex) {
        }
      }
    }
  }
}
