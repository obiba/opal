/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.CellTable;

import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;

/**
 *
 */
public class Table<T> extends CellTable<T> {

  public static final int DEFAULT_PAGESIZE = 50;

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
    setBordered(true);
    setEmptyTableWidget(new InlineLabel("No items."));
  }

  public Table(int pageSize, ProvidesKey<T> keyProvider) {
    super(pageSize, keyProvider);
    setStriped(true);
    setCondensed(true);
    setBordered(true);
  }

  /**
   * Saves the real empty widget before adding it to its container.
   * @param widget
   */
  @Override
  public void setEmptyTableWidget(Widget widget) {
    emptyTableWidget = widget;
    super.setEmptyTableWidget(widget);
  }

  /**
   * Hack because of loading indicator does not work with data provider.
   * <p>NOTE: use the base class to set the temporary empty widget in order to preserve the real one {@link #setEmptyTableWidget} </p>
   */
  public void showLoadingIndicator(ListDataProvider<?> listDataProvider) {
    super.setEmptyTableWidget(getLoadingIndicator());
    listDataProvider.getList().clear();
    listDataProvider.flush();
  }

  /**
   * The complementary method used to hide the temporary empty widget {@link #showLoadingIndicator}
   */
  public void hideLoadingIndicator() {
    super.setEmptyTableWidget(emptyTableWidget);
  }

}
