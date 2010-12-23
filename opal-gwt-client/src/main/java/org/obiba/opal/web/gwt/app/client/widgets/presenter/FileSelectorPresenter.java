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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileUploadDialogPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.FolderCreationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 *
 */
public class FileSelectorPresenter extends WidgetPresenter<FileSelectorPresenter.Display> {

  FileSystemTreePresenter fileSystemTreePresenter;

  FolderDetailsPresenter folderDetailsPresenter;

  @Inject
  Provider<FileUploadDialogPresenter> fileUploadDialogPresenterProvider;

  private Object fileSelectionSource;

  private FileSelectionType fileSelectionType = FileSelectionType.FILE;

  private List<SelectionResolver> selectionResolverChain;

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
    getDisplay().setDisplaysUploadFile(displaysFiles());

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
    addSelectButtonHandler();
    addCancelButtonHandler();
    addCreateFolderButtonHandler();
    super.registerHandler(getDisplay().addUploadButtonHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        FileUploadDialogPresenter presenter = fileUploadDialogPresenterProvider.get();
        presenter.setCurrentFolder(getCurrentFolder());
        presenter.bind();
        presenter.revealDisplay();
      }
    }));
  }

  private void addFileSelectionRequiredHandler() {
    super.registerHandler(eventBus.addHandler(FileSelectionRequiredEvent.getType(), new FileSelectionRequiredHandler()));
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
          getDisplay().clearNewFolderName();
        } else {
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, response.getText(), null));
        }
      }
    };

    ResourceRequestBuilderFactory.newBuilder().forResource("/files" + folder).put().withCallback(201, callbackHandler).withCallback(403, callbackHandler).withCallback(500, callbackHandler).send();
  }

  private FileDto getCurrentFolder() {
    return folderDetailsPresenter.getCurrentFolder();
  }

  public FileSelection getSelection() {
    FileSelection selection = null;

    for(SelectionResolver resolver : selectionResolverChain) {
      FileDto currentFolder = folderDetailsPresenter.getCurrentFolder();
      FileDto currentSelection = folderDetailsPresenter.getSelectedFile();

      resolver.resolveSelection(fileSelectionType, currentFolder.getPath(), currentSelection != null ? currentSelection.getPath() : null, getDisplay().getNewFileName());
      if(resolver.resolved()) {
        selection = resolver.getSelection();
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

    void setDisplaysUploadFile(boolean displaysFiles);

    void hideDialog();

    void setTreeDisplay(FileSystemTreePresenter.Display treeDisplay);

    void setDetailsDisplay(FolderDetailsPresenter.Display detailsDisplay);

    void setNewFilePanelVisible(boolean visible);

    void setNewFolderPanelVisible(boolean visible);

    HasWidgets getFileSystemTreePanel();

    HasWidgets getFolderDetailsPanel();

    HandlerRegistration addUploadButtonHandler(ClickHandler handler);

    HandlerRegistration addSelectButtonHandler(ClickHandler handler);

    HandlerRegistration addCancelButtonHandler(ClickHandler handler);

    HandlerRegistration addCreateFolderButtonHandler(ClickHandler handler);

    String getNewFileName();

    void clearNewFileName();

    HasText getCreateFolderName();

    void clearNewFolderName();
  }

  class FileSelectionRequiredHandler implements FileSelectionRequiredEvent.Handler {

    public void onFileSelectionRequired(FileSelectionRequiredEvent event) {
      setFileSelectionSource(event.getSource());
      setFileSelectionType(event.getFileSelectionType());
      refreshDisplay();
      revealDisplay();
    }
  }

  class CreateFolderButtonHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      String newFolder = getDisplay().getCreateFolderName().getText().trim();
      while(newFolder.startsWith("/")) {
        newFolder = newFolder.substring(1);
      }
      FileDto currentFolder = getCurrentFolder();
      if(currentFolder != null && newFolder.length() != 0) {
        String path = currentFolder.getPath();
        createFolder(path.endsWith("/") ? path + newFolder : path + '/' + newFolder);
      }
    }
  }

  class SelectButtonHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      FileSelection selection = getSelection();
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

  public static class FileSelection {

    private String selectionPath;

    private FileSelectionType selectionType;

    public FileSelection(String selectionPath, FileSelectionType selectionType) {
      this.selectionPath = selectionPath;
      this.selectionType = selectionType;
    }

    public String getSelectionPath() {
      return selectionPath;
    }

    public FileSelectionType getSelectionType() {
      return selectionType;
    }
  }

  interface SelectionResolver {

    public void resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile, String newFileName);

    public boolean resolved();

    public FileSelection getSelection();
  }

  static abstract class AbstractSelectionResolver implements SelectionResolver {

    protected FileSelection selection;

    protected boolean resolved;

    public boolean resolved() {
      return resolved;
    }

    public FileSelection getSelection() {
      return selection;
    }

    public FileSelection getFileSelection(String selectedFolder, String selectedFile, String newFileName) {
      String selectionPath = null;

      if(newFileName != null && newFileName.trim().length() != 0) {
        selectionPath = (!selectedFolder.equals("/") ? selectedFolder + "/" : selectedFolder) + newFileName;
      } else {
        selectionPath = selectedFile;
      }

      return new FileSelection(selectionPath, FileSelectionType.FILE);
    }
  }

  static class FileSelectionResolver extends AbstractSelectionResolver {

    public void resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile, String newFileName) {
      resolved = false;
      if(type.equals(FileSelectionType.FILE)) {
        selection = getFileSelection(selectedFolder, selectedFile, newFileName);
        resolved = (selection.getSelectionPath() != null);
      }
    }
  }

  static class ExistingFileSelectionResolver extends AbstractSelectionResolver {

    public void resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile, String newFileName) {
      resolved = false;
      if(type.equals(FileSelectionType.EXISTING_FILE)) {
        selection = new FileSelection(selectedFile, FileSelectionType.FILE);
        resolved = (selection.getSelectionPath() != null);
      }
    }
  }

  static class AnyFolderSelectionResolver extends AbstractSelectionResolver {

    public void resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile, String newFileName) {
      resolved = false;
      if(type.equals(FileSelectionType.FOLDER) || type.equals(FileSelectionType.EXISTING_FOLDER)) {
        selection = new FileSelection(selectedFolder, FileSelectionType.FOLDER);
        resolved = (selection.getSelectionPath() != null);
      }
    }
  }

  static class NewFileOrFolderSelectionResolver extends AbstractSelectionResolver {

    public void resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile, String newFileName) {
      resolved = false;
      if(type.equals(FileSelectionType.FILE_OR_FOLDER)) {
        selection = getFileSelection(selectedFolder, selectedFile, newFileName);
        if(selection.getSelectionPath() == null) {
          selection = new FileSelection(selectedFolder, FileSelectionType.FOLDER);
        }
        resolved = (selection.getSelectionPath() != null);
      }
    }
  }

  static class ExistingFileOrFolderSelectionResolver extends AbstractSelectionResolver {

    public void resolveSelection(FileSelectionType type, String selectedFolder, String selectedFile, String newFileName) {
      resolved = false;
      if(type.equals(FileSelectionType.EXISTING_FILE_OR_FOLDER)) {
        selection = (selectedFile != null) ? new FileSelection(selectedFile, FileSelectionType.FILE) : new FileSelection(selectedFolder, FileSelectionType.FOLDER);
        resolved = (selection.getSelectionPath() != null);
      }
    }
  }
}
