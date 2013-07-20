/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.fs.presenter;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileResourceRequest;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class FilesAdministrationPresenter
    extends ItemAdministrationPresenter<FilesAdministrationPresenter.Display, FilesAdministrationPresenter.Proxy> {

  @ProxyStandard
  @NameToken(Places.files)
  public interface Proxy extends ProxyPlace<FilesAdministrationPresenter> {}

  private final RequestCredentials credentials;

  private final FileExplorerPresenter fileExplorerPresenter;

  private FileDto currentFolder;

  @Inject
  public FilesAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy, RequestCredentials credentials,
      FileExplorerPresenter fileExplorerPresenter) {
    super(eventBus, display, proxy);
    this.credentials = credentials;
    this.fileExplorerPresenter = fileExplorerPresenter;
  }

  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageFileExplorerTitle();
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot("Explorer", fileExplorerPresenter);
  }


  @Override
  public void onReveal() {
    if(currentFolder != null) {
      updateTable(currentFolder.getPath());
    } else {
      updateTable(getDefaultPath());
    }
  }

  private void updateTable(String path) {
    FileResourceRequest.newBuilder(getEventBus()).path(path).withCallback(new ResourceCallback<FileDto>() {
      @Override
      public void onResource(Response response, FileDto file) {
        currentFolder = file;
        getEventBus().fireEvent(new FolderSelectionChangeEvent(currentFolder));
        getEventBus().fireEvent(new FileSelectionChangeEvent(currentFolder));
      }
    }).send();
  }

  private String getDefaultPath() {
    return credentials.getUsername() == null ? "/" : "/home/" + credentials.getUsername();
  }

  public interface Display extends View {

  }
}
