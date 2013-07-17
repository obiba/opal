/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import com.github.gwtbootstrap.client.ui.CellTable;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;

/**
 *
 */
public class Table<T> extends CellTable<T> {

  public static final int DEFAULT_PAGESIZE = 15;

  private Widget emptyTableWidget;


  public Table() {
    this(DEFAULT_PAGESIZE);
  }

  /**
   * @param pageSize
   */
  public Table(int pageSize) {
    super(pageSize);
    setStriped(true);
    setCondensed(true);
  }

  public Table(int pageSize, ProvidesKey<T> keyProvider) {
    super(pageSize, keyProvider);
    setStriped(true);
    setCondensed(true);
  }

  @Override
  public void setEmptyTableWidget(Widget widget) {
    super.setEmptyTableWidget(widget);
  }

  /**
   * Hack because of loading indicator does not work with data provider.
   */
  public void showLoadingIndicator(ListDataProvider<?> listDataProvider) {
    emptyTableWidget = getEmptyTableWidget();
    setEmptyTableWidget(getLoadingIndicator());
    listDataProvider.getList().clear();
    listDataProvider.flush();
  }

  // I call this method after my I've updated the data in my ListDataProvider
  public void hideLoadingIndicator() {
    setEmptyTableWidget(emptyTableWidget);
  }

}
