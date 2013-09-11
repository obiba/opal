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
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileUploadedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderCreationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderSelectionChangeEvent;
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

  private List<FileDto> selectedFiles;

  @Inject
  public FolderDetailsPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void onFilesSelected(List<FileDto> files) {
    selectedFiles = files;
  }

  @Override
  public void onFolderSelection(FileDto fileDto) {
    if(fileDto.getReadable()) {
      getEventBus().fireEvent(new FolderSelectionChangeEvent(fileDto));
      updateTable(fileDto);
    }
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(FileSelectionChangeEvent.getType(), new FileSelectionChangeEvent.Handler() {

      @Override
      public void onFileSelectionChange(FileSelectionChangeEvent event) {
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

    addRegisteredHandler(FolderCreationEvent.getType(), new FolderCreationEvent.Handler() {

      @Override
      public void onFolderCreation(FolderCreationEvent event) {
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

  public boolean hasSelection() {
    return selectedFiles != null && selectedFiles.size() > 0;
  }

  public FileDto getSelectedFile() {
    return hasSelection() ? selectedFiles.get(0) : null;
  }

  private void updateTable(final FileDto file) {
    currentFolder = file;
    selectedFiles = null;
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

  public interface Display extends View, HasUiHandlers<FolderDetailsUiHandlers> {

    void setDisplaysFiles(boolean include);

    void clearSelection();

    void renderRows(FileDto rows);
  }

}
