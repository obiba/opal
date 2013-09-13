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
import org.obiba.opal.web.gwt.app.client.fs.event.FileUploadedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.RequestUrlBuilder;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class FileUploadModalPresenter extends ModalPresenterWidget<FileUploadModalPresenter.Display>
    implements FileUploadModalUiHandlers {

  public interface Display extends PopupView, HasUiHandlers<FileUploadModalUiHandlers> {

    void hideDialog();

    void submit(String url);

    void setRemoteFolderName(String folderName);
  }

  private final Translations translations;

  private FileDto currentFolder;

  private Runnable actionRequiringConfirmation;

  private final RequestUrlBuilder urlBuilder;

  @Inject
  public FileUploadModalPresenter(Display display, EventBus eventBus, RequestUrlBuilder urlBuilder,
      Translations translations) {
    super(eventBus, display);
    this.translations = translations;
    this.urlBuilder = urlBuilder;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(FolderUpdatedEvent.getType(), new FolderUpdatedEvent.Handler() {
      @Override
      public void onFolderUpdated(FolderUpdatedEvent event) {
        currentFolder = event.getFolder();
      }
    });
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler());
  }

  @Override
  public void uploadFile(String fileName) {

    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        submitFile();
      }
    };

    if("".equals(fileName)) {
      fireEvent(NotificationEvent.newBuilder().error(translations.fileMustBeSelected()).build());
    } else if(fileExist(fileName)) {
      fireEvent(ConfirmationRequiredEvent
          .createWithKeys(actionRequiringConfirmation, "replaceExistingFile", "confirmReplaceExistingFile"));
    } else {
      submitFile();
    }
  }

  @Override
  public void submit() {
    getView().hideDialog();
    getEventBus().fireEvent(new FileUploadedEvent());
  }

  @Override
  public void onReveal() {
    if(currentFolder != null) {
      String folderName = currentFolder.getName();
      getView().setRemoteFolderName("root".equals(folderName) ? translations.fileSystemLabel() : folderName);
    }
  }

  private class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) &&
          event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }

  private boolean fileExist(String fileName) {
    if(currentFolder == null) return false;

    // OPAL-1075 Chrome prefixes the file name with C:\fakepath\
    int sep = fileName.lastIndexOf("\\");
    String name = fileName;
    if(sep != -1) {
      name = fileName.substring(sep + 1);
    }
    JsArray<FileDto> filesInCurrentDirectory = currentFolder.getChildrenArray();
    if(filesInCurrentDirectory != null) {
      for(int i = 0; i < filesInCurrentDirectory.length(); i++) {
        if(name.equals(filesInCurrentDirectory.get(i).getName())) {
          return true;
        }
      }
    }

    return false;
  }

  private void submitFile() {
    getView().submit(urlBuilder.buildAbsoluteUrl("/files" + currentFolder.getPath()));
  }

}
