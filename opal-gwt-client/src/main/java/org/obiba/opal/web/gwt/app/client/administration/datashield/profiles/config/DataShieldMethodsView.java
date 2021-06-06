/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.config;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.view.client.ListDataProvider;
import org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.DataShieldProfilePresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.support.FilterHelper;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ConstantActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.datashield.DataShieldMethodDto;
import org.obiba.opal.web.model.client.datashield.RFunctionDataShieldMethodDto;
import org.obiba.opal.web.model.client.datashield.RScriptDataShieldMethodDto;

import com.github.gwtbootstrap.client.ui.Button;
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

import java.util.List;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

/**
 *
 */
public class DataShieldMethodsView extends ViewImpl implements DataShieldMethodsPresenter.Display {

  private static final int PAGE_SIZE = 20;

  interface Binder extends UiBinder<Widget, DataShieldMethodsView> {}

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

  @UiField
  TextBoxClearable filter;

  private final ListDataProvider<DataShieldMethodDto> methodsDataProvider = new ListDataProvider<DataShieldMethodDto>();

  private ActionsColumn<DataShieldMethodDto> actionsColumn;

  private List<DataShieldMethodDto> originalMethods;

  @Inject
  public DataShieldMethodsView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initMethodsTable();
    filter.getTextBox().setPlaceholder(translations.filterDataShieldMethods());
  }

  @UiHandler("filter")
  public void onFilterUpdate(KeyUpEvent event) {
    renderMethods(filterMethods(filter.getText()));
  }

  @Override
  public HandlerRegistration addMethodHandler(ClickHandler handler) {
    return addMethodButton.addClickHandler(handler);
  }

  @Override
  public void showDataShieldMethods(List<DataShieldMethodDto> rows) {
    originalMethods = rows;
    renderMethods(rows);
  }

  private List<DataShieldMethodDto> filterMethods(String query) {
    List<DataShieldMethodDto> methods = Lists.newArrayList();
    if (originalMethods == null || originalMethods.isEmpty()) return methods;
    List<String> tokens = FilterHelper.tokenize(query);
    for (DataShieldMethodDto method : originalMethods) {
      RFunctionDataShieldMethodDto dto = (RFunctionDataShieldMethodDto) method
          .getExtension(RFunctionDataShieldMethodDto.DataShieldMethodDtoExtensions.method);
      String packageName = dto == null ? "" : dto.getRPackage();
      String code = "";
      if (dto != null)
        code = dto.getFunc();
      else
        code = ((RScriptDataShieldMethodDto) method.getExtension(RScriptDataShieldMethodDto.DataShieldMethodDtoExtensions.method)).getScript();
      if (FilterHelper.matches(Joiner.on(" ").join(method.getName(), packageName, code), tokens)) methods.add(method);
    }
    return methods;
  }

  private void renderMethods(List<DataShieldMethodDto> rows) {
    methodsDataProvider.setList(rows);
    int size = methodsDataProvider.getList().size();
    methodsTablePager.firstPage();
    methodsDataProvider.refresh();
  }

  @Override
  public void setMethodActionHandler(ActionHandler<DataShieldMethodDto> handler) {
    actionsColumn.setActionHandler(handler);
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
        RFunctionDataShieldMethodDto fdto = (RFunctionDataShieldMethodDto) object
            .getExtension(RFunctionDataShieldMethodDto.DataShieldMethodDtoExtensions.method);
        if (fdto != null)
          return fdto.getFunc();
        RScriptDataShieldMethodDto sdto = (RScriptDataShieldMethodDto) object
            .getExtension(RScriptDataShieldMethodDto.DataShieldMethodDtoExtensions.method);
        return sdto == null ? "" : sdto.getScript();
      }
    }, translations.rCodeLabel());


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
    return new CompositeAuthorizer(new WidgetAuthorizer(addMethodButton), new HasAuthorization() {
      @Override
      public void beforeAuthorization() {

      }

      @Override
      public void authorized() {

      }

      @Override
      public void unauthorized() {
        methodsTable.removeColumn(actionsColumn);
      }
    });
  }

  @Override
  public HasAuthorization getMethodsAuthorizer() {
    return new WidgetAuthorizer(methodsPanel);
  }

  @Override
  public void setEnvironment(String env) {
    if(DataShieldProfilePresenter.DataShieldEnvironment.ASSIGN.equals(env)) {
      assignMethods.setVisible(true);
      aggregateMethods.setVisible(false);
    } else {
      assignMethods.setVisible(false);
      aggregateMethods.setVisible(true);
    }
  }
}
