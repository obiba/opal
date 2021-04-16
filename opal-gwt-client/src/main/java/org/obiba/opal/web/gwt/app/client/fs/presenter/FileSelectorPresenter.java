/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.fs.presenter;

import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FilesCheckedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderCreatedEvent;
import org.obiba.opal.web.gwt.app.client.fs.service.FileService;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.presenter.SplitPaneWorkbenchPresenter;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class FileSelectorPresenter extends ModalPresenterWidget<FileSelectorPresenter.Display>
    implements FileSelectorUiHandlers {

  private final RequestCredentials credentials;

  private final FilePathPresenter filePathPresenter;

  private final FilePlacesPresenter filePlacesPresenter;

  private final FolderDetailsPresenter folderDetailsPresenter;

  private final ModalProvider<FileUploadModalPresenter> fileUploadModalProvider;

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private final FileService fileService;

  private Object fileSelectionSource;

  private FileSelectionType fileSelectionType = FileSelectionType.FILE;

  private List<FileDto> checkedFiles;

  @Inject
  public FileSelectorPresenter(Display display, EventBus eventBus, FilePathPresenter filePathPresenter,
      FilePlacesPresenter filePlacesPresenter, FolderDetailsPresenter folderDetailsPresenter,
      ModalProvider<FileUploadModalPresenter> fileUploadModalProvider, RequestCredentials credentials,
      Translations translations, TranslationMessages translationMessages, FileService fileService) {
    super(eventBus, display);
    this.filePathPresenter = filePathPresenter;
    this.filePlacesPresenter = filePlacesPresenter;
    this.folderDetailsPresenter = folderDetailsPresenter;
    this.fileUploadModalProvider = fileUploadModalProvider.setContainer(this);
    this.credentials = credentials;
    this.translations = translations;
    this.translationMessages = translationMessages;
    this.fileService = fileService;
    getView().setUiHandlers(this);
  }

  public void handle(FileSelectionRequestEvent event) {
    setFileSelectionSource(event.getSource());
    setFileSelectionType(event.getFileSelectionType());
    showProject(event.getProject());
  }

  public void showProject(String project) {
    filePlacesPresenter.showProject(project);
  }

  public FileDto getCurrentFolder() {
    return folderDetailsPresenter.getCurrentFolder();
  }

  public List<FileDto> getCheckedFiles() {
    return checkedFiles;
  }

  public FileDto getSelectedFile() {
    return checkedFiles == null || getCheckedFiles().isEmpty() ? null : getCheckedFiles().get(0);
  }

  public void clearSelection() {
    checkedFiles = null;
    folderDetailsPresenter.getView().clearSelection();
  }

  @Override
  protected void onBind() {
    super.onBind();
    for(SplitPaneWorkbenchPresenter.Slot slot : SplitPaneWorkbenchPresenter.Slot.values()) {
      setInSlot(slot, getDefaultPresenter(slot));
    }
    addRegisteredHandler(FilesCheckedEvent.getType(), new FilesCheckedEvent.FilesCheckedHandler() {
      @Override
      public void onFilesChecked(FilesCheckedEvent event) {
        checkedFiles = event.getCheckedFiles();
      }
    });
    folderDetailsPresenter.setSingleSelectionModel(true);
  }

  protected PresenterWidget<?> getDefaultPresenter(SplitPaneWorkbenchPresenter.Slot slot) {
    switch(slot) {
      case TOP:
        return filePathPresenter;
      case CENTER:
        return folderDetailsPresenter;
      case LEFT:
        return filePlacesPresenter;
      default:
        return null;
    }
  }

  private void setDisplaysFiles(boolean displaysFiles) {
    folderDetailsPresenter.getView().setDisplaysFiles(displaysFiles);
  }

  @Override
  public void onReveal() {
    // Clear previous state.
    clearSelection(); // clear previous selection (highlighted row)
    getView().clearNewFolderName(); // clear previous new folder name

    // Adjust display based on file selection type.
    setDisplaysFiles(displaysFiles());

    FileDto lastFolder = fileService.getLastFolderDto();
    folderDetailsPresenter.setCurrentFolder(lastFolder == null ? FileDtos.user(credentials.getUsername()) : lastFolder);
  }

  public void setFileSelectionSource(Object fileSelectionSource) {
    this.fileSelectionSource = fileSelectionSource;
  }

  public void setFileSelectionType(FileSelectionType fileSelectionType) {
    this.fileSelectionType = fileSelectionType;
  }

  public boolean displaysFiles() {
    return fileSelectionType == FileSelectionType.FILE || fileSelectionType == FileSelectionType.FILE_OR_FOLDER;
  }

  private void createFolder(String destination, String folder) {

    ResourceCallback<FileDto> createdCallback = new ResourceCallback<FileDto>() {

      @Override
      public void onResource(Response response, FileDto resource) {
        fireEvent(new FolderCreatedEvent(resource));
        getView().clearNewFolderName();
      }
    };

    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }
    };

    ResourceRequestBuilderFactory.<FileDto>newBuilder().forResource("/files" + destination).post()
        .withBody("text/plain", folder).withCallback(createdCallback)
        .withCallback(Response.SC_FORBIDDEN, callbackHandler)
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler).send();
  }

  @Nullable
  public FileSelection getSelection() {
    FileDto selectedFile = getSelectedFile();
    if(selectedFile == null) {
      if (fileSelectionType == FileSelectionType.FOLDER) {
        selectedFile = getCurrentFolder();
        return new FileSelection(selectedFile.getPath(), fileSelectionType);
      }
      return null;
    }

    if(fileSelectionType == FileSelectionType.FILE_OR_FOLDER) {
      return new FileSelection(selectedFile.getPath(), fileSelectionType);
    }

    if(FileDtos.isFile(selectedFile) && fileSelectionType == FileSelectionType.FILE) {
      return new FileSelection(selectedFile.getPath(), fileSelectionType);
    }

    if(FileDtos.isFolder(selectedFile) && fileSelectionType == FileSelectionType.FOLDER) {
      return new FileSelection(selectedFile.getPath(), fileSelectionType);
    }

    return null;
  }

  @Override
  public void onUploadFile() {
    FileUploadModalPresenter fileUploadModalPresenter = fileUploadModalProvider.get();
    fileUploadModalPresenter.setCurrentFolder(folderDetailsPresenter.getCurrentFolder());
    fileUploadModalProvider.show();
  }

  @Override
  public void onSelect() {
    FileSelection selection = getSelection();
    if(selection == null) {
      getView().clearErrors();
      getView().showError(getNoSelectionErrorMessage());
    } else {
      fireEvent(new FileSelectionEvent(fileSelectionSource, selection));
      getView().hideDialog();
    }
  }

  @Override
  public void onCancel() {
    getView().hideDialog();
  }

  @Override
  public void onCreateFolder() {
    String newFolder = getView().getCreateFolderName().getText().trim();
    FileDto currentFolder = getCurrentFolder();
    if(currentFolder != null && !newFolder.isEmpty()) {
      createFolder(currentFolder.getPath(), newFolder);
    }
  }

  public String getNoSelectionErrorMessage() {
    if(folderDetailsPresenter.isSingleSelectionModel()) {
      return translationMessages.mustSelectFileFolder(translations.fileFolderTypeMap().get(fileSelectionType.name()));
    }

    return translationMessages
        .mustSelectAtLeastFileFolder(translations.fileFolderTypeMap().get(fileSelectionType.name()));
  }

  public enum FileSelectionType {
    FILE, FOLDER, FILE_OR_FOLDER
  }

  public interface Display extends PopupView, HasUiHandlers<FileSelectorUiHandlers> {

    void hideDialog();

    HasText getCreateFolderName();

    void clearNewFolderName();

    void clearErrors();

    void showError(String errorMessage);
  }

  public static class FileSelection {

    private final String selectionPath;

    private final FileSelectionType selectionType;

    public FileSelection(String selectionPath, FileSelectionType selectionType) {
      this.selectionPath = selectionPath;
      this.selectionType = selectionType;
    }

    public String getSelectionPath() {
      return selectionPath;
    }

    public FileSelectionType getSelectionType() {
      return selectionType;
    }
  }
}
