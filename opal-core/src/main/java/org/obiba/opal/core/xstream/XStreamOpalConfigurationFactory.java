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

import java.io.InputStream;

import org.obiba.core.spring.xstream.InjectingReflectionProviderWrapper;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.springframework.context.ApplicationContext;

import com.thoughtworks.xstream.XStream;

/**
 * XStream-based factory for creating (deserializing) an {@link OpalConfiguration}.
 */
public class XStreamOpalConfigurationFactory {
  //
  // Static Methods
  //

  public static OpalConfiguration fromXML(ApplicationContext applicationContext, InputStream serializedConfiguration) {
    XStream xstream = new XStream(new InjectingReflectionProviderWrapper((new XStream()).getReflectionProvider(), applicationContext));

    OpalConfiguration opalConfiguration = (OpalConfiguration) xstream.fromXML(serializedConfiguration);

    return opalConfiguration;
  }
}
