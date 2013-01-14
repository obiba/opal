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

import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSystemTreeFolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileUploadedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderRefreshedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.FolderCreationEvent;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.model.client.opal.FileDto;
import org.obiba.opal.web.model.client.opal.FileDto.FileType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class FolderDetailsPresenter extends PresenterWidget<FolderDetailsPresenter.Display> {

  public interface Display extends View {

    void setDisplaysFiles(boolean include);

    void clearSelection();

    void renderRows(FileDto rows);

    HandlerRegistration addFileSelectionHandler(FileSelectionHandler fileSelectionHandler);

    SingleSelectionModel<FileDto> getTableSelectionModel();

  }

  public interface FileSelectionHandler {

    void onFileSelection(FileDto fileDto);
  }

  /**
   * The folder currently being displayed. This is null until a request to the server succeeds (see {@code updateTable})
   * after which, this attribute should never be null.
   */
  private FileDto currentFolder;

  private final RequestCredentials credentials;

  @Inject
  public FolderDetailsPresenter(Display display, EventBus eventBus, RequestCredentials credentials) {
    super(eventBus, display);
    this.credentials = credentials;
  }

  @Override
  protected void onBind() {

    super.getView().addFileSelectionHandler(new FileSelectionHandler() {

      public void onFileSelection(FileDto fileDto) {
        if(fileDto.getType().isFileType(FileType.FILE) == false && fileDto.getReadable()) {
          getEventBus().fireEvent(new FolderSelectionChangeEvent(fileDto));
          updateTable(fileDto.getPath());
        }
      }
    });

    super.registerHandler(
        getView().getTableSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

          @Override
          public void onSelectionChange(SelectionChangeEvent event) {
            FileDto selectedFile = getView().getTableSelectionModel().getSelectedObject();
            if(selectedFile != null) {
              getEventBus().fireEvent(new FileSelectionChangeEvent(selectedFile));
            }
          }

        }));

    super.registerHandler(getEventBus().addHandler(FileSystemTreeFolderSelectionChangeEvent.getType(),
        new FileSystemTreeFolderSelectionChangeEvent.Handler() {

          public void onFolderSelectionChange(FileSystemTreeFolderSelectionChangeEvent event) {
            updateTable(event.getFolder().getPath());
          }
        }));

    super.registerHandler(getEventBus().addHandler(FileUploadedEvent.getType(), new FileUploadedEvent.Handler() {

      public void onFileUploaded(FileUploadedEvent event) {
        // Refresh the current folder since a new file was probably added to it.
        updateTable(currentFolder.getPath());
      }
    }));

    super.registerHandler(getEventBus().addHandler(FolderCreationEvent.getType(), new FolderCreationEvent.Handler() {

      public void onFolderCreation(FolderCreationEvent event) {
        getEventBus().fireEvent(new FolderSelectionChangeEvent(event.getFolder()));
        updateTable(event.getFolder().getPath());
      }
    }));
  }

  @Override
  public void onReveal() {
    if(currentFolder != null) {
      updateTable(currentFolder.getPath());
    } else {
      updateTable(getDefaultPath());
    }
  }

  @Override
  public void onReset() {
    updateTable(getDefaultPath());
  }

  private String getDefaultPath() {
    if(credentials.getUsername() != null) {
      return "/home/" + credentials.getUsername();
    } else {
      return "/";
    }
  }

  public FileDto getCurrentFolder() {
    return currentFolder;
  }

  public boolean hasSelection() {
    return getView().getTableSelectionModel().getSelectedObject() != null;
  }

  public FileDto getSelectedFile() {
    return getView().getTableSelectionModel().getSelectedObject();
  }

  private void updateTable(final String path) {
    getView().clearSelection();

    FileResourceRequest.newBuilder(getEventBus()).path(path).withCallback(new ResourceCallback<FileDto>() {
      @Override
      public void onResource(Response response, FileDto file) {
        currentFolder = file;
        getView().renderRows(file);
        getEventBus().fireEvent(new FolderRefreshedEvent(currentFolder));
      }
    }).send();

  }
}
