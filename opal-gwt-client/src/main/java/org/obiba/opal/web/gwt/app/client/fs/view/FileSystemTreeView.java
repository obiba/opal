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

import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter.Display;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class FileSystemTreeView implements Display {

  private final Translations translations = GWT.create(Translations.class);

  private final Tree fileSystemTree;

  private TreeItem treeRoot;

  private FileDto selectedFile;

  public FileSystemTreeView() {
    fileSystemTree = new Tree();
  }

  @Override
  public Widget asWidget() {
    return fileSystemTree;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void initTree(FileDto rootDto) {
    fileSystemTree.clear();
    treeRoot = createTreeItem(rootDto);
    treeRoot.setText(translations.fileSystemLabel());
    fileSystemTree.addItem(treeRoot);
    addBranch(treeRoot, rootDto);
    treeRoot.setState(true);
    selectedFile = rootDto;
  }

  public HandlerRegistration addFileSystemTreeOpenHandler(OpenHandler<TreeItem> openHandler) {
    return fileSystemTree.addOpenHandler(openHandler);
  }

  @SuppressWarnings("unchecked")
  public void addBranch(TreeItem treeItem, FileDto folderToAdd) {
    FileDto file;
    JsArray<FileDto> children = (folderToAdd.getChildrenCount() != 0) ? folderToAdd.getChildrenArray() : (JsArray<FileDto>) JsArray.createArray();
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
    final TreeItem item = new TreeItem(fileItem.getName());
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

  public void selectFile(FileDto folder) {
    selectFile(folder, true);
  }

  public FileDto getSelectedFile() {
    return selectedFile;
  }

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
