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

    HasSelectionHandlers<TreeItem> getFileSystemTree();

    void selectTreeItem(FileDto folder);

    void selectTreeRoot();

  }

  @Inject
  public FileSystemTreePresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
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

  }

  @Override
  public void revealDisplay() {
    getDisplay().selectTreeRoot();
  }

  protected void initDisplayComponents() {

    ResourceRequestBuilderFactory.<FileDto> newBuilder().forResource("/files").get().withCallback(new ResourceCallback<FileDto>() {
      @Override
      public void onResource(Response response, FileDto root) {
        getDisplay().initTree(root);
      }
    }).send();

  }

  private void addEventHandlers() {
    getDisplay().getFileSystemTree().addSelectionHandler(new SelectionHandler<TreeItem>() {

      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {

        final TreeItem selectedItem = event.getSelectedItem();
        FileDto selectedFile = ((FileDto) selectedItem.getUserObject());

        eventBus.fireEvent(new FileSystemTreeFolderSelectionChangeEvent(selectedFile));

        if(selectedItem.getChildCount() == 0) {
          ResourceRequestBuilderFactory.<FileDto> newBuilder().forResource("/files" + selectedFile.getPath()).get().withCallback(new ResourceCallback<FileDto>() {
            @Override
            public void onResource(Response response, FileDto file) {
              getDisplay().addBranch(selectedItem, file);
            }
          }).send();
        }
      }

    });

    super.registerHandler(eventBus.addHandler(FolderSelectionChangeEvent.getType(), new FolderSelectionChangeEvent.Handler() {

      @Override
      public void onFolderSelectionChange(FolderSelectionChangeEvent event) {
        getDisplay().selectTreeItem(event.getFolder());
      }

    }));
  }
}
