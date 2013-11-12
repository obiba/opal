package org.obiba.opal.web.gwt.app.client.magma.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.presenter.MagmaPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.ui.BreadcrumbsTabPanel;

import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
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

  private static final Translations translations = GWT.create(Translations.class);

  interface Binder extends UiBinder<Widget, MagmaView> {}

  @UiField
  Heading heading;

  @UiField
  BreadcrumbsTabPanel tabPanel;

  private final PlaceManager placeManager;

  private Widget datasourceWidget;

  private Widget tableWidget;

  private Widget variableWidget;

  private String datasource;

  private String table;

  private String variable;

  @Inject
  public MagmaView(Binder uiBinder, PlaceManager placeManager) {
    this.placeManager = placeManager;
    initWidget(uiBinder.createAndBindUi(this));
    tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        if(event.getSelectedItem() == 0) {
          //getUiHandlers().onDatasourceSelection(datasource);
          setHeading(datasource, "Datasource");
        } else if(event.getSelectedItem() == 1) {
          //getUiHandlers().onTableSelection(datasource, table);
          setHeading(table, "Table");
        }
      }
    });
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
  public void selectDatasource(String name) {
    datasource = name;
    tabPanel.clear();
    tabPanel.addAndSelect(datasourceWidget, name);
    tabPanel.setMenuVisible(false);
    setHeading(name, "Datasource");

  }

  @Override
  public void selectTable(String datasource, String table, boolean isView) {
    this.datasource = datasource;
    this.table = table;
    tabPanel.clear();
    tabPanel.add(datasourceWidget, getDatasourceLink(datasource));
    tabPanel.addAndSelect(tableWidget, getTableLink(datasource, table));
    tabPanel.setMenuVisible(true);
    setHeading(table, isView ? "View" : "Table");
  }

  @Override
  public void selectVariable(String datasource, String table, String variable) {
    this.datasource = datasource;
    this.table = table;
    this.variable = variable;
    tabPanel.clear();
    tabPanel.add(datasourceWidget, getDatasourceLink(datasource));
    tabPanel.add(tableWidget, getTableLink(datasource, table));
    tabPanel.addAndSelect(variableWidget, variable);
    tabPanel.setMenuVisible(true);
    setHeading(variable, "Variable");
  }

  private void setHeading(String text, String subtext) {
    heading.setText(translations.tablesLabel());
    //heading.setSubtext("[" + subtext + "]");
  }

  private NavLink getDatasourceLink(String name) {
    NavLink link = new NavLink();
    link.setIcon(IconType.TABLE);
    link.setHref("#" + placeManager.buildHistoryToken(ProjectPlacesHelper.getDatasourcePlace(name)));
    link.setTitle(translations.allTablesLabel());
    return link;
  }

  private NavLink getTableLink(String datasource, String table) {
    NavLink link = new NavLink(table);
    link.setHref("#" + placeManager.buildHistoryToken(ProjectPlacesHelper.getTablePlace(datasource, table)));
    return link;
  }

}
