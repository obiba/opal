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

import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDeletedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSystemTreeFolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.event.FolderCreationEvent;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class FileSystemTreePresenter extends PresenterWidget<FileSystemTreePresenter.Display> {

  public interface Display extends View {
    void initTree(FileDto root);

    void addBranch(TreeItem treeItem, FileDto folderToAdd);

    void addBranch(FileDto folderToAdd);

    void removeBranch(FileDto folderToRemove);

    HasSelectionHandlers<TreeItem> getFileSystemTree();

    HandlerRegistration addFileSystemTreeOpenHandler(OpenHandler<TreeItem> openHandler);

    void selectFile(FileDto folder);

    void selectFile(FileDto folder, boolean fireEvents);

    FileDto getSelectedFile();

  }

  private final RequestCredentials credentials;

  @Inject
  public FileSystemTreePresenter(Display display, EventBus eventBus, RequestCredentials credentials) {
    super(eventBus, display);
    this.credentials = credentials;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  @Override
  public void onReveal() {
    ResourceRequestBuilderFactory.<FileDto>newBuilder().forResource("/files/_meta").get()
        .withCallback(new ResourceCallback<FileDto>() {
          @Override
          public void onResource(Response response, FileDto root) {
            getView().initTree(root);
            getView().selectFile(root, false);
            FileDto home = getChildFromName(root, "home");
            if(home != null) {
              FileDto userHome = getChildFromName(home, credentials.getUsername());
              if(userHome != null) {
                getView().selectFile(userHome, false);
              }
            }
          }
        }).send();
  }

  private FileDto getChildFromName(FileDto directory, String name) {
    if(directory.getChildrenCount() == 0) return null;

    for(FileDto child : JsArrays.toIterable(directory.getChildrenArray())) {
      if(child.getName().equals(name)) {
        return child;
      }
    }
    return null;
  }

  private void addTreeItemSelectionHandler() {
    registerHandler(getView().getFileSystemTree().addSelectionHandler(new SelectionHandler<TreeItem>() {

      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {

        final TreeItem selectedItem = event.getSelectedItem();
        FileDto selectedFile = (FileDto) selectedItem.getUserObject();

        if(!selectedFile.getReadable()) {
          // reset to previous selection
          getView().selectFile(getView().getSelectedFile(), false);
          return;
        }

        getEventBus().fireEvent(new FileSystemTreeFolderSelectionChangeEvent(selectedFile));
        getEventBus().fireEvent(new FileSelectionChangeEvent(selectedFile));
        getView().selectFile(selectedFile, false);

        if(childrenNotAdded(selectedItem)) {
          FileResourceRequest.newBuilder(getEventBus()).path(selectedFile.getPath())
              .withCallback(new ResourceCallback<FileDto>() {
                @Override
                public void onResource(Response response, FileDto file) {
                  getView().addBranch(selectedItem, file);
                }
              }).send();
        }
      }

      private boolean childrenNotAdded(TreeItem selectedItem) {
        return selectedItem.getChildCount() == 0;
      }

    }));
  }

  private void addEventHandlers() {
    addTreeItemSelectionHandler();

    registerHandler(
        getEventBus().addHandler(FolderSelectionChangeEvent.getType(), new FolderSelectionChangeEvent.Handler() {

          @Override
          public void onFolderSelectionChange(FolderSelectionChangeEvent event) {
            getView().selectFile(event.getFolder(), false);
          }

        }));

    registerHandler(getEventBus().addHandler(FolderCreationEvent.getType(), new FolderCreationEvent.Handler() {

      @Override
      public void onFolderCreation(FolderCreationEvent event) {
        // Refresh the file system since a new folder was added.
        getView().addBranch(event.getFolder());
      }
    }));

    registerHandler(getEventBus().addHandler(FileDeletedEvent.getType(), new FileDeletedEvent.Handler() {

      @Override
      public void onFileDeleted(FileDeletedEvent event) {
        getView().selectFile(FileDtos.getParent(event.getFile()));
        getView().removeBranch(event.getFile());
      }

    }));

    registerHandler(getView().addFileSystemTreeOpenHandler(new OpenHandler<TreeItem>() {

      @Override
      public void onOpen(OpenEvent<TreeItem> event) {
        refreshTreeNode(event.getTarget());
      }

    }));

  }

  private void refreshTreeNode(final TreeItem treeItem) {
    FileDto folder = (FileDto) treeItem.getUserObject();

    FileResourceRequest.newBuilder(getEventBus()).path(folder.getPath()).withCallback(new ResourceCallback<FileDto>() {
      @Override
      public void onResource(Response response, FileDto file) {
        treeItem.removeItems();
        getView().addBranch(treeItem, file);
        getView().selectFile(getView().getSelectedFile(), false);
      }
    }).send();

  }
}
