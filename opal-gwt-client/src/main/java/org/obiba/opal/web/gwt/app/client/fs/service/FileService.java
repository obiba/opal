/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.fs.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;
import com.google.inject.Singleton;
import org.obiba.opal.web.model.client.opal.FileDto;

@Singleton
public class FileService {

  private static final String LAST_FOLDER = "lastFolder";

  private String lastFolder;

  public FileService() {
    stockStore = Storage.getSessionStorageIfSupported();
    GWT.log("Session storage is supported: " + isStoreSupported());
  }

  public String getLastFolder() {
    if (isStoreSupported())
      return stockStore.getItem(LAST_FOLDER);
    return lastFolder;
  }

  public FileDto getLastFolderDto() {
    String path = getLastFolder();
    if (path == null) return null;
    FileDto folder = FileDto.create();
    folder.setType(FileDto.FileType.FOLDER);
    folder.setPath(path);
    return folder;
  }

  private Storage stockStore = null;

  public void clear() {
    if (isStoreSupported()) stockStore.clear();
    else lastFolder = null;
  }

  private boolean isStoreSupported() {
    return stockStore != null;
  }

  public void setLastFolder(String path) {
    if (isStoreSupported())
      stockStore.setItem(LAST_FOLDER, path);
    else
      lastFolder = path;
  }
}
