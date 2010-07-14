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
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
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
    addBranch(treeRoot, rootDto, 0);
    fileSystemTree.setSelectedItem(treeRoot, true);
  }

  public HandlerRegistration addFileSystemTreeOpenHandler(OpenHandler<TreeItem> openHandler) {
    return fileSystemTree.addOpenHandler(openHandler);
  }

  public void addBranch(TreeItem treeItem, FileDto folderToAdd, int level) {
    ++level;
    FileDto file;
    for(int i = 0; i < folderToAdd.getChildrenArray().length(); i++) {
      file = folderToAdd.getChildrenArray().get(i);

      if(file.getType().isFileType(FileDto.FileType.FOLDER)) {
        TreeItem childItem = createTreeItem(file);
        if(file.getChildrenCount() > 0) {
          addBranch(childItem, file, level);
        }
        treeItem.addItem(childItem);

        if(level == 1) {
          treeItem.setState(true);
        }
      }

    }
  }

  public void addBranch(FileDto folderToAdd) {
    FileDto parentDto = FileDto.create();
    parentDto.setType(FileType.FOLDER);
    parentDto.setPath(getParentFolderPath(folderToAdd.getPath()));
    parentDto.setName(getFolderName(parentDto.getPath()));

    TreeItem parentItem = findTreeItem(parentDto);
    if(parentItem != null) {
      parentItem.addItem(createTreeItem(folderToAdd));
    } else {
      addBranch(parentDto);
    }
  }

  private String getParentFolderPath(String childPath) {
    String parentPath = null;

    int lastSeparatorIndex = childPath.lastIndexOf('/');

    if(lastSeparatorIndex != -1) {
      parentPath = lastSeparatorIndex != 0 ? childPath.substring(0, lastSeparatorIndex) : "/";
    }

    return parentPath;
  }

  private String getFolderName(String folderPath) {
    String folderName = folderPath;

    int lastSeparatorIndex = folderPath.lastIndexOf('/');

    if(lastSeparatorIndex != -1) {
      folderName = folderPath.substring(lastSeparatorIndex + 1);
    }

    return folderName;
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
    TreeItem item = findTreeItem(folder);
    fileSystemTree.setSelectedItem(item);
    item.setState(true);
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
