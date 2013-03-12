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
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.RequestUrlBuilder;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class FileUploadDialogPresenter extends PresenterWidget<FileUploadDialogPresenter.Display> {

  public interface Display extends PopupView {

    void hideDialog();

    HasClickHandlers getUploadButton();

    HasClickHandlers getCancelButton();

    String getFilename();

    HandlerRegistration addSubmitCompleteHandler(FormPanel.SubmitCompleteHandler handler);

    void submit(String url);

    HasText getRemoteFolderName();
  }

  private Translations translations;

  private FileDto currentFolder;

  private Runnable actionRequiringConfirmation;

  private final RequestUrlBuilder urlBuilder;

  @Inject
  public FileUploadDialogPresenter(Display display, EventBus eventBus, RequestUrlBuilder urlBuilder,
      Translations translations) {
    super(eventBus, display);
    this.translations = translations;
    this.urlBuilder = urlBuilder;
  }

  public void setCurrentFolder(FileDto currentFolder) {
    this.currentFolder = currentFolder;
  }

  @Override
  protected void onBind() {
    super.registerHandler(getView().getUploadButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        uploadFile();
      }
    }));

    super.registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));

    super.registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));

    super.registerHandler(getView().addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
      public void onSubmitComplete(SubmitCompleteEvent event) {
        getView().hideDialog();
        getEventBus().fireEvent(new FileUploadedEvent());
      }
    }));

  }

  @Override
  public void onReveal() {
    String folderName = currentFolder.getName();
    getView().getRemoteFolderName().setText(folderName.equals("root") ? translations.fileSystemLabel() : folderName);
  }

  class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    public void onConfirmation(ConfirmationEvent event) {
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) &&
          event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }

  private boolean fileExist(String fileName) {
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

  private void uploadFile() {

    actionRequiringConfirmation = new Runnable() {
      public void run() {
        submitFile();
      }
    };

    String fileName = getView().getFilename();
    if(fileName.equals("")) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error(translations.fileMustBeSelected()).build());
    } else if(fileExist(fileName)) {
      getEventBus().fireEvent(ConfirmationRequiredEvent
          .createWithKeys(actionRequiringConfirmation, "replaceExistingFile", "confirmReplaceExistingFile"));
    } else {
      submitFile();
    }

  }

  private void submitFile() {
    getView().submit(urlBuilder.buildAbsoluteUrl("/files" + currentFolder.getPath()));
  }

}
