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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDeletedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSystemTreeFolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderRefreshedEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class FileExplorerPresenter extends WidgetPresenter<FileExplorerPresenter.Display> {

  public interface Display extends WidgetDisplay {
    public HasWidgets getFileSystemTree();

    public HasWidgets getFolderDetailsPanel();

    public HasClickHandlers getFileUploadButton();

    public HasClickHandlers getFileDeleteButton();

    public HasClickHandlers getFileDownloadButton();

    public HasClickHandlers getCreateFolderButton();

    public void setEnabledFileDeleteButton(boolean enabled);

    public HasAuthorization getCreateFolderAuthorizer();

    public HasAuthorization getFileUploadAuthorizer();

    public HasAuthorization getFileDownloadAuthorizer();

    public HasAuthorization getFileDeleteAuthorizer();

  }

  @Inject
  Provider<FileUploadDialogPresenter> fileUploadDialogPresenterProvider;

  FileSystemTreePresenter fileSystemTreePresenter;

  FolderDetailsPresenter folderDetailsPresenter;

  FileUploadDialogPresenter fileUploadDialogPresenter;

  CreateFolderDialogPresenter createFolderDialogPresenter;

  private Runnable actionRequiringConfirmation;

  @Inject
  public FileExplorerPresenter(Display display, EventBus eventBus, FileSystemTreePresenter fileSystemTreePresenter, FolderDetailsPresenter folderDetailsPresenter, FileUploadDialogPresenter fileUploadDialogPresenter, CreateFolderDialogPresenter createFolderDialogPresenter) {
    super(display, eventBus);
    this.fileSystemTreePresenter = fileSystemTreePresenter;
    this.folderDetailsPresenter = folderDetailsPresenter;
    this.fileUploadDialogPresenter = fileUploadDialogPresenter;
    this.createFolderDialogPresenter = createFolderDialogPresenter;
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    initDisplayComponents();
    addEventHandlers();
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
    getDisplay().getFileSystemTree().remove(fileSystemTreePresenter.getDisplay().asWidget());
    getDisplay().getFolderDetailsPanel().remove(folderDetailsPresenter.getDisplay().asWidget());
    folderDetailsPresenter.unbind();
    fileSystemTreePresenter.unbind();
  }

  @Override
  public void refreshDisplay() {
    folderDetailsPresenter.refreshDisplay();
  }

  @Override
  public void revealDisplay() {
    fileSystemTreePresenter.revealDisplay();
    folderDetailsPresenter.revealDisplay();
  }

  private void authorizeFile(FileDto dto) {
    // download
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/files" + dto.getPath()).get().authorize(getDisplay().getFileDownloadAuthorizer()).send();
    // delete
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/files" + dto.getPath()).delete().authorize(getDisplay().getFileDeleteAuthorizer()).send();
  }

  private void authorizeFolder(FileDto dto) {
    // create folder and upload
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/files" + dto.getPath()).post()//
    .authorize(new CompositeAuthorizer(getDisplay().getCreateFolderAuthorizer(), getDisplay().getFileUploadAuthorizer())).send();

    if(!folderDetailsPresenter.hasSelection()) {
      // download
      ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/files" + dto.getPath()).get().authorize(getDisplay().getFileDownloadAuthorizer()).send();
      // delete
      setEnableFileDeleteButton();
    }
  }

  private void setEnableFileDeleteButton() {
    FileDto folder = folderDetailsPresenter.getCurrentFolder();
    if(folder.getPath().equals("/") || folder.getChildrenCount() > 0) {
      getDisplay().setEnabledFileDeleteButton(false);
    } else {
      ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/files" + folder.getPath()).delete().authorize(getDisplay().getFileDeleteAuthorizer()).send();
    }
  }

  protected void initDisplayComponents() {

    folderDetailsPresenter.getDisplay().setSelectionEnabled(true);

    getDisplay().getFileSystemTree().add(fileSystemTreePresenter.getDisplay().asWidget());
    getDisplay().getFolderDetailsPanel().add(folderDetailsPresenter.getDisplay().asWidget());

    fileSystemTreePresenter.bind();
    folderDetailsPresenter.bind();
  }

  private void addEventHandlers() {

    super.registerHandler(getDisplay().getFileDeleteButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // We are either deleting a file or a folder
        final FileDto fileToDelete = folderDetailsPresenter.hasSelection() ? folderDetailsPresenter.getSelectedFile() : folderDetailsPresenter.getCurrentFolder();
        actionRequiringConfirmation = new Runnable() {
          public void run() {
            deleteFile(fileToDelete);
          }
        };

        eventBus.fireEvent(new ConfirmationRequiredEvent(actionRequiringConfirmation, "deleteFile", "confirmDeleteFile"));
      }
    }));

    super.registerHandler(getDisplay().getFileDownloadButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        downloadFile(folderDetailsPresenter.getSelectedFile());
      }
    }));

    super.registerHandler(getDisplay().getCreateFolderButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        FileDto currentFolder = folderDetailsPresenter.getCurrentFolder();
        createFolderDialogPresenter.setCurrentFolder(currentFolder);
        createFolderDialogPresenter.bind();
        createFolderDialogPresenter.revealDisplay();
      }
    }));

    super.registerHandler(getDisplay().getFileUploadButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        FileDto currentFolder = folderDetailsPresenter.getCurrentFolder();
        fileUploadDialogPresenter = fileUploadDialogPresenterProvider.get();
        fileUploadDialogPresenter.setCurrentFolder(currentFolder);
        fileUploadDialogPresenter.bind();
        fileUploadDialogPresenter.revealDisplay();
      }
    }));

    super.registerHandler(eventBus.addHandler(FileSelectionChangeEvent.getType(), new FileSelectionChangeEvent.Handler() {

      @Override
      public void onFileSelectionChange(FileSelectionChangeEvent event) {
        GWT.log("file: " + event.getFile().getPath());
        getDisplay().setEnabledFileDeleteButton(folderDetailsPresenter.hasSelection());
        if(folderDetailsPresenter.hasSelection()) {
          authorizeFile(event.getFile());
        }
      }
    }));

    super.registerHandler(eventBus.addHandler(FileSystemTreeFolderSelectionChangeEvent.getType(), new FileSystemTreeFolderSelectionChangeEvent.Handler() {

      @Override
      public void onFolderSelectionChange(FileSystemTreeFolderSelectionChangeEvent event) {
        GWT.log("folder: " + event.getFolder().getPath());
        setEnableFileDeleteButton();
      }

    }));

    super.registerHandler(eventBus.addHandler(FolderRefreshedEvent.getType(), new FolderRefreshedEvent.Handler() {

      @Override
      public void onFolderRefreshed(FolderRefreshedEvent event) {
        GWT.log("refresh: " + event.getFolder().getPath());
        authorizeFolder(event.getFolder());
      }
    }));

    super.registerHandler(eventBus.addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));

  }

  class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    public void onConfirmation(ConfirmationEvent event) {
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) && event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }

  private void deleteFile(final FileDto file) {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() != Response.SC_OK) {
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, response.getText(), null));
        } else {
          eventBus.fireEvent(new FileDeletedEvent(file));
        }
      }
    };

    ResourceRequestBuilderFactory.newBuilder().forResource("/files" + file.getPath()).delete().withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_FORBIDDEN, callbackHandler).withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler).withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  private void downloadFile(final FileDto file) {
    String url = new StringBuilder(GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "")).append("ws/files").append(file.getPath()).toString();
    eventBus.fireEvent(new FileDownloadEvent(url));
  }
}
