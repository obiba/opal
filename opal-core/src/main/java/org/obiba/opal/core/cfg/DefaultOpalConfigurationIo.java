/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.cfg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.obiba.core.spring.xstream.InjectingReflectionProviderWrapper;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

@Component
public class DefaultOpalConfigurationIo implements OpalConfigurationIo {

  private static final Logger log = LoggerFactory.getLogger(DefaultOpalConfigurationIo.class);

  @Value("${OPAL_HOME}/data/opal-config.xml")
  private File configFile;

  @Autowired
  private ApplicationContext applicationContext;

  @Override
  public OpalConfiguration readConfiguration() throws InvalidConfigurationException {
    if(configFile.exists()) {
      log.debug("Read existing Opal Configuration");
      try {
        return readFromXml(new FileInputStream(configFile));
      } catch(FileNotFoundException e) {
        throw new InvalidConfigurationException("Error reading Opal configuration file.", e);
      }
    }
    return readDefaultConfig();
  }

  @Override
  public void writeConfiguration(OpalConfiguration opalConfiguration) throws InvalidConfigurationException {
    try {
      File tmpConfig = File.createTempFile("cfg", ".xml");
      tmpConfig.deleteOnExit();
      try(Writer writer = new OutputStreamWriter(new FileOutputStream(tmpConfig), Charsets.UTF_8)) {
        doCreateXStreamInstance().toXML(opalConfiguration, writer);
      }
      Files.move(tmpConfig, configFile);
    } catch(FileNotFoundException e) {
      throw new InvalidConfigurationException(
          "Opal configuration file '" + configFile.getAbsolutePath() + "' is not a regular file.", e);
    } catch(IOException | XStreamException e) {
      throw new InvalidConfigurationException("Error writing Opal configuration file.", e);
    }
  }

  protected XStream doCreateXStreamInstance() {
    return MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory()
        .createXStream(new InjectingReflectionProviderWrapper(new PureJavaReflectionProvider(), applicationContext));
  }

  private OpalConfiguration readFromXml(InputStream inputStream) {
    try(InputStreamReader isr = new InputStreamReader(inputStream, Charsets.UTF_8)) {
      return (OpalConfiguration) doCreateXStreamInstance().fromXML(isr);
    } catch(IOException | XStreamException e) {
      throw new InvalidConfigurationException("Error reading Opal configuration file.", e);
    }
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private OpalConfiguration readDefaultConfig() {
    log.info("Read Opal Default Configuration");
    try {
      OpalConfiguration opalConfiguration = readFromXml(
          new DefaultResourceLoader().getResource("classpath:/opal-default-config.xml").getInputStream());
      configFile.getParentFile().mkdirs();
      Files.touch(new File(configFile.getParentFile(), configFile.getName()));
      writeConfiguration(opalConfiguration);
      return opalConfiguration;
    } catch(IOException e) {
      throw new InvalidConfigurationException("Error reading Opal default configuration file.", e);
    }
  }
}
