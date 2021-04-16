/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi;

import java.io.File;
import org.obiba.plugins.spi.ServicePlugin;

public interface OpalServicePlugin extends ServicePlugin {

  /**
   * Resolve a Opal file system path to a local file.
   */
  interface OpalFileSystemPathResolver {
    File resolve(String path);
  }

  /**
   * Sets an instance of an Opal file system path resolver.
   *
   * @param resolver
   */
  void setOpalFileSystemPathResolver(OpalFileSystemPathResolver resolver);

}
