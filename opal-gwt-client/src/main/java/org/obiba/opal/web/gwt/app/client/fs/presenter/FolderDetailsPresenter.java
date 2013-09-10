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
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileUploadedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderCreationEvent;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FileDto;
import org.obiba.opal.web.model.client.opal.FileDto.FileType;

import com.google.gwt.http.client.Request;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
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

  @Inject
  public FolderDetailsPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {

    getView().addFileSelectionHandler(new FileSelectionHandler() {

      @Override
      public void onFileSelection(FileDto fileDto) {
        if(!fileDto.getType().isFileType(FileType.FILE) && fileDto.getReadable()) {
          getEventBus().fireEvent(new FolderSelectionChangeEvent(fileDto));
          updateTable(fileDto);
        }
      }
    });

    registerHandler(getView().getTableSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        FileDto selectedFile = getView().getTableSelectionModel().getSelectedObject();
        if(selectedFile != null) {
          getEventBus().fireEvent(new FileSelectionChangeEvent(selectedFile));
        }
      }

    }));

    registerHandler(
        getEventBus().addHandler(FileSelectionChangeEvent.getType(), new FileSelectionChangeEvent.Handler() {

          @Override
          public void onFileSelectionChange(FileSelectionChangeEvent event) {
            updateTable(event.getFile());
          }

        }));

    registerHandler(getEventBus().addHandler(FileUploadedEvent.getType(), new FileUploadedEvent.Handler() {

      @Override
      public void onFileUploaded(FileUploadedEvent event) {
        // Refresh the current folder since a new file was probably added to it.
        updateTable(currentFolder);
      }
    }));

    registerHandler(getEventBus().addHandler(FolderCreationEvent.getType(), new FolderCreationEvent.Handler() {

      @Override
      public void onFolderCreation(FolderCreationEvent event) {
        getEventBus().fireEvent(new FolderSelectionChangeEvent(event.getFolder()));
        updateTable(event.getFolder());
      }
    }));
  }

  @Override
  protected void onReveal() {
    if(currentFolder != null) getAndUpdateTable(currentFolder);
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

  private void updateTable(final FileDto file) {
    currentFolder = file;
    if(!isVisible()) return;
    getAndUpdateTable(file);
  }

  private void getAndUpdateTable(final FileDto file) {
    getView().clearSelection();
    FileResourceRequest.newBuilder(file.getPath()).withCallback(new ResourceCallback<FileDto>() {
      @Override
      public void onResource(Response response, FileDto resource) {
        currentFolder = resource;
        getView().renderRows(resource);
        fireEvent(new FolderSelectionChangeEvent(currentFolder));
      }
    }).withCallback(new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_NOT_FOUND) {
          fireEvent(NotificationEvent.newBuilder().error("FileNotFound").args(file.getPath()).build());
        } else {
          fireEvent(NotificationEvent.newBuilder().error("FileNotAccessible").args(file.getPath()).build());
        }
      }
    }, Response.SC_NOT_FOUND, Response.SC_UNAUTHORIZED, Response.SC_INTERNAL_SERVER_ERROR).send();

  }
}
