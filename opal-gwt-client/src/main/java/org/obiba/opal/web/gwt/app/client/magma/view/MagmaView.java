package org.obiba.opal.web.gwt.app.client.magma.view;

import org.obiba.opal.web.gwt.app.client.magma.presenter.MagmaPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.MagmaUiHandlers;
import org.obiba.opal.web.gwt.app.client.workbench.view.BreadcrumbsTabPanel;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
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

  @Inject
  public MagmaView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
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
    tabPanel.clear();
    tabPanel.addAndSelect(datasourceWidget, name);
  }

  @Override
  public void selectTable(String datasource, String table) {
    tabPanel.clear();
    tabPanel.add(datasourceWidget, datasource);
    tabPanel.addAndSelect(tableWidget, table);
  }

  @Override
  public void selectVariable(String datasource, String table, String variable) {
    tabPanel.clear();
    tabPanel.add(datasourceWidget, datasource);
    tabPanel.add(tableWidget, table);
    tabPanel.addAndSelect(variableWidget, variable);
  }

}
