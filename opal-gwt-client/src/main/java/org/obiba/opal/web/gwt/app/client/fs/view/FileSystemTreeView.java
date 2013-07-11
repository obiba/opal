/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.fs.view;

import java.util.Iterator;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter.Display;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class FileSystemTreeView extends ViewImpl implements Display {

  private final Translations translations = GWT.create(Translations.class);

  private final Tree fileSystemTree;

  private FileDto selectedFile;

  public FileSystemTreeView() {
    fileSystemTree = new Tree();
    fileSystemTree.addOpenHandler(new OpenHandler<TreeItem>() {

      @Override
      public void onOpen(OpenEvent<TreeItem> event) {
        event.getTarget().addStyleName("expanded");
      }
    });
    fileSystemTree.addCloseHandler(new CloseHandler<TreeItem>() {

      @Override
      public void onClose(CloseEvent<TreeItem> event) {
        event.getTarget().removeStyleName("expanded");
      }
    });
  }

  @Override
  public Widget asWidget() {
    return fileSystemTree;
  }

  @Override
  public void initTree(FileDto rootDto) {
    fileSystemTree.clear();
    TreeItem treeRoot = createTreeItem(rootDto);
    treeRoot.setText(translations.fileSystemLabel());
    fileSystemTree.addItem(treeRoot);
    addBranch(treeRoot, rootDto);
    treeRoot.setState(true);
    selectedFile = rootDto;
  }

  @Override
  public HandlerRegistration addFileSystemTreeOpenHandler(OpenHandler<TreeItem> openHandler) {
    return fileSystemTree.addOpenHandler(openHandler);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void addBranch(TreeItem treeItem, FileDto folderToAdd) {
    FileDto file;
    JsArray<FileDto> children = folderToAdd.getChildrenCount() == 0
        ? (JsArray<FileDto>) JsArray.createArray()
        : folderToAdd.getChildrenArray();
    for(int i = 0; i < children.length(); i++) {
      file = children.get(i);

      if(file.getType().isFileType(FileDto.FileType.FOLDER)) {
        TreeItem childItem = createTreeItem(file);
        if(file.getChildrenCount() > 0) {
          addBranch(childItem, file);
        }
        treeItem.addItem(childItem);
      }
    }
  }

  @Override
  public void addBranch(FileDto folderToAdd) {
    // Only add a branch if the fileSystemTree has been initialized (has a root).
    if(fileSystemTree.getItemCount() > 0) {
      FileDto parentDto = FileDtos.getParent(folderToAdd);

      TreeItem parentItem = findTreeItem(parentDto);
      if(parentItem != null) {
        parentItem.addItem(createTreeItem(folderToAdd));
      } else {
        addBranch(parentDto);
      }
    }
  }

  private TreeItem createTreeItem(FileDto fileItem) {
    TreeItem item = new TreeItem(fileItem.getName());
    item.addStyleName("folder" + (fileItem.getReadable() ? "" : " forbidden"));
    item.setUserObject(fileItem);
    return item;
  }

  @Override
  public HasSelectionHandlers<TreeItem> getFileSystemTree() {
    return fileSystemTree;
  }

  @Override
  public void selectFile(FileDto folder, boolean fireEvents) {
    TreeItem item = findTreeItem(folder);
    fileSystemTree.setSelectedItem(item, fireEvents);
    fileSystemTree.ensureSelectedItemVisible();
    selectedFile = folder;
  }

  @Override
  public void selectFile(FileDto folder) {
    selectFile(folder, true);
  }

  @Override
  public FileDto getSelectedFile() {
    return selectedFile;
  }

  @Nullable
  private TreeItem findTreeItem(FileDto dto) {
    Iterator<TreeItem> treeIter = fileSystemTree.treeItemIterator();
    while(treeIter.hasNext()) {
      TreeItem item = treeIter.next();
      if(((FileDto) item.getUserObject()).getPath().equals(dto.getPath())) {
        return item;
      }
    }
    return null;
  }

  @Override
  public void removeBranch(FileDto folderToRemove) {
    TreeItem itemToRemove = findTreeItem(folderToRemove);
    if(itemToRemove != null) {
      itemToRemove.remove();
    }
  }
}
