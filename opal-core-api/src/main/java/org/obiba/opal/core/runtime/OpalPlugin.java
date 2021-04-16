/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.runtime;

import org.obiba.plugins.PluginResources;
import org.obiba.runtime.Version;

import java.io.File;

/**
 * Opal plugin resources.
 */
public class OpalPlugin extends PluginResources {

  public OpalPlugin(File directory) {
    super(directory);
  }

  @Override
  public String getHostVersionKey() {
    return "opal.version";
  }

  @Override
  public String getHostHome() {
    return System.getProperty("OPAL_HOME");
  }

  public Version getOpalVersion() {
    return getHostVersion();
  }

}
