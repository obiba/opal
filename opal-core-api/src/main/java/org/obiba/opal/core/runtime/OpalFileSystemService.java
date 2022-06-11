/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.runtime;

import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.fs.OpalFileSystem;

public interface OpalFileSystemService extends SystemService {

  /**
   * For test purpose.
   *
   * @return
   */
  boolean hasFileSystem();

  /**
   * Get the opal file system.
   *
   * @return
   */
  OpalFileSystem getFileSystem();

}
