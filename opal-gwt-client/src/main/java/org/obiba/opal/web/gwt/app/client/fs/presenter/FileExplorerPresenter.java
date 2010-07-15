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

import java.util.Arrays;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.UserMessageEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSystemTreeFolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderRefreshedEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter.FileSelectionHandler;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter.MessageDialogType;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FileDto;
import org.obiba.opal.web.model.client.opal.FileDto.FileType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
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

  }

  @Inject
  Provider<FileSystemTreePresenter> fileSystemTreePresenterProvider;

  @Inject
  Provider<FolderDetailsPresenter> folderDetailsPresenterProvider;

  @Inject
  Provider<FileUploadDialogPresenter> fileUploadDialogPresenterProvider;

  FileSystemTreePresenter fileSystemTreePresenter;

  FolderDetailsPresenter folderDetailsPresenter;

  FileUploadDialogPresenter fileUploadDialogPresenter;

  @Inject
  CreateFolderDialogPresenter createFolderDialogPresenter;

  FileDto currentFolder;

  private FileDto selectedFile;

  @Inject
  public FileExplorerPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
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
  }

  @Override
  public void refreshDisplay() {
    // fileSystemTreePresenter.refreshDisplay();
    folderDetailsPresenter.refreshDisplay();
  }

  @Override
  public void revealDisplay() {
    fileSystemTreePresenter.revealDisplay();
  }

  protected void initDisplayComponents() {

    fileSystemTreePresenter = fileSystemTreePresenterProvider.get();

    folderDetailsPresenter = folderDetailsPresenterProvider.get();
    folderDetailsPresenter.getDisplay().addFileSelectionHandler(createFileSelectionHandler());
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
        if(selectedFile == null) {
          eventBus.fireEvent(new UserMessageEvent(MessageDialogType.ERROR, "fileMustBeSelected", null));
        } else {
          boolean confirm = Window.confirm("Confirm deletion of " + selectedFile.getPath() + " ?");
          if(confirm) {
            deleteFile(selectedFile);
          }
        }
      }
    }));

    super.registerHandler(getDisplay().getFileDownloadButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        downloadFile(selectedFile);
      }
    }));

    super.registerHandler(getDisplay().getCreateFolderButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        createFolderDialogPresenter.setCurrentFolder(currentFolder);
        createFolderDialogPresenter.bind();
        createFolderDialogPresenter.revealDisplay();
      }
    }));

    super.registerHandler(getDisplay().getFileUploadButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        fileUploadDialogPresenter = fileUploadDialogPresenterProvider.get();
        fileUploadDialogPresenter.setCurrentFolder(currentFolder);
        fileUploadDialogPresenter.bind();
        fileUploadDialogPresenter.revealDisplay();
      }
    }));

    super.registerHandler(eventBus.addHandler(FileSelectionChangeEvent.getType(), new FileSelectionChangeEvent.Handler() {

      @Override
      public void onFileSelectionChange(FileSelectionChangeEvent event) {
        selectedFile = event.getFile();
      }
    }));

    super.registerHandler(eventBus.addHandler(FileSystemTreeFolderSelectionChangeEvent.getType(), new FileSystemTreeFolderSelectionChangeEvent.Handler() {

      @Override
      public void onFolderSelectionChange(FileSystemTreeFolderSelectionChangeEvent event) {
        currentFolder = event.getFolder();
      }

    }));

    super.registerHandler(eventBus.addHandler(FolderRefreshedEvent.getType(), new FolderRefreshedEvent.Handler() {

      @Override
      public void onFolderRefreshed(FolderRefreshedEvent event) {
        currentFolder = event.getFolder();
      }

    }));

  }

  private FileSelectionHandler createFileSelectionHandler() {
    return new FileSelectionHandler() {

      public void onFileSelection(FileDto fileDto) {
        if(fileDto.getType().isFileType(FileType.FILE)) {
          downloadFile(fileDto);
        }
      }
    };
  }

  private void deleteFile(final FileDto file) {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        GWT.log("Deleting " + file.getPath());
        if(response.getStatusCode() != Response.SC_OK) {
          GWT.log("Error code= " + response.getStatusCode() + " " + response.getText());
          eventBus.fireEvent(new UserMessageEvent(MessageDialogType.ERROR, response.getText(), Arrays.asList(new String[] { String.valueOf(file.getPath()) })));
        } else {
          selectedFile = null;
          refreshDisplay();
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
