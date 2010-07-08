/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.fs.event.FileSystemTreeFolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter.FileSelectionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.FolderCreationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.FileDto;
import org.obiba.opal.web.model.client.FileDto.FileType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;

/**
 *
 */
public class FileSelectorPresenter extends WidgetPresenter<FileSelectorPresenter.Display> {

  //
  // Instance Variables
  //

  FileSystemTreePresenter fileSystemTreePresenter;

  FolderDetailsPresenter folderDetailsPresenter;

  private Object fileSelectionSource;

  private FileSelectionType fileSelectionType = FileSelectionType.FILE;

  private String selectedFile;

  private String selectedFolder;

  //
  // Constructors
  //

  @Inject
  public FileSelectorPresenter(Display display, EventBus eventBus, FileSystemTreePresenter fileSystemTreePresenter, FolderDetailsPresenter folderDetailsPresenter) {
    super(display, eventBus);

    this.fileSystemTreePresenter = fileSystemTreePresenter;
    this.folderDetailsPresenter = folderDetailsPresenter;
    this.folderDetailsPresenter.getDisplay().setSelectionEnabled(true);

    getDisplay().setTreeDisplay(fileSystemTreePresenter.getDisplay());
    getDisplay().setDetailsDisplay(folderDetailsPresenter.getDisplay());

    folderDetailsPresenter.getDisplay().addFileSelectionHandler(new FileSelectionHandler() {

      public void onFileSelection(FileDto fileDto) {
        if(fileDto.getType().isFileType(FileType.FILE)) {
          selectedFile = fileDto.getPath();
        }
      }
    });
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    fileSystemTreePresenter.bind();
    folderDetailsPresenter.bind();

    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void revealDisplay() {
    // Clear previous state.
    folderDetailsPresenter.getDisplay().clearSelection(); // clear previous selection (highlighted row)
    getDisplay().clearNewFolderName(); // clear previous new folder name

    // Adjust display based on file selection type.
    getDisplay().setNewFilePanelVisible(allowsFileCreation());
    getDisplay().setNewFolderPanelVisible(allowsFolderCreation());

    getDisplay().showDialog();
  }

  @Override
  public void refreshDisplay() {
    fileSystemTreePresenter.refreshDisplay();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Methods
  //

  public void setFileSelectionSource(Object fileSelectionSource) {
    this.fileSelectionSource = fileSelectionSource;
  }

  public void setFileSelectionType(FileSelectionType fileSelectionType) {
    this.fileSelectionType = fileSelectionType;
  }

  public boolean allowsFileCreation() {
    return fileSelectionType.equals(FileSelectionType.FILE);
  }

  public boolean allowsFolderCreation() {
    return fileSelectionType.equals(FileSelectionType.FILE) || fileSelectionType.equals(FileSelectionType.FOLDER);
  }

  private void addEventHandlers() {
    addFileSelectionRequiredHandler(); // handler for file selection required
    addFolderSelectionHandler(); // handler for folder selected in FileSystemTree
    addSelectButtonHandler();
    addCreateFolderButtonHandler();
  }

  private void addFileSelectionRequiredHandler() {
    super.registerHandler(eventBus.addHandler(FileSelectionRequiredEvent.getType(), new FileSelectionRequiredEvent.Handler() {

      public void onFileSelectionRequired(FileSelectionRequiredEvent event) {
        selectedFile = selectedFolder = null; // clear previous selection
        setFileSelectionSource(event.getSource());
        setFileSelectionType(event.getFileSelectionType());
        refreshDisplay();
        revealDisplay();
      }
    }));
  }

  private void addFolderSelectionHandler() {
    super.registerHandler(eventBus.addHandler(FileSystemTreeFolderSelectionChangeEvent.getType(), new FileSystemTreeFolderSelectionChangeEvent.Handler() {

      public void onFolderSelectionChange(FileSystemTreeFolderSelectionChangeEvent event) {
        selectedFolder = event.getFolder().getPath();
      }
    }));
  }

  private void addCreateFolderButtonHandler() {
    super.registerHandler(getDisplay().addCreateFolderButtonHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        String newFolder = getDisplay().getCreateFolderName().getText();
        if(selectedFolder != null && newFolder.trim().length() != 0) {
          createFolder(selectedFolder + "/" + newFolder);
        }
      }
    }));
  }

  private void addSelectButtonHandler() {
    super.registerHandler(getDisplay().addSelectButtonHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        String selection = getSelection();
        if(selection != null) {
          System.out.println("DSPATHIS selection: [" + selection + "]");
          eventBus.fireEvent(new FileSelectionEvent(FileSelectorPresenter.this.fileSelectionSource, selection));
        }
        getDisplay().hideDialog();
      }
    }));
  }

  private void createFolder(final String folder) {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == 201) {
          eventBus.fireEvent(new FolderCreationEvent(folder));
        } else if(response.getStatusCode() == 403) {
          System.out.println("Folder creation failure (403)");
        } else if(response.getStatusCode() == 500) {
          System.out.println("Folder creation failure (500)");
        }
      }
    };

    ResourceRequestBuilderFactory.newBuilder().forResource("/files" + folder).put().withCallback(201, callbackHandler).withCallback(403, callbackHandler).withCallback(500, callbackHandler).send();
  }

  public String getSelection() {
    String selection = null;

    switch(fileSelectionType) {
    case FILE:
      String newFileName = getDisplay().getNewFileName().getText();
      if(newFileName != null && newFileName.trim().length() != 0) {
        selection = (!selectedFolder.equals("/") ? selectedFolder + "/" : selectedFolder) + newFileName;
      } else {
        selection = selectedFile;
      }
      break;
    case EXISTING_FILE:
      selection = selectedFile;
      break;
    case FOLDER:
    case EXISTING_FOLDER:
      selection = selectedFolder;
      break;
    }

    return selection;
  }

  //
  // Inner Classes / Interfaces
  //

  public enum FileSelectionType {
    FILE, EXISTING_FILE, FOLDER, EXISTING_FOLDER
  }

  public interface Display extends WidgetDisplay {

    void showDialog();

    void hideDialog();

    void setTreeDisplay(FileSystemTreePresenter.Display treeDisplay);

    void setDetailsDisplay(FolderDetailsPresenter.Display detailsDisplay);

    void setNewFilePanelVisible(boolean visible);

    void setNewFolderPanelVisible(boolean visible);

    void clearNewFolderName();

    HasWidgets getFileSystemTreePanel();

    HasWidgets getFolderDetailsPanel();

    HandlerRegistration addSelectButtonHandler(ClickHandler handler);

    HandlerRegistration addCreateFolderButtonHandler(ClickHandler handler);

    HasText getNewFileName();

    HasText getCreateFolderName();
  }
}
