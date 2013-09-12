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

import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDeletedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FilesCheckedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FilesDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.presenter.SplitPaneWorkbenchPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class FileExplorerPresenter extends PresenterWidget<FileExplorerPresenter.Display>
    implements FileExplorerUiHandlers {

  private final FilePathPresenter filePathPresenter;

  private final FilePlacesPresenter filePlacesPresenter;

  private final FolderDetailsPresenter folderDetailsPresenter;

  private final ModalProvider<FileUploadModalPresenter> fileUploadModalProvider;

  private final ModalProvider<CreateFolderModalPresenter> createFolderModalProvider;

  private Runnable actionRequiringConfirmation;

  private List<FileDto> checkedFiles;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public FileExplorerPresenter(Display display, EventBus eventBus, FilePathPresenter filePathPresenter,
      FilePlacesPresenter filePlacesPresenter, FolderDetailsPresenter folderDetailsPresenter,
      ModalProvider<FileUploadModalPresenter> fileUploadModalProvider,
      ModalProvider<CreateFolderModalPresenter> createFolderModalProvider) {
    super(eventBus, display);
    this.filePathPresenter = filePathPresenter;
    this.filePlacesPresenter = filePlacesPresenter;
    this.folderDetailsPresenter = folderDetailsPresenter;
    this.fileUploadModalProvider = fileUploadModalProvider.setContainer(this);
    this.createFolderModalProvider = createFolderModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  public void showProject(String project) {
    filePlacesPresenter.showProject(project);
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

  private void authorizeFolder(FileDto dto) {
    // create folder and upload
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/files" + dto.getPath()).post()//
        .authorize(new CompositeAuthorizer(getView().getCreateFolderAuthorizer(), getView().getFileUploadAuthorizer()))
        .send();
  }

  private void addEventHandlers() {
    addRegisteredHandler(FolderUpdatedEvent.getType(), new FolderUpdatedEvent.Handler() {

      @Override
      public void onFolderUpdated(FolderUpdatedEvent event) {
        authorizeFolder(event.getFolder());
        checkedFiles = null;
        updateCheckedFilesAuthorizations();
      }
    });

    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler());

    addRegisteredHandler(FilesCheckedEvent.getType(), new FilesCheckedEvent.Handler() {
      @Override
      public void onFilesChecked(FilesCheckedEvent event) {
        checkedFiles = null;
        if(!isVisible()) return;
        checkedFiles = event.getCheckedFiles();
        updateCheckedFilesAuthorizations();
      }
    });
  }

  private void updateCheckedFilesAuthorizations() {
    if(hasCheckedFiles()) {
      updateCheckedFilesDownloadAuthorization();
      updateCheckedFilesDeleteAuthorization();
    } else {
      getView().getFileDownloadAuthorizer().unauthorized();
      getView().getFileDeleteAuthorizer().unauthorized();
      getView().getFileCopyAuthorizer().unauthorized();
      getView().getFileCutAuthorizer().unauthorized();
      getView().getFilePasteAuthorizer().unauthorized();
    }
  }

  /**
   * Authorize download if all that is selected can be downloaded.
   */
  private void updateCheckedFilesDownloadAuthorization() {
    if (!hasCheckedFiles()) return;

    CascadingAuthorizer.Builder builder = CascadingAuthorizer.newBuilder();
    FileDto current = null;
    for(FileDto file : checkedFiles) {
      builder.and(ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/files" + file.getPath()).get()
          .authorize(getView().getFileDownloadAuthorizer()));
      if(current == null) current = FileDtos.getParent(file);
    }

    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/files" + current.getPath()).get()
        .authorize(builder.authorize(getView().getFileDownloadAuthorizer()).build()).send();
  }

  /**
   * Authorize delete if all that is selected can be deleted.
   */
  private void updateCheckedFilesDeleteAuthorization() {
    if (!hasCheckedFiles()) return;

    CascadingAuthorizer.Builder builder = CascadingAuthorizer.newBuilder();
    for(FileDto file : checkedFiles) {
      builder.and(ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/files" + file.getPath()).delete()
          .authorize(getView().getFileDeleteAuthorizer()));
    }

    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/files" + checkedFiles.get(0).getPath()).delete()
        .authorize(builder.authorize(getView().getFileDeleteAuthorizer()).build()).send();
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
    FileDto currentFolder = folderDetailsPresenter.getCurrentFolder();
    CreateFolderModalPresenter createFolderModal = createFolderModalProvider.get();
    createFolderModal.setCurrentFolder(currentFolder);
  }

  @Override
  public void onUploadFile() {
    FileDto currentFolder = folderDetailsPresenter.getCurrentFolder();
    FileUploadModalPresenter fileUploadModal = fileUploadModalProvider.get();
    fileUploadModal.setCurrentFolder(currentFolder);
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
            if(response.getStatusCode() != Response.SC_OK) {
              getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
            } else {
              getEventBus().fireEvent(new FileDeletedEvent(file));
            }
          }
        };

        ResourceRequestBuilderFactory.newBuilder().forResource("/files" + file.getPath()).delete()
            .withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_FORBIDDEN, callbackHandler)
            .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler)
            .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
      }
    };

    getEventBus().fireEvent(
        ConfirmationRequiredEvent.createWithKeys(actionRequiringConfirmation, "deleteFile", "confirmDeleteFile"));
  }

  @Override
  public void onDownload() {
    if(!hasCheckedFiles()) return;

    if(checkedFiles.size() == 1) {
      fireEvent(new FileDownloadRequestEvent(FileDtos.getLink(checkedFiles.get(0))));
    } else {
      fireEvent(new FilesDownloadRequestEvent(FileDtos.getParent(checkedFiles.get(0)), checkedFiles));
    }
  }

  @Override
  public void onCopy() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void onCut() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void onPaste() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  private boolean hasCheckedFiles() {
    return checkedFiles != null && checkedFiles.size() > 0;
  }

  public interface Display extends View, HasUiHandlers<FileExplorerUiHandlers> {

    void setEnabledFileDeleteButton(boolean enabled);

    HasAuthorization getCreateFolderAuthorizer();

    HasAuthorization getFileUploadAuthorizer();

    HasAuthorization getFileDownloadAuthorizer();

    HasAuthorization getFileDeleteAuthorizer();

    HasAuthorization getFileCopyAuthorizer();

    HasAuthorization getFileCutAuthorizer();

    HasAuthorization getFilePasteAuthorizer();

  }
}