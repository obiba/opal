/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorTreePresenter;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * View for a Tree displaying Opal datasources and tables.
 */
public class NavigatorTreeView extends ViewImpl implements NavigatorTreePresenter.Display {

  Tree tree;

  TreeItem currentSelection = null;

  public NavigatorTreeView() {
    tree = new Tree();
    tree.setAnimationEnabled(true);
    tree.addSelectionHandler(new SelectionHandler<TreeItem>() {

      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {
        TreeItem item = event.getSelectedItem();
        item.addStyleName("selected");
        if(currentSelection != null && !currentSelection.equals(item)) {
          currentSelection.removeStyleName("selected");
        }
        currentSelection = item;
      }
    });
  }

  @Override
  public void setItems(List<TreeItem> items) {
    // update items and keep tree state as much as possible
    List<String> expandedItems = new ArrayList<String>();
    for(int i = 0; i < tree.getItemCount(); i++) {
      TreeItem item = tree.getItem(i);
      if(item.getState()) {
        expandedItems.add(item.getText());
      }
    }
    tree.clear();
    for(TreeItem item : items) {
      item.setState(expandedItems.contains(item.getText()));
      tree.addItem(item);
    }
    if(currentSelection == null) return;

    if(currentSelection.getParentItem() != null) {
      reselectTable();
    } else {
      reselectDatasource();
    }
  }

  private void reselectTable() {
    String datasourceName = currentSelection.getParentItem().getText();
    currentSelection = getTableItem(datasourceName, currentSelection.getText());
    if(currentSelection == null) {
      selectDatasource(datasourceName, true);
    } else {
      // soft selection (do not trigger event)
      currentSelection.addStyleName("selected");
      tree.setSelectedItem(currentSelection, false);
    }
  }

  private void reselectDatasource() {
    currentSelection = getDatasourceItem(currentSelection.getText());
    if(currentSelection == null) {
      selectFirstDatasource(true);
    } else {
      // soft selection (do not trigger event)
      currentSelection.addStyleName("selected");
      tree.setSelectedItem(currentSelection, false);
    }
  }

  @Override
  public HasSelectionHandlers<TreeItem> getTree() {
    return tree;
  }

  @Override
  public void clear() {
    this.tree.clear();
  }

  @Override
  public Widget asWidget() {
    return tree;
  }

  @Override
  public void selectFirstDatasource(boolean fireEvents) {
    tree.setSelectedItem(tree.getItem(0), fireEvents);
  }

  @Override
  public void selectTable(String datasourceName, String tableName, boolean fireEvents) {
    if(!isTableSelected(datasourceName, tableName)) {
      TreeItem dsItem = getDatasourceItem(datasourceName);
      if(dsItem != null) {
        dsItem.setState(true);
        selectChildren(tableName, dsItem, fireEvents);
      }
    }
  }

  private void selectChildren(String tableName, TreeItem dsItem, boolean fireEvents) {
    for(int j = 0; j < dsItem.getChildCount(); j++) {
      TreeItem tableItem = dsItem.getChild(j);
      if(tableName.equals(tableItem.getText())) {
        tree.setSelectedItem(tableItem, fireEvents);
        break;
      }
    }
  }

  @Override
  public void selectDatasource(String datasourceName, boolean fireEvents) {
    if(!isDatasourceSelected(datasourceName)) {
      TreeItem dsItem = getDatasourceItem(datasourceName);
      if(dsItem != null) {
        tree.setSelectedItem(dsItem, fireEvents);
      }
    }
  }

  private TreeItem getDatasourceItem(String datasourceName) {
    for(int i = 0; i < tree.getItemCount(); i++) {
      TreeItem dsItem = tree.getItem(i);
      if(dsItem.getText().equals(datasourceName)) {
        return dsItem;
      }
    }
    return null;
  }

  private TreeItem getTableItem(String datasourceName, String tableName) {
    TreeItem dsItem = getDatasourceItem(datasourceName);
    if(dsItem != null) {
      for(int j = 0; j < dsItem.getChildCount(); j++) {
        TreeItem tableItem = dsItem.getChild(j);
        if(tableName.equals(tableItem.getText())) {
          return tableItem;
        }
      }
    }
    return null;
  }

  private boolean isDatasourceSelected(String datasourceName) {
    TreeItem selected = tree.getSelectedItem();
    if(selected == null) return false;
    if(selected.getParentItem() != null) return false;
    return selected.getText().equals(datasourceName);
  }

  private boolean isTableSelected(String datasourceName, String tableName) {
    TreeItem selected = tree.getSelectedItem();
    if(selected == null) return false;
    if(selected.getParentItem() == null) return false;
    return selected.getParentItem().getText().equals(datasourceName) && selected.getText().equals(tableName);
  }

  @Override
  public boolean hasDatasource(String datasourceName) {
    for(int i = 0; i < tree.getItemCount(); i++) {
      TreeItem dsItem = tree.getItem(i);
      if(dsItem.getText().equals(datasourceName)) {
        return true;
      }
    }
    return false;
  }

}
