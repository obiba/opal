package org.obiba.opal.web.gwt.app.client.magma.view;

import org.obiba.opal.web.gwt.app.client.magma.presenter.MagmaPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.MagmaUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.BreadcrumbsTabPanel;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class MagmaView extends ViewWithUiHandlers<MagmaUiHandlers> implements MagmaPresenter.Display {

  interface Binder extends UiBinder<Widget, MagmaView> {}

  @UiField
  BreadcrumbsTabPanel tabPanel;

  private Widget datasourceWidget;

  private Widget tableWidget;

  private Widget variableWidget;

  private String datasource;

  private String table;

  private String variable;

  @Inject
  public MagmaView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
    tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        if (event.getSelectedItem() == 0) {
          getUiHandlers().onDatasourceSelection(datasource);
        }
        else if (event.getSelectedItem() == 1) {
          getUiHandlers().onTableSelection(datasource, table);
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
  }

  @Override
  public void selectTable(String datasource, String table) {
    this.datasource = datasource;
    this.table = table;
    tabPanel.clear();
    tabPanel.add(datasourceWidget, datasource);
    tabPanel.addAndSelect(tableWidget, table);
  }

  @Override
  public void selectVariable(String datasource, String table, String variable) {
    this.datasource = datasource;
    this.table = table;
    this.variable = variable;
    tabPanel.clear();
    tabPanel.add(datasourceWidget, datasource);
    tabPanel.add(tableWidget, table);
    tabPanel.addAndSelect(variableWidget, variable);
  }

}
