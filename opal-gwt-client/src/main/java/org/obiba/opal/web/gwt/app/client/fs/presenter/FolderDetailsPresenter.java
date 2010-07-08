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

import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSystemTreeFolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileUploadedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.FolderCreationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.FileDto;
import org.obiba.opal.web.model.client.FileDto.FileType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionModel.SelectionChangeEvent;
import com.google.inject.Inject;

public class FolderDetailsPresenter extends WidgetPresenter<FolderDetailsPresenter.Display> {

  public interface Display extends WidgetDisplay {

    void setDisplaysFiles(boolean include);

    void setSelectionEnabled(boolean enabled);

    void clearSelection();

    void renderRows(FileDto rows);

    void addFileSelectionHandler(FileSelectionHandler fileSelectionHandler);

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
    super(display, eventBus);
  }

  @Override
  protected void onBind() {

    super.getDisplay().addFileSelectionHandler(new FileSelectionHandler() {

      public void onFileSelection(FileDto fileDto) {
        if(!fileDto.getType().isFileType(FileType.FILE)) {
          eventBus.fireEvent(new FolderSelectionChangeEvent(fileDto));
          updateTable(fileDto.getPath());
        }
      }
    });

    getDisplay().getTableSelectionModel().addSelectionChangeHandler(new SelectionModel.SelectionChangeHandler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        FileDto selectedFile = getDisplay().getTableSelectionModel().getSelectedObject();
        if(selectedFile != null) {
          GWT.log("firing event details file =" + selectedFile.getPath());
          eventBus.fireEvent(new FileSelectionChangeEvent(selectedFile));
        }
      }

    });

    super.registerHandler(eventBus.addHandler(FileSystemTreeFolderSelectionChangeEvent.getType(), new FileSystemTreeFolderSelectionChangeEvent.Handler() {

      public void onFolderSelectionChange(FileSystemTreeFolderSelectionChangeEvent event) {
        updateTable(event.getFolder().getPath());
      }
    }));

    super.registerHandler(eventBus.addHandler(FileUploadedEvent.getType(), new FileUploadedEvent.Handler() {

      public void onFileUploaded(FileUploadedEvent event) {
        // Refresh the current folder since a new file was probably added to it.
        refreshDisplay();
      }
    }));

    super.registerHandler(eventBus.addHandler(FolderCreationEvent.getType(), new FolderCreationEvent.Handler() {

      public void onFolderCreation(FolderCreationEvent event) {
        // Refresh the current folder since a new folder was added.
        refreshDisplay();
      }
    }));
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  public void refreshDisplay() {
    if(currentFolder != null) {
      updateTable(currentFolder.getPath());
    } else {
      updateTable("/");
    }
  }

  @Override
  public void revealDisplay() {
    updateTable("/");
  }

  private void updateTable(String path) {
    getDisplay().clearSelection();
    ResourceRequestBuilderFactory.<FileDto> newBuilder().forResource("/files/meta" + path).get().withCallback(new ResourceCallback<FileDto>() {
      @Override
      public void onResource(Response response, FileDto resource) {
        currentFolder = resource;
        getDisplay().renderRows(resource);
      }
    }).send();
  }
}
