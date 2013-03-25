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
import java.util.LinkedList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsVariableCopyColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ConstantActionsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.EditableColumn;
import org.obiba.opal.web.gwt.app.client.widgets.view.EditableListBox;
import org.obiba.opal.web.gwt.app.client.wizard.variablestoview.presenter.VariablesToViewPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.Chooser;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.cell.client.TextInputCell;
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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;
import com.watopi.chosen.client.event.ChosenChangeEvent;

/**
 *
 */
public class VariablesToViewView extends PopupViewImpl implements VariablesToViewPresenter.Display {

  private static final int SCRIPT_MAX_LENGTH = 150;

  @UiTemplate("VariablesToViewView.ui.xml")
  interface ViewUiBinder extends UiBinder<DialogBox, VariablesToViewView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

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
  Panel multipleVariablePanel;

  @UiField
  SimplePager pager;

  @UiField
  VariableEditableTable table;

  @UiField
  Panel singleVariablePanel;

  @UiField
  TextBox singleVariable;

  @UiField
  CheckBox renameWithNumber;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  private JsArray<DatasourceDto> datasources;

  private final ListDataProvider<VariableDto> dataProvider = new ListDataProvider<VariableDto>();

  private final TextInputCell cell = new TextInputCell();

  private ActionsVariableCopyColumn<VariableDto> actionsColumn;

  private final int PAGE_SIZE = 10;

  @Inject
  public VariablesToViewView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);

    initWidgets();
    addHandlers();
  }

  private void initWidgets() {
    dialog.setText(translations.addVariablesToViewTitle());
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
  }

  private void addTableColumns() {

    EditableColumn<VariableDto> editColumn = new EditableColumn<VariableDto>(cell) {
      @Override
      public String getValue(VariableDto object) {
        return object.getName();
      }
    };

    table.addColumn(editColumn, translations.nameLabel());
    table.addColumn(new TextColumn<VariableDto>() {
      @Override
      public String getValue(VariableDto object) {
        return getTruncatedScript(object);
      }

      private String getTruncatedScript(VariableDto object) {
        String script = "";
        for(int i = 0; i < JsArrays.toSafeArray(object.getAttributesArray()).length(); i++) {
          if("script".equals(object.getAttributesArray().get(i).getName())) {
            script = object.getAttributesArray().get(i).getValue();
            if(script.length() > SCRIPT_MAX_LENGTH) {
              script = script.substring(0, Math.min(script.length(), SCRIPT_MAX_LENGTH)) + " ...";
            }
            break;
          }
        }
        return script;
      }
    }, translations.scriptLabel());

    actionsColumn = new ActionsVariableCopyColumn<VariableDto>(
        new ConstantActionsProvider<VariableDto>(ActionsVariableCopyColumn.REMOVE_ACTION));
    table.addColumn(actionsColumn, translations.actionsLabel());

    table.setPageSize(PAGE_SIZE);
    table.setEmptyTableWidget(noVariables);
    pager.setDisplay(table);
    dataProvider.addDataDisplay(table);
  }

  @Override
  public void renderRows(List<VariableDto> originalVariables, JsArray<VariableDto> rows, boolean clearNames) {
    // Set all variable names to their original name
    if(clearNames) {
      for(VariableDto v : originalVariables) {
        cell.clearViewData(v.getName());
      }
    }

    dataProvider.setList(JsArrays.toList(JsArrays.toSafeArray(rows)));

    pager.setVisible(dataProvider.getList().size() > PAGE_SIZE);
    if(dataProvider.getList().size() > 1) {
      singleVariablePanel.setVisible(false);
      multipleVariablePanel.setVisible(true);

      dataProvider.refresh();
      table.redraw();
    } else {
      singleVariablePanel.setVisible(true);
      multipleVariablePanel.setVisible(false);
      singleVariable.setText(dataProvider.getList().get(0).getName());
    }

    if(dataProvider.getList().isEmpty()) {
      saveButton.setEnabled(false);
    }
    updateRenameCheckboxVisibility(originalVariables);

  }

  @Override
  public void updateRenameCheckboxVisibility(List<VariableDto> originalVariables) {

    // Show rename categories to number only if there is at least one variable with categories
    boolean isRenameEnabled = false;
    for(VariableDto originalVariable : originalVariables) {
      if(VariableDtos.hasCategories(originalVariable) && ("text".equals(originalVariable.getValueType()) ||
          "integer".equals(originalVariable.getValueType()) && !VariableDtos.allCategoriesMissing(originalVariable))) {
        isRenameEnabled = true;
        break;
      }
    }
    renameWithNumber.setEnabled(isRenameEnabled);
  }

  private void displayViewsFor(String datasourceName) {
    viewListBox.clear();

    DatasourceDto datasource = getDatasource(datasourceName);
    if(datasource != null) {
      List<String> views = toList(datasource.getViewArray());

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
    return dialog;
  }

  @Override
  public void showDialog() {
    renameWithNumber.setValue(false);
    saveButton.setEnabled(true);
    cancelButton.setEnabled(true);

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
    if(datasources.length() > 0) displayViewsFor(name);
  }

  @Override
  public HasClickHandlers getSaveButton() {
    return saveButton;
  }

  @Override
  public HasClickHandlers getCancelButton() {
    return cancelButton;
  }

  @Override
  public ActionsVariableCopyColumn<VariableDto> getActions() {
    return actionsColumn;
  }

  @Override
  public void removeVariable(VariableDto object) {
    List<VariableDto> list = new LinkedList<VariableDto>(dataProvider.getList());
    for(int i = 0; i < list.size(); i++) {
      if(list.get(i).getName().equals(object.getName())) {
        list.remove(i);
        break;
      }
    }

    if(list.isEmpty()) {
      saveButton.setEnabled(false);
    }

    dataProvider.setList(list);
    dataProvider.refresh();
  }

  @Override
  public HasText getViewName() {
    return viewListBox;
  }

  @Override
  public List<VariableDto> getVariables(boolean withNewNames) {
    List<VariableDto> list = new LinkedList<VariableDto>();
    if(dataProvider.getList().size() == 1 && !singleVariable.getText().isEmpty()) {
      VariableDto v = dataProvider.getList().get(0);
      v.setName(singleVariable.getText());
      list.add(v);
    } else {

      // make effective the name changes
      for(VariableDto v : dataProvider.getList()) {
        if(withNewNames) {
          TextInputCell.ViewData vi = cell.getViewData(v.getName());

          if(vi != null) {
            v.setName(vi.getCurrentValue());
          }
        }
        list.add(v);
      }
    }

    return list;
  }

  @Override
  public String getDatasourceName() {
    return datasourceListBox.getSelectedValue();
  }

  @Override
  public boolean isRenameSelected() {
    return renameWithNumber.getValue();
  }

  @Override
  public HasClickHandlers getRenameButton() {
    return renameWithNumber;
  }
}
