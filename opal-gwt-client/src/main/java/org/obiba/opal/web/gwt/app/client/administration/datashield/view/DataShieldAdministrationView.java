/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield.view;

import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldConfigPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ConstantActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.datashield.DataShieldMethodDto;
import org.obiba.opal.web.model.client.datashield.RFunctionDataShieldMethodDto;
import org.obiba.opal.web.model.client.datashield.RScriptDataShieldMethodDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.ViewImpl;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

/**
 *
 */
public class DataShieldAdministrationView extends ViewImpl implements DataShieldAdministrationPresenter.Display {

  private static final int PAGE_SIZE = 10;

  interface Binder extends UiBinder<Widget, DataShieldAdministrationView> {}

  private final Translations translations;

  @UiField
  Panel methodsPanel;

  @UiField
  Button addMethodButton;

  @UiField
  CellTable<DataShieldMethodDto> methodsTable;

  @UiField
  OpalSimplePager methodsTablePager;

  @UiField
  FlowPanel aggregateMethods;

  @UiField
  FlowPanel assignMethods;

  private final JsArrayDataProvider<DataShieldMethodDto> methodsDataProvider
      = new JsArrayDataProvider<DataShieldMethodDto>();

  private ActionsColumn<DataShieldMethodDto> actionsColumn;

  @Inject
  public DataShieldAdministrationView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initMethodsTable();
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
    methodsTable.setVisible(size > 0);
    methodsDataProvider.refresh();
    methodsTablePager.setPagerVisible(size > methodsTablePager.getPageSize());
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

    methodsTable.addColumn(new TextColumn<DataShieldMethodDto>() {
      @Override
      public String getValue(DataShieldMethodDto object) {
        RFunctionDataShieldMethodDto dto = (RFunctionDataShieldMethodDto) object
            .getExtension(RFunctionDataShieldMethodDto.DataShieldMethodDtoExtensions.method);
        return dto == null ? "" : dto.getRPackage();
      }
    }, translations.packageLabel());

    methodsTable.addColumn(new TextColumn<DataShieldMethodDto>() {
      @Override
      public String getValue(DataShieldMethodDto object) {
        RFunctionDataShieldMethodDto dto = (RFunctionDataShieldMethodDto) object
            .getExtension(RFunctionDataShieldMethodDto.DataShieldMethodDtoExtensions.method);
        return dto == null ? "" : dto.getVersion();
      }
    }, translations.versionLabel());

    actionsColumn = new ActionsColumn<DataShieldMethodDto>(
        new ConstantActionsProvider<DataShieldMethodDto>(EDIT_ACTION, REMOVE_ACTION));
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
    if(DataShieldConfigPresenter.DataShieldEnvironment.ASSIGN.equals(env)) {
      assignMethods.setVisible(true);
      aggregateMethods.setVisible(false);
    } else {
      assignMethods.setVisible(false);
      aggregateMethods.setVisible(true);
    }
  }
}
