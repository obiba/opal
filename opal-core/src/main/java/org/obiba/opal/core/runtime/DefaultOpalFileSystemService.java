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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.fs.impl.DefaultOpalFileSystem;
import org.obiba.opal.fs.security.SecuredOpalFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultOpalFileSystemService implements OpalFileSystemService {

  private static final Logger log = LoggerFactory.getLogger(DefaultOpalFileSystemService.class);

  private OpalConfigurationService opalConfigurationService;

  private OpalFileSystem opalFileSystem;

  @Autowired
  public void setOpalConfigurationService(OpalConfigurationService opalConfigurationService) {
    this.opalConfigurationService = opalConfigurationService;
  }

  @Override
  public void start() {
    initFileSystem();
  }

  @Override
  public void stop() {
    if (opalFileSystem != null)
      opalFileSystem.close();
  }

  @Override
  public boolean hasFileSystem() {
    return true;
  }

  @Override
  public OpalFileSystem getFileSystem() {
    if (opalFileSystem == null)
      initFileSystem();
    return opalFileSystem;
  }

  private synchronized void initFileSystem() {
    try {
      opalFileSystem = new SecuredOpalFileSystem(
          new DefaultOpalFileSystem(opalConfigurationService.getOpalConfiguration().getFileSystemRoot()));

      // Create some system folders, if they do not exist.
      ensureFolder("home");
      ensureFolder("projects");
      ensureFolder("reports");
      ensureFolder("tmp");
    } catch (RuntimeException e) {
      log.error("The opal filesystem cannot be started.");
      throw e;
    } catch (FileSystemException e) {
      log.error("Error creating a system directory in the Opal File System.", e);
    }
  }

  private void ensureFolder(String path) throws FileSystemException {
    FileObject folder = getFileSystem().getRoot().resolveFile(path);
    folder.createFolder();
  }

}
