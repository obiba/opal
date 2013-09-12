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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDeletedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderCreatedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileUploadedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FilesCheckedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderUpdatedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class FolderDetailsPresenter extends PresenterWidget<FolderDetailsPresenter.Display> implements FolderDetailsUiHandlers {

  /**
   * The folder currently being displayed. This is null until a request to the server succeeds (see {@code updateTable})
   * after which, this attribute should never be null.
   */
  private FileDto currentFolder;

  @Inject
  public FolderDetailsPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void onFilesChecked(List<FileDto> files) {
    fireEvent(new FilesCheckedEvent(files));
  }

  @Override
  public void onFolderSelection(FileDto fileDto) {
    if(fileDto.getReadable()) {
      updateTable(fileDto);
    }
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(FolderRequestEvent.getType(), new FolderRequestEvent.Handler() {

      @Override
      public void onFolderRequest(FolderRequestEvent event) {
        updateTable(event.getFile());
      }
    });

    addRegisteredHandler(FileUploadedEvent.getType(), new FileUploadedEvent.Handler() {

      @Override
      public void onFileUploaded(FileUploadedEvent event) {
        // Refresh the current folder
        updateTable(currentFolder);
      }
    });

    addRegisteredHandler(FolderCreatedEvent.getType(), new FolderCreatedEvent.Handler() {

      @Override
      public void onFolderCreated(FolderCreatedEvent event) {
        // Refresh the current folder
        updateTable(currentFolder);
      }
    });

    addRegisteredHandler(FileDeletedEvent.getType(), new FileDeletedEvent.Handler() {

      @Override
      public void onFileDeleted(FileDeletedEvent event) {
        // Refresh the current folder
        updateTable(currentFolder);
      }
    });
  }

  @Override
  protected void onReveal() {
    if(currentFolder != null) getAndUpdateTable(currentFolder);
  }

  public FileDto getCurrentFolder() {
    return currentFolder;
  }

  public void setCurrentFolder(FileDto currentFolder) {
    this.currentFolder = currentFolder;
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
        fireEvent(new FolderUpdatedEvent(currentFolder));
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

  public interface Display extends View, HasUiHandlers<FolderDetailsUiHandlers> {

    void setDisplaysFiles(boolean include);

    void clearSelection();

    void renderRows(FileDto rows);
  }

}
