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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionRequiredEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderCreationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
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

  public static final Object LEFT = new Object();

  public static final Object CENTER = new Object();

  FileSystemTreePresenter fileSystemTreePresenter;

  FolderDetailsPresenter folderDetailsPresenter;

  FileUploadDialogPresenter fileUploadDialogPresenter;

  private Object fileSelectionSource;

  private FileSelectionType fileSelectionType = FileSelectionType.FILE;

  private final List<SelectionResolver> selectionResolverChain;

  @Inject
  public FileSelectorPresenter(Display display, EventBus eventBus, FileSystemTreePresenter fileSystemTreePresenter,
      FolderDetailsPresenter folderDetailsPresenter, FileUploadDialogPresenter fileUploadDialogPresenter) {
    super(eventBus, display);

    this.fileSystemTreePresenter = fileSystemTreePresenter;
    this.folderDetailsPresenter = folderDetailsPresenter;
    this.fileUploadDialogPresenter = fileUploadDialogPresenter;

    selectionResolverChain = new ArrayList<SelectionResolver>();
    selectionResolverChain.add(new FileSelectionResolver());
    selectionResolverChain.add(new ExistingFileSelectionResolver());
    selectionResolverChain.add(new AnyFolderSelectionResolver());
    selectionResolverChain.add(new NewFileOrFolderSelectionResolver());
    selectionResolverChain.add(new ExistingFileOrFolderSelectionResolver());

    getView().setUiHandlers(this);
  }

  public void handle(FileSelectionRequiredEvent event) {
    setFileSelectionSource(event.getSource());
    setFileSelectionType(event.getFileSelectionType());
  }

  @Override
  protected void onBind() {
    setInSlot(LEFT, fileSystemTreePresenter);
    setInSlot(CENTER, folderDetailsPresenter);
  }

  @Override
  public void onReveal() {
    // Clear previous state.
    folderDetailsPresenter.getView().clearSelection(); // clear previous selection (highlighted row)
    getView().clearNewFileName(); // clear previous new file name
    getView().clearNewFolderName(); // clear previous new folder name

    // Adjust display based on file selection type.
    folderDetailsPresenter.getView().setDisplaysFiles(displaysFiles());
    getView().setNewFilePanelVisible(allowsFileCreation());
    getView().setNewFolderPanelVisible(allowsFolderCreation());
    getView().setDisplaysUploadFile(displaysFiles());
  }

  public void setFileSelectionSource(Object fileSelectionSource) {
    this.fileSelectionSource = fileSelectionSource;
  }

  public void setFileSelectionType(FileSelectionType fileSelectionType) {
    this.fileSelectionType = fileSelectionType;
  }

  public boolean displaysFiles() {
    return fileSelectionType == FileSelectionType.FILE ||
        fileSelectionType == FileSelectionType.EXISTING_FILE ||
        fileSelectionType == FileSelectionType.FILE_OR_FOLDER ||
        fileSelectionType == FileSelectionType.EXISTING_FILE_OR_FOLDER;
  }

  public boolean allowsFileCreation() {
    return fileSelectionType == FileSelectionType.FILE || fileSelectionType == FileSelectionType.FILE_OR_FOLDER;
  }

  public boolean allowsFolderCreation() {
    return fileSelectionType == FileSelectionType.FILE || fileSelectionType == FileSelectionType.FOLDER ||
        fileSelectionType == FileSelectionType.FILE_OR_FOLDER;
  }

  private void createFolder(String destination, String folder) {

    ResourceCallback<FileDto> createdCallback = new ResourceCallback<FileDto>() {

      @Override
      public void onResource(Response response, FileDto resource) {
        getEventBus().fireEvent(new FolderCreationEvent(resource));
        getView().clearNewFolderName();
      }
    };

    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }
    };

    ResourceRequestBuilderFactory.<FileDto>newBuilder().forResource("/files" + destination).post()
        .withBody("text/plain", folder).withCallback(createdCallback).withCallback(403, callbackHandler)
        .withCallback(500, callbackHandler).send();
  }

  private FileDto getCurrentFolder() {
    return folderDetailsPresenter.getCurrentFolder();
  }

  @Nullable
  public FileSelection getSelection() {
    FileSelection selection = null;

    for(SelectionResolver resolver : selectionResolverChain) {
      FileDto currentFolder = folderDetailsPresenter.getCurrentFolder();
      FileDto currentSelection = folderDetailsPresenter.getSelectedFile();

      resolver.resolveSelection(fileSelectionType, currentFolder.getPath(),
          currentSelection == null ? null : currentSelection.getPath(), getView().getNewFileName());
      if(resolver.resolved()) {
        selection = resolver.getSelection();
        break;
      }
    }

    return selection;
  }

  @Override
  public void uploadFile() {
    fileUploadDialogPresenter.setCurrentFolder(getCurrentFolder());
    addToPopupSlot(fileUploadDialogPresenter);
  }

  @Override
  public void selectFolder() {
    FileSelection selection = getSelection();
    if(selection != null) {
      getEventBus().fireEvent(new FileSelectionEvent(fileSelectionSource, selection));
    }
    getView().hideDialog();
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  @Override
  public void createFolder() {
    String newFolder = getView().getCreateFolderName().getText().trim();
    FileDto currentFolder = getCurrentFolder();
    if(currentFolder != null && newFolder.length() != 0) {
      if("/".equals(currentFolder.getPath())) { // create under root
        createFolder("/", newFolder);
      } else {
        createFolder(currentFolder.getPath(), newFolder);
      }
    }
  }

  public enum FileSelectionType {
    FILE, EXISTING_FILE, FOLDER, EXISTING_FOLDER, FILE_OR_FOLDER, EXISTING_FILE_OR_FOLDER
  }

  public interface Display extends PopupView, HasUiHandlers<FileSelectorUiHandlers> {

    void setDisplaysUploadFile(boolean displaysFiles);

    void hideDialog();

    void setNewFilePanelVisible(boolean visible);

    void setNewFolderPanelVisible(boolean visible);

    String getNewFileName();

    void clearNewFileName();

    HasText getCreateFolderName();

    void clearNewFolderName();
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

  interface SelectionResolver {

    void resolveSelection(FileSelectionType type, String selectedFolder, @Nullable String selectedFile,
        String newFileName);

    boolean resolved();

    FileSelection getSelection();
  }

  static abstract class AbstractSelectionResolver implements SelectionResolver {

    protected FileSelection selection;

    protected boolean resolved;

    @Override
    public boolean resolved() {
      return resolved;
    }

    @Override
    public FileSelection getSelection() {
      return selection;
    }

    public FileSelection getFileSelection(String selectedFolder, String selectedFile, String newFileName) {
      String selectionPath = null;

      if(newFileName != null && newFileName.trim().length() != 0) {
        selectionPath = ("/".equals(selectedFolder) ? selectedFolder : selectedFolder + "/") + newFileName;
      } else {
        selectionPath = selectedFile;
      }

      return new FileSelection(selectionPath, FileSelectionType.FILE);
    }
  }

  static class FileSelectionResolver extends AbstractSelectionResolver {

    @Override
    public void resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile,
        String newFileName) {
      resolved = false;
      if(type == FileSelectionType.FILE) {
        selection = getFileSelection(selectedFolder, selectedFile, newFileName);
        resolved = selection.getSelectionPath() != null;
      }
    }
  }

  static class ExistingFileSelectionResolver extends AbstractSelectionResolver {

    @Override
    public void resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile,
        String newFileName) {
      resolved = false;
      if(type == FileSelectionType.EXISTING_FILE) {
        selection = new FileSelection(selectedFile, FileSelectionType.FILE);
        resolved = selection.getSelectionPath() != null;
      }
    }
  }

  static class AnyFolderSelectionResolver extends AbstractSelectionResolver {

    @Override
    public void resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile,
        String newFileName) {
      resolved = false;
      if(type == FileSelectionType.FOLDER || type == FileSelectionType.EXISTING_FOLDER) {
        selection = new FileSelection(selectedFolder, FileSelectionType.FOLDER);
        resolved = selection.getSelectionPath() != null;
      }
    }
  }

  static class NewFileOrFolderSelectionResolver extends AbstractSelectionResolver {

    @Override
    public void resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile,
        String newFileName) {
      resolved = false;
      if(type == FileSelectionType.FILE_OR_FOLDER) {
        selection = getFileSelection(selectedFolder, selectedFile, newFileName);
        if(selection.getSelectionPath() == null) {
          selection = new FileSelection(selectedFolder, FileSelectionType.FOLDER);
        }
        resolved = selection.getSelectionPath() != null;
      }
    }
  }

  static class ExistingFileOrFolderSelectionResolver extends AbstractSelectionResolver {

    @Override
    public void resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile,
        String newFileName) {
      resolved = false;
      if(type == FileSelectionType.EXISTING_FILE_OR_FOLDER) {
        selection = selectedFile != null
            ? new FileSelection(selectedFile, FileSelectionType.FILE)
            : new FileSelection(selectedFolder, FileSelectionType.FOLDER);
        resolved = selection.getSelectionPath() != null;
      }
    }
  }
}
