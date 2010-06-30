/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.fs;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileDownloadPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter.FileSelectionHandler;
import org.obiba.opal.web.model.client.FileDto;
import org.obiba.opal.web.model.client.FileDto.FileType;

/**
 *
 */
public class FileDownloadSelectionHandler implements FileSelectionHandler {
  //
  // Instance Variables
  //

  private FileDownloadPresenter fileDownloadPresenter;

  //
  // Constructors
  //

  public FileDownloadSelectionHandler(FileDownloadPresenter fileDownloadPresenter) {
    this.fileDownloadPresenter = fileDownloadPresenter;
  }

  //
  // FileSelectionHandler Methods
  //

  public void onFileSelection(FileDto fileDto) {
    if(fileDto.getType().isFileType(FileType.FILE)) {
      fileDownloadPresenter.downloadFile(fileDto);
    }
  }
}
