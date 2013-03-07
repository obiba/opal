/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.datashield.view;

import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ConstantActionsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.datashield.DataShieldMethodDto;
import org.obiba.opal.web.model.client.datashield.RScriptDataShieldMethodDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

import static org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn.EDIT_ACTION;

/**
 *
 */
public class DataShieldAdministrationView extends ViewImpl implements DataShieldAdministrationPresenter.Display {

  private static final int PAGE_SIZE = 50;

  @UiTemplate("DataShieldAdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DataShieldAdministrationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Translations translations;

  private final Widget widget;

  @UiField
  Panel methodsPanel;

  @UiField
  Button addMethodButton;

  @UiField
  CellTable<DataShieldMethodDto> methodsTable;

  @UiField
  SimplePager methodsTablePager;

  @UiField
  FlowPanel aggregateMethods;

  @UiField
  FlowPanel assignMethods;

  private JsArrayDataProvider<DataShieldMethodDto> methodsDataProvider = new JsArrayDataProvider<DataShieldMethodDto>();

  private ActionsColumn<DataShieldMethodDto> actionsColumn;

  @Inject
  public DataShieldAdministrationView(Translations translations) {
    this.translations = translations;
    widget = uiBinder.createAndBindUi(this);
    initMethodsTable();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public HandlerRegistration addMethodHandler(ClickHandler handler) {
    return addMethodButton.addClickHandler(handler);
  }

  @Override
  public void renderDataShieldMethodsRows(JsArray<DataShieldMethodDto> rows) {
    methodsDataProvider.setArray(rows);

    int size = methodsDataProvider.getList().size();
    methodsTablePager.firstPage();
    methodsTablePager.setVisible(size > 0);
    methodsTable.setVisible(size > 0);
    methodsDataProvider.refresh();
  }

  @Override
  public HasActionHandler<DataShieldMethodDto> getDataShieldMethodActionsColumn() {
    return actionsColumn;
  }

  private void initMethodsTable() {

    addMethodsTableColumns();

    methodsTable.setPageSize(PAGE_SIZE);
    methodsTablePager.setDisplay(methodsTable);
    methodsDataProvider.addDataDisplay(methodsTable);
  }

  private void addMethodsTableColumns() {
    methodsTable.addColumn(new TextColumn<DataShieldMethodDto>() {
      @Override
      public String getValue(DataShieldMethodDto object) {
        return object.getName();
      }
    }, translations.nameLabel());

    methodsTable.addColumn(new TextColumn<DataShieldMethodDto>() {
      @Override
      public String getValue(DataShieldMethodDto object) {
        return object.getExtension(RScriptDataShieldMethodDto.DataShieldMethodDtoExtensions.method) != null
            ? translations.rScriptLabel()
            : translations.rFunctionLabel();
      }
    }, translations.typeLabel());

    actionsColumn = new ActionsColumn<DataShieldMethodDto>(
        new ConstantActionsProvider<DataShieldMethodDto>(EDIT_ACTION, DELETE_ACTION));
    methodsTable.addColumn(actionsColumn, translations.actionsLabel());
  }

  @Override
  public HasAuthorization getAddMethodAuthorizer() {
    return new WidgetAuthorizer(addMethodButton);
  }

  @Override
  public HasAuthorization getMethodsAuthorizer() {
    return new WidgetAuthorizer(methodsPanel);
  }

  @Override
  public void setEnvironment(String env) {
    if("assign".equals(env)) {
      assignMethods.setVisible(true);
      aggregateMethods.setVisible(false);
    } else {
      assignMethods.setVisible(false);
      aggregateMethods.setVisible(true);
    }
  }
}
