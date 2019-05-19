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

import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDeletedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileRenameRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FilesCheckedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderRefreshEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.presenter.SplitPaneWorkbenchPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class FileExplorerPresenter extends PresenterWidget<FileExplorerPresenter.Display>
    implements FileExplorerUiHandlers {

  private enum FileAction {
    COPY, MOVE
  }

  private final FilePathPresenter filePathPresenter;

  private final FilePlacesPresenter filePlacesPresenter;

  private final FolderDetailsPresenter folderDetailsPresenter;

  private final TranslationMessages translationMessages;

  private final ModalProvider<FileUploadModalPresenter> fileUploadModalProvider;

  private final ModalProvider<CreateFolderModalPresenter> createFolderModalProvider;

  private final ModalProvider<EncryptDownloadModalPresenter> encryptDownloadModalProvider;

  private final ModalProvider<RenameModalPresenter> renameModalPresenterModalProvider;

  private Runnable actionRequiringConfirmation;

  private List<FileDto> checkedFiles;

  private List<FileDto> filesClipboard;

  private FileAction currentAction;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public FileExplorerPresenter(Display display, EventBus eventBus, FilePathPresenter filePathPresenter,
      FilePlacesPresenter filePlacesPresenter, FolderDetailsPresenter folderDetailsPresenter,
      ModalProvider<FileUploadModalPresenter> fileUploadModalProvider,
      ModalProvider<CreateFolderModalPresenter> createFolderModalProvider, TranslationMessages translationMessages,
      ModalProvider<EncryptDownloadModalPresenter> encryptDownloadModalProvider,
      ModalProvider<RenameModalPresenter> renameModalPresenterModalProvider) {
    super(eventBus, display);
    this.filePathPresenter = filePathPresenter;
    this.filePlacesPresenter = filePlacesPresenter;
    this.folderDetailsPresenter = folderDetailsPresenter;
    this.translationMessages = translationMessages;
    this.fileUploadModalProvider = fileUploadModalProvider.setContainer(this);
    this.createFolderModalProvider = createFolderModalProvider.setContainer(this);
    this.encryptDownloadModalProvider = encryptDownloadModalProvider.setContainer(this);
    this.renameModalPresenterModalProvider = renameModalPresenterModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  public void showProject(String project) {
    filePlacesPresenter.showProject(project);
  }

  public void reset() {
    folderDetailsPresenter.setCurrentFolder(null);
  }

  @Override
  protected void onBind() {
    super.onBind();
    addEventHandlers();
    for(SplitPaneWorkbenchPresenter.Slot slot : SplitPaneWorkbenchPresenter.Slot.values()) {
      setInSlot(slot, getDefaultPresenter(slot));
    }
  }

  protected PresenterWidget<?> getDefaultPresenter(SplitPaneWorkbenchPresenter.Slot slot) {
    switch(slot) {
      case TOP:
        return filePathPresenter;
      case CENTER:
        return folderDetailsPresenter;
      case LEFT:
        return filePlacesPresenter;
    }
    return null;
  }

  private void addEventHandlers() {
    addRegisteredHandler(FolderUpdatedEvent.getType(), new FolderUpdatedEvent.FolderUpdatedHandler() {

      @Override
      public void onFolderUpdated(FolderUpdatedEvent event) {
        checkedFiles = null;
        updateCurrentFolderAuthorizations();
        updateCheckedFilesAuthorizations();
        updateCheckedFilesRenameAuthorization();
      }

      private void updateCurrentFolderAuthorizations() {
        // create folder and upload
        ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/files" + getCurrentFolder().getPath())
            .post()//
            .authorize(
                new CompositeAuthorizer(getView().getCreateFolderAuthorizer(), getView().getFileUploadAuthorizer()))
            .send();

        updateCurrentFolderPasteAuthorization();
      }
    });

    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler());

    addRegisteredHandler(FilesCheckedEvent.getType(), new FilesCheckedEvent.FilesCheckedHandler() {
      @Override
      public void onFilesChecked(FilesCheckedEvent event) {
        checkedFiles = null;
        if(!isVisible()) return;
        checkedFiles = event.getCheckedFiles();
        updateCheckedFilesAuthorizations();
      }
    });

    addRegisteredHandler(FileRenameRequestEvent.getType(), new FileRenameRequestEvent.FileRenameRequestHandler() {

      private FileDto findFolderWithSameName(FileDto folder, String name) {
        JsArray<FileDto> files = folder.getChildrenArray();
        for(int i = 0; i < files.length(); i++) {
          FileDto f = files.get(i);
          if(!FileDto.FileType.FILE.isFileType(f.getType()) && name.equals(f.getName())) {
            return f;
          }
        }

        return null;
      }

      @Override
      public void onFileRenameRequest(FileRenameRequestEvent event) {
        final FileDto folder = event.getFolder();
        FileDto file = event.getFile();
        String newName = event.getName();
        final FileDto folderWithSameName = findFolderWithSameName(folder, newName);
        String requestUrl = "/files" + folder.getPath() + "/" +
            (folderWithSameName == null ? newName : folderWithSameName.getName());

        ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == Response.SC_OK) {
              getEventBus().fireEvent(new FolderRefreshEvent(folderWithSameName == null ? folder : folderWithSameName));
            } else {
              getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
            }
          }
        };

        UriBuilder uriBuilder = UriBuilder.create().fromPath(requestUrl);
        uriBuilder.query("action", FileAction.MOVE.toString().toLowerCase());
        uriBuilder.query("file", URL.encodePathSegment(file.getPath()));
        ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build()).put()
            .withCallback(Response.SC_OK, callbackHandler)
            .send();
      }
    });
  }

  private FileDto getCurrentFolder() {
    return folderDetailsPresenter.getCurrentFolder();
  }

  private void updateCheckedFilesAuthorizations() {
    if(hasCheckedFiles()) {
      updateCheckedFilesDownloadAuthorization();
      updateCheckedFilesCopyAuthorization();
      updateCheckedFilesCutAuthorization();
      updateCheckedFilesRenameAuthorization();
      updateCheckedFilesDeleteAuthorization();
    } else {
      getView().getFileDownloadAuthorizer().unauthorized();
      getView().getFileDeleteAuthorizer().unauthorized();
      getView().getFileCopyAuthorizer().unauthorized();
      getView().getFileCutAuthorizer().unauthorized();
    }
  }

  /**
   * Authorize download if all that is selected can be downloaded.
   */
  private void updateCheckedFilesDownloadAuthorization() {
    if(!hasCheckedFiles()) return;

    boolean allReadable = true;
    for(FileDto file : checkedFiles) {
      if(!allReadable) break;
      allReadable = file.getReadable();
    }
    if(allReadable) {
      getView().getFileDownloadAuthorizer().authorized();
    } else {
      getView().getFileDownloadAuthorizer().unauthorized();
    }
  }

  /**
   * Authorize copy if all that is selected can be downloaded.
   */
  private void updateCheckedFilesCopyAuthorization() {
    if(!hasCheckedFiles()) return;

    boolean allReadable = true;
    for(FileDto file : checkedFiles) {
      if(!allReadable) break;
      allReadable = file.getReadable();
    }
    if(allReadable) {
      getView().getFileCopyAuthorizer().authorized();
    } else {
      getView().getFileCopyAuthorizer().unauthorized();
    }
  }

  /**
   * Authorize cut if all that is selected can be downloaded and deleted.
   */
  private void updateCheckedFilesCutAuthorization() {
    if(!hasCheckedFiles()) return;

    boolean allWritable = true;
    for(FileDto file : checkedFiles) {
      if(!allWritable) break;
      allWritable = file.getReadable() && file.getWritable();
    }
    if(allWritable) {
      getView().getFileCutAuthorizer().authorized();
    } else {
      getView().getFileCutAuthorizer().unauthorized();
    }
  }

  /**
   * Authorize paste if current folder is writable.
   */
  private void updateCurrentFolderPasteAuthorization() {
    boolean authorized = hasFilesInClipboard() && getCurrentFolder().getWritable();
    // destination must be writable and cannot be the same as the source
    if(authorized && getCurrentFolder().getPath().equals(FileDtos.getParent(filesClipboard.get(0)).getPath())) {
      authorized = false;
    }

    // cannot paste in a children
    if(authorized) {
      for(FileDto file : filesClipboard) {
        if(FileDtos.isFolder(file) && getCurrentFolder().getPath().startsWith(file.getPath())) {
          authorized = false;
          break;
        }
      }
    }

    if(authorized) {
      getView().getFilePasteAuthorizer().authorized();
    } else {
      getView().getFilePasteAuthorizer().unauthorized();
    }
  }

  /**
   * Authorize delete if all that is selected can be deleted.
   */
  private void updateCheckedFilesRenameAuthorization() {
    if(!hasCheckedFiles()) getView().getFileRenameAuthorizer().unauthorized();

    if(checkedFiles.size() == 1 && checkedFiles.get(0).getWritable()) {
      getView().getFileRenameAuthorizer().authorized();
    } else {
      getView().getFileRenameAuthorizer().unauthorized();
    }
  }

  /**
   * Authorize delete if all that is selected can be deleted.
   */
  private void updateCheckedFilesDeleteAuthorization() {
    if(!hasCheckedFiles()) return;

    boolean allWritable = true;
    for(FileDto file : checkedFiles) {
      if(!allWritable) break;
      allWritable = file.getWritable();
    }
    if(allWritable) {
      getView().getFileDeleteAuthorizer().authorized();
    } else {
      getView().getFileDeleteAuthorizer().unauthorized();
    }
  }

  class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) &&
          event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }

  @Override
  public void onAddFolder() {
    createFolderModalProvider.get().setCurrentFolder(getCurrentFolder());
  }

  @Override
  public void onUploadFile() {
    fileUploadModalProvider.get().setCurrentFolder(getCurrentFolder());
  }

  @Override
  public void onDelete() {
    if(!hasCheckedFiles()) return;

    // We are either deleting a file or a folder
    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        for(FileDto fileToDelete : checkedFiles) {
          deleteFile(fileToDelete);
        }
      }

      private void deleteFile(final FileDto file) {
        ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(ConfirmationTerminatedEvent.create());
            if(response.getStatusCode() == Response.SC_OK) {
              getEventBus().fireEvent(new FileDeletedEvent(file));
            } else {
              getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
            }
          }
        };

        ResourceRequestBuilderFactory.newBuilder().forResource("/files" + file.getPath()).delete()
            .withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_FORBIDDEN, callbackHandler)
            .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler)
            .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
      }
    };

    getEventBus().fireEvent(ConfirmationRequiredEvent
        .createWithMessages(actionRequiringConfirmation, translationMessages.removeFile(),
            translationMessages.confirmDeleteFile()));
  }

  @Override
  public void onDownload() {
    if(!hasCheckedFiles()) return;
    encryptDownloadModalProvider.get().setFiles(checkedFiles);
  }

  @Override
  public void onCopy() {
    filesClipboard = checkedFiles;
    currentAction = FileAction.COPY;
    getView().showFilesInClipboard(filesClipboard);
  }


  @Override
  public void onRename() {
    if(!hasCheckedFiles()) return;
    renameModalPresenterModalProvider.get().initialize(getCurrentFolder(), checkedFiles.get(0));
  }

  @Override
  public void onCut() {
    filesClipboard = checkedFiles;
    currentAction = FileAction.MOVE;
    getView().showFilesInClipboard(filesClipboard);
  }

  @Override
  public void onPaste() {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_OK) {
          fireEvent(new FolderRequestEvent(getCurrentFolder()));
        }
        filesClipboard = null;
        currentAction = null;
        updateCurrentFolderPasteAuthorization();
        getView().showFilesInClipboard(filesClipboard);
      }
    };

    UriBuilder uriBuilder = UriBuilder.create().fromPath(FileDtos.getLink(getCurrentFolder()));
    for(FileDto child : filesClipboard) {
      uriBuilder.query("file", child.getPath());
    }
    uriBuilder.query("action", currentAction.toString().toLowerCase());
    ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build()).put()
        .withCallback(Response.SC_OK, callbackHandler)
        .send();
  }

  private boolean hasCheckedFiles() {
    return checkedFiles != null && !checkedFiles.isEmpty();
  }

  private boolean hasFilesInClipboard() {
    return filesClipboard != null && !filesClipboard.isEmpty();
  }

  public interface Display extends View, HasUiHandlers<FileExplorerUiHandlers> {

    HasAuthorization getCreateFolderAuthorizer();

    HasAuthorization getFileUploadAuthorizer();

    HasAuthorization getFileDownloadAuthorizer();

    HasAuthorization getFileRenameAuthorizer();

    HasAuthorization getFileDeleteAuthorizer();

    HasAuthorization getFileCopyAuthorizer();

    HasAuthorization getFileCutAuthorizer();

    HasAuthorization getFilePasteAuthorizer();

    void showFilesInClipboard(List<FileDto> filesClipboard);
  }
}