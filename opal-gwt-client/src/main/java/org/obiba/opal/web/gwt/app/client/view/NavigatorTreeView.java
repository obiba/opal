/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.presenter.NavigatorTreePresenter;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * View for a Tree displaying Opal datasources and tables.
 */
public class NavigatorTreeView implements NavigatorTreePresenter.Display {

  Tree tree;

  public NavigatorTreeView() {
    tree = new Tree();
    tree.setAnimationEnabled(true);
  }

  @Override
  public void setItems(List<TreeItem> items) {
    tree.clear();
    for(TreeItem item : items) {
      tree.addItem(item);
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
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void selectFirstDatasource() {
    tree.setSelectedItem(tree.getItem(0), true);
  }

}
