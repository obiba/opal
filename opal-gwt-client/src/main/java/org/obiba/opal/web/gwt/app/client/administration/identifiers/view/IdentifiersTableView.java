/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.identifiers.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.IdentifiersTablePresenter;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.IdentifiersTableUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.VariableAttributeColumn;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class IdentifiersTableView extends ViewWithUiHandlers<IdentifiersTableUiHandlers>
    implements IdentifiersTablePresenter.Display {

  private static final int PAGE_SIZE = 20;

  interface Binder extends UiBinder<Widget, IdentifiersTableView> {}

  @UiField
  Heading title;

  @UiField
  Label timestamps;

  @UiField
  Label systemIdsCount;

  @UiField
  Label idMappingsCount;

  @UiField
  Panel mappingsPanel;

  @UiField
  Panel identifiersPanel;

  @UiField
  Button downloadIdentifiers;

  @UiField
  DropdownButton importIdentifiers;

  private final Translations translations;

  private final ListDataProvider<VariableDto> variablesProvider;

  private SimplePager variablesPager;

  private SimplePager valueSetsPager;

  private final ValueSetsDataProvider valueSetsProvider;

  private TableDto table;

  private Table<VariableDto> variablesTable;

  private Table<ValueSetsDto.ValueSetDto> valueSetsTable;

  private JsArrayString variableNames;

  @Inject
  public IdentifiersTableView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    valueSetsProvider = new ValueSetsDataProvider();
    variablesProvider = new ListDataProvider<>();
    this.translations = translations;

    importIdentifiers.setText(translations.importIdentifiers());
  }

  @Override
  public void showIdentifiersTable(TableDto table) {
    this.table = table;
    title.setText(table.getEntityType());
    timestamps.setText(Moment.create(table.getTimestamps().getLastUpdate()).fromNow());
    systemIdsCount.setText("" + table.getValueSetCount());
    idMappingsCount.setText("" + table.getVariableCount());
  }

  @Override
  public void setVariables(JsArray<VariableDto> variables) {
    populateMappingsPanel(variables);
    populateIdentifiersPanel(variables);
  }

  @Override
  public void setValueSets(int offset, ValueSetsDto valueSets) {
    variableNames = valueSets.getVariablesArray();
    List<ValueSetsDto.ValueSetDto> values = JsArrays.toList(valueSets.getValueSetsArray());
    valueSetsProvider.updateRowData(offset, values);
    valueSetsTable.setVisibleRange(offset, valueSetsTable.getPageSize());
    valueSetsPager.setVisible(values.size() >= PAGE_SIZE);
  }

  @UiHandler("deleteIdTable")
  void onDeleteTable(ClickEvent event) {
    getUiHandlers().onDeleteIdentifiersTable();
  }

  @UiHandler("importSystemId")
  void onImportSystemIdentifiers(ClickEvent event) {
    getUiHandlers().onImportSystemIdentifiers();
  }

  @UiHandler("copySystemId")
  void onCopySystemIdentifiers(ClickEvent event) {
    getUiHandlers().onCopySystemIdentifiers();
  }


  @UiHandler("importIdMapping")
  void onImportIdentifiersMapping(ClickEvent event) {
    getUiHandlers().onImportIdentifiersMapping();
  }

  @UiHandler("addIdMapping")
  void onAddIdentifiersMapping(ClickEvent event) {
    getUiHandlers().onAddIdentifiersMapping();
  }

  @UiHandler("downloadIdentifiers")
  void onDownloadIdentifiersMapping(ClickEvent event) {
    getUiHandlers().onDownloadIdentifiers();
  }

  //
  // Private methods
  //

  private class ValueSetsDataProvider extends AsyncDataProvider<ValueSetsDto.ValueSetDto> {
    @Override
    protected void onRangeChanged(HasData<ValueSetsDto.ValueSetDto> display) {
      // Get the new range.
      Range range = display.getVisibleRange();
      getUiHandlers().onIdentifiersRequest(table, "true", range.getStart(), range.getLength());
    }
  }

  private void populateMappingsPanel(JsArray<VariableDto> variables) {
    if(variablesTable != null) {
      variablesProvider.removeDataDisplay(variablesTable);
    }
    mappingsPanel.clear();
    variablesPager = new SimplePager(SimplePager.TextLocation.RIGHT);
    variablesPager.addStyleName("pull-right bottom-margin");
    mappingsPanel.add(variablesPager);
    createAndInitializeVariablesTable();
    mappingsPanel.add(variablesTable);

    variablesProvider.addDataDisplay(variablesTable);
    variablesProvider.setList(JsArrays.toList(variables));
    variablesProvider.refresh();
    variablesPager.setVisible(variablesProvider.getList().size() > variablesTable.getPageSize());
  }

  private void populateIdentifiersPanel(JsArray<VariableDto> variables) {
    if(valueSetsTable != null) {
      valueSetsProvider.removeDataDisplay(valueSetsTable);
    }
    identifiersPanel.clear();

    valueSetsPager = new SimplePager(SimplePager.TextLocation.RIGHT);
    valueSetsPager.addStyleName("pull-right");
    identifiersPanel.add(valueSetsPager);
    createAndInitializeValueSetsTable(variables);
    identifiersPanel.add(valueSetsTable);
    valueSetsProvider.addDataDisplay(valueSetsTable);
  }

  private void createAndInitializeValueSetsTable(JsArray<VariableDto> variables) {
    valueSetsTable = new Table<ValueSetsDto.ValueSetDto>();
    valueSetsTable.setPageSize(PAGE_SIZE);
    valueSetsTable.addColumn(new TextColumn<ValueSetsDto.ValueSetDto>() {

      @Override
      public String getValue(ValueSetsDto.ValueSetDto value) {
        return value.getIdentifier();
      }
    }, "ID");
    valueSetsTable.setColumnWidth(0, 1, Style.Unit.PX);
    for(VariableDto variable : JsArrays.toIterable(variables)) {
      valueSetsTable.addColumn(new IdentifierColumn(variable), variable.getName());
    }
    valueSetsTable.addStyleName("pull-left");
    valueSetsTable.addStyleName("small-top-margin");
    valueSetsPager.setDisplay(valueSetsTable);
    valueSetsTable.setRowCount(table.getValueSetCount());
    valueSetsTable.setPageStart(0);
  }

  private void createAndInitializeVariablesTable() {
    variablesTable = new Table<VariableDto>();
    variablesTable.setPageSize(PAGE_SIZE);
    variablesTable.addColumn(new TextColumn<VariableDto>() {
      @Override
      public String getValue(VariableDto object) {
        return object.getName();
      }
    }, translations.nameLabel());
    variablesTable.addColumn(new VariableAttributeColumn("description"), translations.descriptionLabel());
    variablesTable.addColumn(new VariableActionsColumn(), translations.actionsLabel());
    variablesTable.addStyleName("pull-left");
    variablesTable.addStyleName("small-top-margin");
    variablesPager.setDisplay(variablesTable);
    variablesTable.setPageStart(0);
  }

  private class VariableActionsColumn extends ActionsColumn<VariableDto> {

    public static final String GENERATE_IDS_ACTION = "Generate identifiers";

    public static final String DOWNLOAD_IDS_ACTION = "Download identifiers";

    private VariableActionsColumn() {
      super(new ActionsProvider<VariableDto>() {

        @Override
        public String[] allActions() {
          return new String[] { EDIT_ACTION, REMOVE_ACTION, GENERATE_IDS_ACTION, DOWNLOAD_IDS_ACTION };
        }

        @Override
        public String[] getActions(VariableDto value) {
          return allActions();
        }
      });
      setActionHandler(new ActionHandler<VariableDto>() {
        @Override
        public void doAction(VariableDto object, String actionName) {

          switch(actionName){
            case REMOVE_ACTION:
              getUiHandlers().onDeleteIdentifiersMapping(object);
              break;
            case  EDIT_ACTION:
              getUiHandlers().onEditIdentifiersMapping(object);
              break;
            case GENERATE_IDS_ACTION:
              getUiHandlers().onGenerateIdentifiersMapping(object);
              break;
            case DOWNLOAD_IDS_ACTION:
              getUiHandlers().onDownloadIdentifiers(object);
              break;
          }
        }
      });
    }
  }

  private class IdentifierColumn extends TextColumn<ValueSetsDto.ValueSetDto> {
    private final VariableDto variable;

    private int position = -1;

    IdentifierColumn(VariableDto variable) {
      this.variable = variable;
    }

    @Override
    public String getValue(ValueSetsDto.ValueSetDto valueSet) {
      if(valueSet.getValuesArray() == null || valueSet.getValuesArray().length() <= 0) return "";
      if(position < 0) {
        for(int i = 0; i < variableNames.length(); i++) {
          if(variableNames.get(i).equals(variable.getName())) {
            position = i;
            return valueSet.getValuesArray().get(i).getValue();
          }
        }
      } else {
        return valueSet.getValuesArray().get(position).getValue();
      }
      return "";
    }
  }
}
