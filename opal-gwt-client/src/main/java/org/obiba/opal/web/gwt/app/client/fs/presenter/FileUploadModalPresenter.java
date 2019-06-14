/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.fs.presenter;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileUploadedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.RequestUrlBuilder;
import org.obiba.opal.web.model.client.opal.FileDto;

import java.util.List;

public class FileUploadModalPresenter extends ModalPresenterWidget<FileUploadModalPresenter.Display>
    implements FileUploadModalUiHandlers {

  private final Translations translations;

  private FileDto currentFolder;

  private Runnable actionRequiringConfirmation;

  private final RequestUrlBuilder urlBuilder;

  private TranslationMessages translationMessages;

  @Inject
  public FileUploadModalPresenter(Display display, EventBus eventBus, RequestUrlBuilder urlBuilder,
                                  Translations translations, TranslationMessages translationMessages) {
    super(eventBus, display);
    this.translations = translations;
    this.urlBuilder = urlBuilder;
    this.translationMessages = translationMessages;
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
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler());
  }

  @Override
  public void uploadFiles(List<String> fileNames) {

    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        submitFiles();
      }
    };

    if (fileNames.isEmpty()) {
      fireEvent(NotificationEvent.newBuilder().error(translations.fileMustBeSelected()).build());
    } else {
      List<String> existingFileNames = Lists.newArrayList();
      for (String fileName : fileNames) {
        if (fileExist(fileName))
          existingFileNames.add(fileName);
      }

      if (!existingFileNames.isEmpty()) {
        String names = Joiner.on(", ").join(existingFileNames);
        fireEvent(ConfirmationRequiredEvent
            .createWithMessages(actionRequiringConfirmation, translationMessages.replaceExistingFile(),
                translationMessages.confirmReplaceExistingFile(names)));
      } else {
        submitFiles();
      }
    }
  }

  @Override
  public void submit() {
    getView().hideDialog();
    getEventBus().fireEvent(new FileUploadedEvent());
  }

  @Override
  public void onReveal() {
    showRemoteFolderName();
  }

  private void showRemoteFolderName() {
    if (currentFolder != null) {
      getView().setRemoteFolderName(currentFolder.getPath());
    }
  }

  public void setCurrentFolder(FileDto currentFolder) {
    this.currentFolder = currentFolder;
    showRemoteFolderName();
  }

  private class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if (actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) &&
          event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }

  private boolean fileExist(String fileName) {
    if (currentFolder == null) return false;

    // OPAL-1075 Chrome prefixes the file name with C:\fakepath\
    int sep = fileName.lastIndexOf("\\");
    String name = fileName;
    if (sep != -1) {
      name = fileName.substring(sep + 1);
    }
    JsArray<FileDto> filesInCurrentDirectory = currentFolder.getChildrenArray();
    if (filesInCurrentDirectory != null) {
      for (int i = 0; i < filesInCurrentDirectory.length(); i++) {
        if (name.equals(filesInCurrentDirectory.get(i).getName())) {
          return true;
        }
      }
    }

    return false;
  }

  private void submitFiles() {
    getView().submit(urlBuilder.buildAbsoluteUrl("/files" + currentFolder.getPath()));
    fireEvent(ConfirmationTerminatedEvent.create());
  }

  public interface Display extends PopupView, HasUiHandlers<FileUploadModalUiHandlers> {

    void hideDialog();

    void submit(String url);

    void setRemoteFolderName(String folderName);
  }

}
