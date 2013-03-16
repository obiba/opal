/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.wizard.variablestoview.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.EditableColumn;
import org.obiba.opal.web.gwt.app.client.widgets.view.EditableListBox;
import org.obiba.opal.web.gwt.app.client.wizard.variablestoview.presenter.VariablesToViewPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.Chooser;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.opal.VariableCopyDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;
import com.watopi.chosen.client.event.ChosenChangeEvent;

/**
 *
 */
public class VariablesToViewView extends PopupViewImpl implements VariablesToViewPresenter.Display {

  @UiTemplate("VariablesToViewView.ui.xml")
  interface ViewUiBinder extends UiBinder<DialogBox, VariablesToViewView> {}

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  DialogBox dialog;

  @UiField
  DockLayoutPanel contentLayout;

  @UiField
  ResizeHandle resizeHandle;

  @UiField
  Chooser datasourceListBox;

  @UiField
  EditableListBox viewListBox;

  @UiField
  InlineLabel noVariables;

  @UiField
  Table<VariableCopyDto> table;

  @UiField
  SimplePager pager;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  private JsArray<DatasourceDto> datasources;

  private final ListDataProvider<VariableCopyDto> dataProvider = new ListDataProvider<VariableCopyDto>();
//  private VariableClickableColumn variableNameColumn;

  private final int PAGE_SIZE = 10;

  @Inject
  public VariablesToViewView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    initWidgets();
    addHandlers();
  }

  private void initWidgets() {
    dialog.setText(translations.addVariablesToView());
    resizeHandle.makeResizable(contentLayout);
    addTableColumns();
  }

  private void addHandlers() {
    datasourceListBox.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {

      @Override
      public void onChange(ChosenChangeEvent event) {
        displayViewsFor(datasourceListBox.getSelectedValue());
      }
    });

//    tableListBox.addValueChangeHandler(new ValueChangeHandler<String>() {
//
//      @Override
//      public void onValueChange(ValueChangeEvent<String> event) {
//        boolean knownTable = tableListBox.hasItem(tableListBox.getText());
//        entityTypeListBox.setEnabled(knownTable == false);
//        if(knownTable) {
//          tableSelectionHandler.onTableSelected(getSelectedDatasource(), getSelectedTable());
//        }
//      }
//    });
  }

  private void addTableColumns() {

    table.addColumn(new VariableCopyEditableNameColumn("name") {
      @Override
      public String getValue(VariableCopyDto object) {
        return object.getVariable();
      }
    }, translations.nameLabel());

    table.addColumn(new TextColumn<VariableCopyDto>() {
      @Override
      public String getValue(VariableCopyDto object) {
        return object.getDatasource() + "." + object.getTable() + ":" + object.getVariable();
      }
    }, translations.originalVariable());

    table.setPageSize(PAGE_SIZE);
    table.setEmptyTableWidget(noVariables);
//    table.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(variableIndexColumn, true));
    pager.setDisplay(table);
    dataProvider.addDataDisplay(table);
  }

  @Override
  public void renderRows(JsArray<VariableCopyDto> rows) {
    dataProvider.setList(JsArrays.toList(JsArrays.toSafeArray(rows)));
    pager.firstPage();
    dataProvider.refresh();
  }

  private void displayViewsFor(String datasourceName) {
    viewListBox.clear();

    DatasourceDto datasource = getDatasource(datasourceName);
    if(datasource != null) {
//      List<String> tables = toList(datasource.getTableArray());
      List<String> views = toList(datasource.getViewArray());
//      views.removeAll(tables);

      for(String name : views) {
        viewListBox.addItem(name);
      }
    }
  }

  private DatasourceDto getDatasource(String datasourceName) {
    for(int i = 0; i < datasources.length(); i++) {
      DatasourceDto datasource = datasources.get(i);
      if(datasource.getName().equals(datasourceName)) {
        return datasource;
      }
    }
    return null;
  }

  private List<String> toList(JsArrayString jsArrayString) {
    List<String> list = new ArrayList<String>();
    for(int i = 0; i < jsArrayString.length(); i++) {
      list.add(jsArrayString.get(i));
    }
    return list;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void showDialog() {
    show();
    center();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void setDatasources(JsArray<DatasourceDto> datasources, String name) {
    datasourceListBox.clear();
    for(int i = 0; i < datasources.length(); i++) {
      datasourceListBox.addItem(datasources.get(i).getName(), datasources.get(i).getName());
    }
    datasourceListBox.setSelectedValue(name);
    this.datasources = datasources;
    if(datasources.length() > 0) displayViewsFor(datasources.get(0).getName());
  }

  @Override
  public HasClickHandlers getSaveButton() {
    return saveButton;
  }

  @Override
  public HasClickHandlers getCancelButton() {
    return cancelButton;
  }

  // Inner classes

  private abstract static class VariableCopyEditableNameColumn extends EditableColumn<VariableCopyDto> {

    private final String name;

    private VariableCopyEditableNameColumn(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

  }
}
