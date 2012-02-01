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

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class Grid<T> extends DataGrid<T> {

  private static final int DEFAULT_PAGESIZE = 15;

  private static final GridResources resources = GWT.<GridResources> create(GridResources.class);

  public interface GridResources extends DataGrid.Resources {
    @NotStrict
    @Source("org/obiba/opal/web/gwt/app/public/css/opal-DataGrid.css")
    DataGrid.Style dataGridStyle();
  }

  public Grid() {
    this(DEFAULT_PAGESIZE);
  }

  /**
   * @param pageSize
   */
  public Grid(int pageSize) {
    super(pageSize, resources);
    setStyleName(resources.dataGridStyle().dataGridWidget());
    addStyleName("opal-Grid");
    Image loading = new Image("image/loading.gif");
    loading.addStyleName("loading");
    setLoadingIndicator(loading);
  }

  @Override
  public void setEmptyTableWidget(Widget widget) {
    widget.addStyleName("empty-label");
    super.setEmptyTableWidget(widget);
  }
}
