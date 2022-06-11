/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.support;

import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.fs.OpalFileSystem;

public class OpalFileSystemServiceMock implements OpalFileSystemService {
  @Override
  public boolean hasFileSystem() {
    return false;
  }

  @Override
  public OpalFileSystem getFileSystem() {
    return null;
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {

  }
}
