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

import com.github.gwtbootstrap.client.ui.NavLink;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorTreePresenter;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * View for a Tree displaying Opal datasources and tables.
 */
public class NavigatorTreeView extends ViewImpl implements NavigatorTreePresenter.Display {

  private final Translations translations = GWT.create(Translations.class);

  private Breadcrumbs breadcrumbs;

  private String table;

  private String datasource;

  private ClickHandler datasourceClickHandler;

  private ClickHandler tableClickHandler;

  public NavigatorTreeView() {
    breadcrumbs = new Breadcrumbs();
  }

  @Override
  public void clear() {
    breadcrumbs.clear();
    breadcrumbs.add(new NavLink("Datasources"));
  }

  @Override
  public void selectVariable(TableDto table, VariableDto variable, boolean fireEvent) {
    clear();
    breadcrumbs.add(getDatasourceLink(table.getDatasourceName()));
    breadcrumbs.add(getTableLink(table.getName()));
    breadcrumbs.add(new NavLink(variable.getName()));
  }

  @Override
  public Widget asWidget() {
    return breadcrumbs;
  }

  @Override
  public void selectTable(String datasource, String table, boolean fireEvents) {
    clear();
    breadcrumbs.add(getDatasourceLink(datasource));
    breadcrumbs.add(getTableLink(table));
    if (fireEvents) {
      tableClickHandler.onClick(null);
    }
  }

  @Override
  public void selectDatasource(String datasource, boolean fireEvents) {
    clear();
    breadcrumbs.add(getDatasourceLink(datasource));
    this.datasource = datasource;
    if (fireEvents) {
      datasourceClickHandler.onClick(null);
    }
  }

  @Override
  public void setDatasourceClickHandler(ClickHandler handler) {
    datasourceClickHandler = handler;
  }

  @Override
  public void setTableClickHandler(ClickHandler handler) {
    tableClickHandler = handler;
  }

  @Override
  public String getDatasourceName() {
    return datasource;
  }

  @Override
  public String getTableName() {
    return table;
  }

  private NavLink getDatasourceLink(String datasourceName) {
    NavLink link = new NavLink(datasourceName);
    link.addClickHandler(datasourceClickHandler);
    datasource = datasourceName;
    return link;
  }

  private NavLink getTableLink(String tableName) {
    NavLink link = new NavLink(tableName);
    link.addClickHandler(tableClickHandler);
    table = tableName;
    return link;
  }

}
