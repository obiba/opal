/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.presenter.MagmaPresenter;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.ui.BreadcrumbsTabPanel;

import com.github.gwtbootstrap.client.ui.Badge;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavPills;
import com.github.gwtbootstrap.client.ui.constants.BadgeType;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class MagmaView extends ViewImpl implements MagmaPresenter.Display {

  interface Binder extends UiBinder<Widget, MagmaView> {}

  @UiField
  Heading heading;

  @UiField
  BreadcrumbsTabPanel tabPanel;

  private final PlaceManager placeManager;

  private final Translations translations;

  private Widget datasourceWidget;

  private Widget tableWidget;

  private Widget variableWidget;

  private Badge badge;

  @Inject
  public MagmaView(Binder uiBinder, PlaceManager placeManager, Translations translations) {
    this.placeManager = placeManager;
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        switch(event.getSelectedItem()) {
          case 0:
            //getUiHandlers().onDatasourceSelection(datasource);
            setHeading();
            break;
          case 1:
            //getUiHandlers().onTableSelection(datasource, table);
            setHeading();
            break;
        }
      }
    });
    badge = new Badge();
    badge.addStyleName("small-right-indent");
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    switch((Slot) slot) {
      case DATASOURCE:
        datasourceWidget = content.asWidget();
        break;
      case TABLE:
        tableWidget = content.asWidget();
        break;
      case VARIABLE:
        variableWidget = content.asWidget();
        break;
    }
  }

  @Override
  public void setBookmarkIcon(IsWidget widget) {
    tabPanel.prependMenuWidget(badge);
    tabPanel.appendMenuWidget(widget);
  }

  @Override
  public void selectDatasource(String name) {
    tabPanel.clear();
    tabPanel.addAndSelect(datasourceWidget, name);
    //tabPanel.setMenuVisible(false);
    badge.setType(BadgeType.INFO);
    badge.setText("D");
    badge.setTitle(translations.datasourceLabel());
    setHeading();
  }

  @Override
  public void selectTable(String datasource, String table, boolean isView) {
    tabPanel.clear();
    tabPanel.add(datasourceWidget, getDatasourceLink(datasource));
    tabPanel.addAndSelect(tableWidget, table);
    tabPanel.setMenuVisible(true);
    badge.setType(BadgeType.WARNING);
    badge.setText("T");
    badge.setTitle(translations.tableLabel());
    setHeading();
  }

  @Override
  public void selectVariable(String datasource, String table, String variable) {
    tabPanel.clear();
    tabPanel.add(datasourceWidget, getDatasourceLink(datasource));
    tabPanel.add(tableWidget, getTableLink(datasource, table));
    tabPanel.addAndSelect(variableWidget, variable);
    tabPanel.setMenuVisible(true);
    badge.setType(BadgeType.IMPORTANT);
    badge.setText("V");
    badge.setTitle(translations.variableLabel());
    setHeading();
  }

  private void setHeading() {
    heading.setText(translations.tablesLabel());
  }

  private HasClickHandlers getDatasourceLink(String name) {
    NavLink link = new NavLink(name);
//    link.setIcon(IconType.TABLE);
    link.setHref("#" + placeManager.buildHistoryToken(ProjectPlacesHelper.getDatasourcePlace(name)));
    link.setTitle(translations.allTablesLabel());
    return link;
  }

  private HasClickHandlers getTableLink(String datasource, String table) {
    NavLink link = new NavLink(table);
    link.setHref("#" + placeManager.buildHistoryToken(ProjectPlacesHelper.getTablePlace(datasource, table)));
    return link;
  }

}
