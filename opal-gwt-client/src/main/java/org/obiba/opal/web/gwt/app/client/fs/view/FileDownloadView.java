/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.fs.view;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileDownloadPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileDownloadPresenter.HasUrl;

import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class FileDownloadView implements FileDownloadPresenter.Display {
  //
  // Instance Variables
  //

  Frame downloader;

  // Adaptor for Frame
  HasUrl hasUrlImpl;

  //
  // Constructors
  //

  public FileDownloadView() {
    downloader = new Frame();
    downloader.setVisible(false);

    hasUrlImpl = new HasUrl() {

      public void setUrl(String url) {
        downloader.setUrl(url);
      }
    };
  }

  //
  // FileDownloadPresenter.Display Methods
  //

  @Override
  public HasUrl getFileDownloader() {
    return hasUrlImpl;
  }

  @Override
  public Widget asWidget() {
    return downloader;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

}
