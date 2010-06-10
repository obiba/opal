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

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter.Display;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.FileDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class FileSystemTreeView extends Composite implements Display {

  @UiTemplate("FileSystemTreeView.ui.xml")
  interface FileSystemTreeUiBinder extends UiBinder<DockLayoutPanel, FileSystemTreeView> {
  }

  private static FileSystemTreeUiBinder uiBinder = GWT.create(FileSystemTreeUiBinder.class);

  private Translations translations = GWT.create(Translations.class);

  @UiField
  Tree fileSystemTree;

  public FileSystemTreeView() {
    initWidget(uiBinder.createAndBindUi(this));
    initTree();
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  private void initTree() {
  }

  @Override
  public void setFileSystemTree(FileDto rootDto) {
    fileSystemTree.clear();
    TreeItem treeRoot = createTreeItem(rootDto);
    treeRoot.setText(translations.fileSystemLabel());
    fileSystemTree.addItem(treeRoot);
    addBranch(treeRoot, rootDto);
    treeRoot.setSelected(true);
  }

  public void addBranch(TreeItem treeItem, FileDto folderToAdd) {
    FileDto file;
    for(int i = 0; i < folderToAdd.getChildrenArray().length(); i++) {
      file = folderToAdd.getChildrenArray().get(i);
      if(file.getSize() == 0 && !file.getSymbolicLink()) {
        treeItem.addItem(createTreeItem(file));
      }
    }
    treeItem.setState(true);
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

}
