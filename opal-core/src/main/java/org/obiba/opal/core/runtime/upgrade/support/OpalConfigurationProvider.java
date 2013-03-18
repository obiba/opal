/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.upgrade.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.obiba.core.util.StreamUtil;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.thoughtworks.xstream.XStream;

import de.schlichtherle.io.FileInputStream;

/**
 *
 */
@Component
public class OpalConfigurationProvider {

  private static final Charset CHARSET = Charset.availableCharsets().get("UTF-8");

  @Value("${OPAL_HOME}/conf/opal-config.xml")
  private File configFile;

  public OpalConfiguration readOpalConfiguration(boolean initMagma) {
    boolean instanciated = MagmaEngine.isInstanciated();
    if(initMagma && !instanciated) {
      new MagmaEngine().extend(new MagmaJsExtension()).extend(new MagmaXStreamExtension());
    }
    Reader reader = null;
    try {
      reader = new InputStreamReader(new FileInputStream(configFile), CHARSET);
      XStream xstream = MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();
      return (OpalConfiguration) xstream.fromXML(reader);
    } catch(FileNotFoundException e) {
      throw new RuntimeException("Could not read Opal configuration file.", e);
    } finally {
      StreamUtil.silentSafeClose(reader);
      if(initMagma && !instanciated) {
        MagmaEngine.get().shutdown();
      }
    }
  }
}
