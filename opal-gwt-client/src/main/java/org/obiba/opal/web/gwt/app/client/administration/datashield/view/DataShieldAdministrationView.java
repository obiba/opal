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

import static org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldAdministrationPresenter.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldAdministrationPresenter.EDIT_ACTION;

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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class DataShieldAdministrationView extends Composite implements DataShieldAdministrationPresenter.Display {

  @UiTemplate("DataShieldAdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DataShieldAdministrationView> {
  }

  //
  // Constants
  //

  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  Button addMethodButton;

  @UiField
  CellTable<DataShieldMethodDto> methodsTable;

  @UiField
  SimplePager methodsTablePager;

  private JsArrayDataProvider<DataShieldMethodDto> methodsProvider = new JsArrayDataProvider<DataShieldMethodDto>();

  private ActionsColumn<DataShieldMethodDto> methodActionsColumn;

  //
  // Constructors
  //

  public DataShieldAdministrationView() {
    super();
    initWidget(uiBinder.createAndBindUi(this));
    initMethodsTable();
  }

  //
  // AdministrationPresenter.Display Methods
  //

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {

  }

  @Override
  public void stopProcessing() {

  }

  @Override
  public HandlerRegistration addMethodHandler(ClickHandler handler) {
    return addMethodButton.addClickHandler(handler);
  }

  @Override
  public void renderDataShieldMethodsRows(JsArray<DataShieldMethodDto> rows) {
    methodsProvider.setArray(rows);

    int size = methodsProvider.getList().size();
    methodsTablePager.firstPage();
    methodsTablePager.setVisible(size > 0);
    methodsTable.setVisible(size > 0);
    methodsProvider.refresh();
  }

  @Override
  public HasActionHandler<DataShieldMethodDto> getDataShieldMethodActionsColumn() {
    return methodActionsColumn;
  }

  //
  // Methods
  //

  private void initMethodsTable() {

    addMethodsTableColumns();

    methodsTable.setPageSize(50);
    methodsTablePager.setDisplay(methodsTable);
    methodsProvider.addDataDisplay(methodsTable);
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
        if(object.getExtension(RScriptDataShieldMethodDto.DataShieldMethodDtoExtensions.method) != null) {
          return translations.rScriptLabel();
        } else
          return translations.rFunctionLabel();
      }
    }, translations.typeLabel());

    methodActionsColumn = new ActionsColumn<DataShieldMethodDto>(new ConstantActionsProvider<DataShieldMethodDto>(EDIT_ACTION, DELETE_ACTION));
    methodsTable.addColumn(methodActionsColumn, translations.actionsLabel());
  }

  @Override
  public HasAuthorization getAddMethodAuthorizer() {
    return new WidgetAuthorizer(addMethodButton);
  }

}
