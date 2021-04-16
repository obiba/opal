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

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.VersionProvider;

/**
 * Need this as we don't want a spring bean Version that would be autowire to OpalConfiguration automatically
 */
public class RuntimeVersionProvider implements VersionProvider {

  private final String versionString;

  public RuntimeVersionProvider(String versionString) {
    this.versionString = versionString;
  }

  @Override
  public Version getVersion() {
    return new Version(versionString);
  }

}
