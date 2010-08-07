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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.obiba.core.spring.xstream.InjectingReflectionProviderWrapper;
import org.obiba.core.util.StreamUtil;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.xstream.DefaultXStreamFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

/**
 * Spring FactoryBean that returns a singleton {@link OpalConfiguration}.
 */
public class OpalConfigurationFactoryBean implements FactoryBean<OpalConfiguration>, ApplicationContextAware {
  //
  // Instance Variables
  //
  private static final Charset UTF8 = Charset.forName("UTF-8");

  private ApplicationContext applicationContext;

  private OpalConfiguration opalConfiguration;

  private File configFile;

  @PostConstruct
  public void readConfiguration() throws IOException {
    InputStreamReader isr = null;
    try {
      isr = new InputStreamReader(new FileInputStream(configFile), UTF8);
      opalConfiguration = (OpalConfiguration) doCreateXStreamInstance(applicationContext).fromXML(isr);
    } finally {
      StreamUtil.silentSafeClose(isr);
      MagmaEngine.get().shutdown();
    }
  }

  @PreDestroy
  public void writeConfiguration() throws IOException {
    OutputStreamWriter writer = null;
    try {
      writer = new OutputStreamWriter(new FileOutputStream(configFile), UTF8);
      doCreateXStreamInstance(applicationContext).toXML(opalConfiguration, writer);
    } finally {
      StreamUtil.silentSafeClose(writer);
    }
  }

  //
  // FactoryBean Methods
  //

  public OpalConfiguration getObject() throws Exception {
    return opalConfiguration;
  }

  public Class<OpalConfiguration> getObjectType() {
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

  protected XStream doCreateXStreamInstance(ApplicationContext applicationContext) {
    new MagmaEngine().extend(new MagmaJsExtension());
    return new DefaultXStreamFactory().createXStream(new InjectingReflectionProviderWrapper(new PureJavaReflectionProvider(), applicationContext));
  }

}
