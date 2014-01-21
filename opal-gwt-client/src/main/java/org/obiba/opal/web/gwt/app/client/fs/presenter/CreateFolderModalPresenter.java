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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderCreatedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class CreateFolderModalPresenter extends ModalPresenterWidget<CreateFolderModalPresenter.Display>
    implements CreateFolderUiHandlers {

  public interface Display extends PopupView, HasUiHandlers<CreateFolderUiHandlers> {
    void hideDialog();
  }

  private final Translations translations = GWT.create(Translations.class);

  private FileDto currentFolder;

  @Inject
  public CreateFolderModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(FolderUpdatedEvent.getType(), new FolderUpdatedEvent.FolderUpdatedHandler() {
      @Override
      public void onFolderUpdated(FolderUpdatedEvent event) {
        currentFolder = event.getFolder();
      }
    });
  }

  @Override
  public void createFolder(String folderName) {
    if("".equals(folderName)) {
      fireEvent(NotificationEvent.newBuilder().error(translations.folderNameIsRequired()).build());
    } else if(".".equals(folderName) || "..".equals(folderName)) {
      fireEvent(NotificationEvent.newBuilder().error(translations.dotNamesAreInvalid()).build());
    } else {
      createRemoteFolder(currentFolder.getPath(), folderName);
    }
  }

  private void createRemoteFolder(String destination, String folder) {

    ResourceCallback<FileDto> createdCallback = new ResourceCallback<FileDto>() {

      @Override
      public void onResource(Response response, FileDto resource) {
        fireEvent(new FolderCreatedEvent(resource));
        getView().hideDialog();
      }
    };

    ResponseCodeCallback error = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }
    };

    ResourceRequestBuilderFactory.<FileDto>newBuilder().forResource("/files" + destination).post()
        .withBody("text/plain", folder).withCallback(createdCallback).withCallback(Response.SC_FORBIDDEN, error)
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, error).send();
  }
}
