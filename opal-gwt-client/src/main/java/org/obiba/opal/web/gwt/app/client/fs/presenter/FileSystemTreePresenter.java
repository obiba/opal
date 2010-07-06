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

import org.obiba.opal.web.gwt.app.client.fs.event.FileSystemTreeFolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.FolderCreationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.FileDto;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.inject.Inject;

public class FileSystemTreePresenter extends WidgetPresenter<FileSystemTreePresenter.Display> {

  public interface Display extends WidgetDisplay {
    void initTree(FileDto root);

    void addBranch(TreeItem treeItem, FileDto folderToAdd);

    void addBranch(FileDto folderToAdd);

    HasSelectionHandlers<TreeItem> getFileSystemTree();

    void selectTreeItem(FileDto folder);

  }

  @Inject
  public FileSystemTreePresenter(Display display, EventBus eventBus) {
    super(display, eventBus);

    // Do NOT add this handler in onBind(). This handler will NOT be removed when
    // FileSystemTreePresenter's unbind() method is called, causing multiple instances
    // of this handler.
    addTreeItemSelectionHandler(eventBus);
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
  }

  @Override
  public void refreshDisplay() {
    ResourceRequestBuilderFactory.<FileDto> newBuilder().forResource("/files/meta").get().withCallback(new ResourceCallback<FileDto>() {
      @Override
      public void onResource(Response response, FileDto root) {
        getDisplay().initTree(root);
        getDisplay().selectTreeItem(root);
        eventBus.fireEvent(new FileSystemTreeFolderSelectionChangeEvent(root));
      }
    }).send();
  }

  @Override
  public void revealDisplay() {
  }

  protected void initDisplayComponents() {

    ResourceRequestBuilderFactory.<FileDto> newBuilder().forResource("/files/meta").get().withCallback(new ResourceCallback<FileDto>() {
      @Override
      public void onResource(Response response, FileDto root) {
        getDisplay().initTree(root);
        getDisplay().selectTreeItem(root);
        eventBus.fireEvent(new FileSystemTreeFolderSelectionChangeEvent(root));
      }
    }).send();

  }

  private void addTreeItemSelectionHandler(final EventBus eventBus) {
    getDisplay().getFileSystemTree().addSelectionHandler(new SelectionHandler<TreeItem>() {

      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {

        final TreeItem selectedItem = event.getSelectedItem();
        FileDto selectedFile = ((FileDto) selectedItem.getUserObject());

        eventBus.fireEvent(new FileSystemTreeFolderSelectionChangeEvent(selectedFile));

        if(childrenNotAdded(selectedItem)) {
          ResourceRequestBuilderFactory.<FileDto> newBuilder().forResource("/files/meta" + selectedFile.getPath()).get().withCallback(new ResourceCallback<FileDto>() {
            @Override
            public void onResource(Response response, FileDto file) {
              getDisplay().addBranch(selectedItem, file);
            }
          }).send();
        }
      }

      private boolean childrenNotAdded(final TreeItem selectedItem) {
        return selectedItem.getChildCount() == 0;
      }

    });
  }

  private void addEventHandlers() {
    super.registerHandler(eventBus.addHandler(FolderSelectionChangeEvent.getType(), new FolderSelectionChangeEvent.Handler() {

      public void onFolderSelectionChange(FolderSelectionChangeEvent event) {
        getDisplay().selectTreeItem(event.getFolder());
      }

    }));

    super.registerHandler(eventBus.addHandler(FolderCreationEvent.getType(), new FolderCreationEvent.Handler() {

      public void onFolderCreation(FolderCreationEvent event) {
        // Refresh the file system since a new folder was added.
        getDisplay().addBranch(event.getFolder());
      }
    }));
  }
}
