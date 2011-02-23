/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.fs.presenter;

import net.customware.gwt.presenter.client.BasicPresenter;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.rest.client.RequestUrlBuilder;

import com.google.inject.Inject;

/**
 *
 */
public class FileDownloadPresenter extends BasicPresenter<FileDownloadPresenter.Display> {

  public interface Display extends WidgetDisplay {

    HasUrl getFileDownloader();
  }

  public interface HasUrl {

    public void setUrl(String url);
  }

  private final RequestUrlBuilder urlBuilder;

  @Inject
  public FileDownloadPresenter(Display display, EventBus eventBus, RequestUrlBuilder urlBuilder) {
    super(display, eventBus);
    this.urlBuilder = urlBuilder;
  }

  @Override
  protected void onBind() {
    super.registerHandler(eventBus.addHandler(FileDownloadEvent.getType(), new FileDownloadEvent.Handler() {

      public void onFileDownload(FileDownloadEvent event) {
        downloadFile(event.getUrl());
      }
    }));
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  public void downloadFile(String url) {
    getDisplay().getFileDownloader().setUrl(urlBuilder.buildAbsoluteUrl(url));
  }

}
