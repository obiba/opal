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

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter.Display;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.FileDto;
import org.obiba.opal.web.model.client.FileDto.FileType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class FileSystemTreeView implements Display {

  private final Translations translations = GWT.create(Translations.class);

  private final Tree fileSystemTree;

  private TreeItem treeRoot;

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
    fileSystemTree.setSelectedItem(treeRoot, true);
  }

  public void addBranch(TreeItem treeItem, FileDto folderToAdd) {
    FileDto file;
    for(int i = 0; i < folderToAdd.getChildrenArray().length(); i++) {
      file = folderToAdd.getChildrenArray().get(i);
      if(file.getType().isFileType(FileDto.FileType.FOLDER) && !file.getSymbolicLink()) {
        treeItem.addItem(createTreeItem(file));
      }
    }
    treeItem.setState(true);
  }

  public void addBranch(FileDto folderToAdd) {
    FileDto parentDto = FileDto.create();
    parentDto.setType(FileType.FOLDER);

    int lastPathSeparatorIndex = folderToAdd.getPath().lastIndexOf('/');
    if(lastPathSeparatorIndex != -1) {
      parentDto.setPath(folderToAdd.getPath().substring(0, lastPathSeparatorIndex));
      parentDto.setName(folderToAdd.getPath().substring(lastPathSeparatorIndex + 1));
    } else {
      parentDto.setPath(folderToAdd.getPath());
      parentDto.setName(folderToAdd.getPath());
    }

    TreeItem parentItem = findTreeItem(parentDto);
    if(parentItem != null) {
      parentItem.addItem(createTreeItem(folderToAdd));
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
  public void selectTreeItem(FileDto folder) {
    String folderName = folder.getName();
    Boolean parentFolderSelected = Boolean.TRUE;

    TreeItem root = fileSystemTree.getSelectedItem();
    for(int i = 0; i < root.getChildCount(); i++) {
      if(folderName.equals(root.getChild(i).getText())) {
        root.setState(true);
        fileSystemTree.setSelectedItem(root.getChild(i));
        parentFolderSelected = Boolean.FALSE;
        break;
      }
    }

    if(parentFolderSelected) {
      if(root.getParentItem() != null) {
        fileSystemTree.setSelectedItem(root.getParentItem());
        root.getParentItem().setState(true);
      }
    }
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
}
