/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell.commands;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public abstract class AbstractOpalRuntimeDependentCommand<T> extends AbstractCommand<T> {

  private OpalRuntime opalRuntime;

  private OpalConfigurationService configService;

  @Autowired
  public void setOpalRuntime(OpalRuntime opalRuntime) {
    this.opalRuntime = opalRuntime;
  }

  @Autowired
  public void setConfigService(OpalConfigurationService configService) {
    this.configService = configService;
  }

  protected OpalRuntime getOpalRuntime() {
    return opalRuntime;
  }

  public FileObject getFileSystemRoot() {
    return getOpalRuntime().getFileSystem().getRoot();
  }

  public FileObject getFile(String path) throws FileSystemException {
    return getFileSystemRoot().resolveFile(path);
  }

  public FileObject getFile(FileObject folder, String fileName) throws FileSystemException {
    return folder.resolveFile(fileName);
  }

  public File getLocalFile(FileObject vsfFile) {
    return getOpalRuntime().getFileSystem().getLocalFile(vsfFile);
  }

  protected OpalConfiguration getOpalConfiguration() {
    return configService.getOpalConfiguration();
  }

}
