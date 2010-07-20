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

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.fs.event.FileSystemTreeFolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter.FileSelectionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.FolderCreationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FileDto;
import org.obiba.opal.web.model.client.opal.FileDto.FileType;

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

  String selectedFile;

  String selectedFolder;

  private List<SelectionResolver> selectionResolverChain;

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

    selectionResolverChain = new ArrayList<SelectionResolver>();
    selectionResolverChain.add(new FileSelectionResolver());
    selectionResolverChain.add(new ExistingFileSelectionResolver());
    selectionResolverChain.add(new AnyFolderSelectionResolver());
    selectionResolverChain.add(new NewFileOrFolderSelectionResolver());
    selectionResolverChain.add(new ExistingFileOrFolderSelectionResolver());
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
    fileSystemTreePresenter.unbind();
    folderDetailsPresenter.unbind();
  }

  @Override
  public void revealDisplay() {
    fileSystemTreePresenter.revealDisplay();
    folderDetailsPresenter.revealDisplay();

    // Clear previous state.
    folderDetailsPresenter.getDisplay().clearSelection(); // clear previous selection (highlighted row)
    getDisplay().clearNewFileName(); // clear previous new file name
    getDisplay().clearNewFolderName(); // clear previous new folder name

    // Adjust display based on file selection type.
    folderDetailsPresenter.getDisplay().setDisplaysFiles(displaysFiles());
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

  public boolean displaysFiles() {
    return fileSelectionType.equals(FileSelectionType.FILE) || fileSelectionType.equals(FileSelectionType.EXISTING_FILE) || fileSelectionType.equals(FileSelectionType.FILE_OR_FOLDER) || fileSelectionType.equals(FileSelectionType.EXISTING_FILE_OR_FOLDER);
  }

  public boolean allowsFileCreation() {
    return fileSelectionType.equals(FileSelectionType.FILE) || fileSelectionType.equals(FileSelectionType.FILE_OR_FOLDER);
  }

  public boolean allowsFolderCreation() {
    return fileSelectionType.equals(FileSelectionType.FILE) || fileSelectionType.equals(FileSelectionType.FOLDER) || fileSelectionType.equals(FileSelectionType.FILE_OR_FOLDER);
  }

  private void addEventHandlers() {
    addFileSelectionRequiredHandler(); // handler for file selection required
    addFileSelectionHandler(); // handler for file selected in FolderDetails
    addFolderSelectionHandler(); // handler for folder selected in FileSystemTree
    addSelectButtonHandler();
    addCancelButtonHandler();
    addCreateFolderButtonHandler();
  }

  private void addFileSelectionHandler() {
    super.registerHandler(folderDetailsPresenter.getDisplay().addFileSelectionHandler(new InternalFileSelectionHandler()));
  }

  private void addFileSelectionRequiredHandler() {
    super.registerHandler(eventBus.addHandler(FileSelectionRequiredEvent.getType(), new FileSelectionRequiredHandler()));
  }

  private void addFolderSelectionHandler() {
    FolderSelectionHandler folderSelectionHandler = new FolderSelectionHandler();

    super.registerHandler(eventBus.addHandler(FileSystemTreeFolderSelectionChangeEvent.getType(), folderSelectionHandler));
    super.registerHandler(eventBus.addHandler(FolderSelectionChangeEvent.getType(), folderSelectionHandler));
  }

  private void addCreateFolderButtonHandler() {
    super.registerHandler(getDisplay().addCreateFolderButtonHandler(new CreateFolderButtonHandler()));
  }

  private void addSelectButtonHandler() {
    super.registerHandler(getDisplay().addSelectButtonHandler(new SelectButtonHandler()));
  }

  private void addCancelButtonHandler() {
    super.registerHandler(getDisplay().addCancelButtonHandler(new CancelButtonHandler()));
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

    for(SelectionResolver resolver : selectionResolverChain) {
      selection = resolver.resolveSelection(fileSelectionType, selectedFolder, selectedFile, getDisplay().getNewFileName());
      if(selection != null) {
        break;
      }
    }

    return selection;
  }

  //
  // Inner Classes / Interfaces
  //

  public enum FileSelectionType {
    FILE, EXISTING_FILE, FOLDER, EXISTING_FOLDER, FILE_OR_FOLDER, EXISTING_FILE_OR_FOLDER
  }

  public interface Display extends WidgetDisplay {

    void showDialog();

    void hideDialog();

    void setTreeDisplay(FileSystemTreePresenter.Display treeDisplay);

    void setDetailsDisplay(FolderDetailsPresenter.Display detailsDisplay);

    void setNewFilePanelVisible(boolean visible);

    void setNewFolderPanelVisible(boolean visible);

    HasWidgets getFileSystemTreePanel();

    HasWidgets getFolderDetailsPanel();

    HandlerRegistration addSelectButtonHandler(ClickHandler handler);

    HandlerRegistration addCancelButtonHandler(ClickHandler handler);

    HandlerRegistration addCreateFolderButtonHandler(ClickHandler handler);

    String getNewFileName();

    void clearNewFileName();

    HasText getCreateFolderName();

    void clearNewFolderName();
  }

  class InternalFileSelectionHandler implements FileSelectionHandler {

    public void onFileSelection(FileDto fileDto) {
      if(fileDto.getType().isFileType(FileType.FILE)) {
        selectedFile = fileDto.getPath();
      }
    }
  }

  class FileSelectionRequiredHandler implements FileSelectionRequiredEvent.Handler {

    public void onFileSelectionRequired(FileSelectionRequiredEvent event) {
      selectedFolder = "/"; // clear previous selection (initial folder is root)
      selectedFile = null; // clear previous selection
      setFileSelectionSource(event.getSource());
      setFileSelectionType(event.getFileSelectionType());
      refreshDisplay();
      revealDisplay();
    }
  }

  class FolderSelectionHandler implements FileSystemTreeFolderSelectionChangeEvent.Handler, FolderSelectionChangeEvent.Handler {

    public void onFolderSelectionChange(FileSystemTreeFolderSelectionChangeEvent event) {
      handleFolderSelection(event.getFolder().getPath());
    }

    public void onFolderSelectionChange(FolderSelectionChangeEvent event) {
      handleFolderSelection(event.getFolder().getPath());
    }

    private void handleFolderSelection(String folderPath) {
      selectedFolder = folderPath;
      selectedFile = null;
    }
  }

  class CreateFolderButtonHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      String newFolder = getDisplay().getCreateFolderName().getText();
      if(selectedFolder != null && newFolder.trim().length() != 0) {
        if(selectedFolder.equals("/")) { // create under root
          createFolder("/" + newFolder);
        } else {
          createFolder(selectedFolder + "/" + newFolder);
        }
      }
    }
  }

  class SelectButtonHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      String selection = getSelection();
      if(selection != null) {
        eventBus.fireEvent(new FileSelectionEvent(FileSelectorPresenter.this.fileSelectionSource, selection));
      }
      getDisplay().hideDialog();
    }
  }

  class CancelButtonHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      getDisplay().hideDialog();
    }
  }

  interface SelectionResolver {

    public String resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile, String newFileName);
  }

  static abstract class AbstractSelectionResolver implements SelectionResolver {

    public abstract String resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile, String newFileName);

    public String getFileSelection(String selectedFolder, String selectedFile, String newFileName) {
      String fileSelection = null;

      if(newFileName != null && newFileName.trim().length() != 0) {
        fileSelection = (!selectedFolder.equals("/") ? selectedFolder + "/" : selectedFolder) + newFileName;
      } else {
        fileSelection = selectedFile;
      }

      return fileSelection;
    }
  }

  static class FileSelectionResolver extends AbstractSelectionResolver {

    public String resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile, String newFileName) {
      if(type.equals(FileSelectionType.FILE)) {
        return getFileSelection(selectedFolder, selectedFile, newFileName);
      }
      return null;
    }
  }

  static class ExistingFileSelectionResolver extends AbstractSelectionResolver {

    public String resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile, String newFileName) {
      if(type.equals(FileSelectionType.EXISTING_FILE)) {
        return selectedFile;
      }
      return null;
    }
  }

  static class AnyFolderSelectionResolver extends AbstractSelectionResolver {

    public String resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile, String newFileName) {
      if(type.equals(FileSelectionType.FOLDER) || type.equals(FileSelectionType.EXISTING_FOLDER)) {
        return selectedFolder;
      }
      return null;
    }
  }

  static class NewFileOrFolderSelectionResolver extends AbstractSelectionResolver {

    public String resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile, String newFileName) {
      if(type.equals(FileSelectionType.FILE_OR_FOLDER)) {
        String selection = getFileSelection(selectedFolder, selectedFile, newFileName);
        if(selection == null) {
          selection = selectedFolder;
        }
        return selection;
      }
      return null;
    }
  }

  static class ExistingFileOrFolderSelectionResolver extends AbstractSelectionResolver {

    public String resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile, String newFileName) {
      if(type.equals(FileSelectionType.EXISTING_FILE_OR_FOLDER)) {
        return (selectedFile != null) ? selectedFile : selectedFolder;
      }
      return null;
    }
  }
}
