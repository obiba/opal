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
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.event.FolderCreationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class CreateFolderDialogPresenter extends PresenterWidget<CreateFolderDialogPresenter.Display> {

  public interface Display extends PopupView {

    void hideDialog();

    HasClickHandlers getCreateFolderButton();

    HasClickHandlers getCancelButton();

    HasText getFolderToCreate();

  }

  private final Translations translations = GWT.create(Translations.class);

  private FileDto currentFolder;

  @Inject
  public CreateFolderDialogPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  @Override
  public void onReveal() {
    getView().getFolderToCreate().setText("");
  }

  private void addEventHandlers() {
    registerHandler(getView().getCreateFolderButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String folderToCreate = getView().getFolderToCreate().getText();
        if("".equals(folderToCreate)) {
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(translations.folderNameIsRequired()).build());
        } else if(".".equals(folderToCreate) || "..".equals(folderToCreate)) {
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(translations.dotNamesAreInvalid()).build());
        } else {
          createFolder(currentFolder.getPath(), folderToCreate);
        }
      }
    }));

    registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));

  }

  private void createFolder(String destination, String folder) {

    ResourceCallback<FileDto> createdCallback = new ResourceCallback<FileDto>() {

      @Override
      public void onResource(Response response, FileDto resource) {
        getEventBus().fireEvent(new FolderCreationEvent(resource));
        getView().hideDialog();
      }
    };

    ResponseCodeCallback error = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }
    };

    ResourceRequestBuilderFactory.<FileDto>newBuilder().forResource("/files" + destination).post()
        .withBody("text/plain", folder).withCallback(createdCallback).withCallback(403, error).withCallback(500, error)
        .send();
  }

  public void setCurrentFolder(FileDto currentFolder) {
    this.currentFolder = currentFolder;
  }
}
