package org.obiba.opal.web.gwt.app.client.navigator.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.EntityDialogPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ClickableColumn;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;
import org.obiba.opal.web.gwt.app.client.workbench.view.TableChooser;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 *
 */
public class EntityDialogView extends PopupViewImpl implements EntityDialogPresenter.Display {

  @UiTemplate("EntityDialogView.ui.xml")
  interface EntityViewUiBinder extends UiBinder<DialogBox, EntityDialogView> {
  }

  private static final EntityViewUiBinder uiBinder = GWT.create(EntityViewUiBinder.class);

  @UiField
  DialogBox dialog;

  @UiField
  DockLayoutPanel content;

  @UiField
  Label entityType;

  @UiField
  Label entityId;

  @UiField
  CellTable<EntityDialogPresenter.VariableValueRow> table;

  @UiField
  SimplePager pager;

  @UiField
  InlineLabel noTables;

  @UiField(provided = true)
  TableChooser tableChooser;

  @UiField
  Button closeButton;

  @UiField
  ResizeHandle resizeHandle;


  private final Widget widget;

  private Translations translations = GWT.create(Translations.class);

  private ListDataProvider<EntityDialogPresenter.VariableValueRow> dataProvider = new ListDataProvider<EntityDialogPresenter.VariableValueRow>();

  @Inject
  public EntityDialogView(EventBus eventBus) {
    super(eventBus);
    tableChooser = new TableChooser(false);
    widget = uiBinder.createAndBindUi(this);
    resizeHandle.makeResizable(content);
    addTableColumns();
    dialog.hide();
  }

  @Override
  public void show() {
    dialog.center();
    super.show();
  }

  @Override
  public void setEntityType(String entityType) {
    this.entityType.setText(entityType);
  }

  @Override
  public void setEntityId(String entityId) {
    this.entityId.setText(entityId);
  }

  @Override
  public void setTables(JsArray<TableDto> tables, TableDto selectedTable) {
    tableChooser.clear();
    tableChooser.addTableSelections(tables);
    tableChooser.selectTable(selectedTable);
  }

  @Override
  public TableDto getSelectedTable() {
    List<TableDto> tables = tableChooser.getSelectedTables();
    // there is only one table since the chooser is not multi select
    return tables.isEmpty() ? null : tables.get(0);
  }

  @Override
  public HasChangeHandlers getTableChooser() {
    return tableChooser;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected PopupPanel asPopupPanel() {
    return dialog;
  }

  @Override
  public HasClickHandlers getButton() {
    return closeButton;
  }

  @Override
  public void renderRows(final List<EntityDialogPresenter.VariableValueRow> rows) {
    dataProvider.setList(rows);
    pager.firstPage();
    dataProvider.refresh();
  }

  private void addTableColumns() {

    table.addColumn(new TextColumn<EntityDialogPresenter.VariableValueRow>() {
      @Override
      public String getValue(EntityDialogPresenter.VariableValueRow object) {
        return object.getVariable();
      }
    }, translations.variableLabel());

    table.addColumn(new ClickableColumn<EntityDialogPresenter.VariableValueRow>() {
      @Override
      public String getValue(EntityDialogPresenter.VariableValueRow object) {
        // TODO needs more sophisticated rendering, for now we merely print everything as string
        // need to take care of binary and repeated values

        JsArray<ValueSetsDto.ValueDto> valueDtoList = object.getValueDto().getValuesArray();

        if (valueDtoList.length() > 1) {
          return beatuifyRepeatableValues(valueDtoList);
        }
        else {
          if(object.getValueDto().hasLink()) {
            return object.getValueDto().getLink();
          }
          return object.getValueDto().getValue();
        }
      }
    }, translations.variableLabel());

    dataProvider.addDataDisplay(table);
    table.setPageSize(NavigatorView.PAGE_SIZE);
    table.setEmptyTableWidget(noTables);
    pager.setDisplay(table);
  }

  private String beatuifyRepeatableValues(JsArray<ValueSetsDto.ValueDto> valueDtoList) {
    List<String> values = new ArrayList<String>();
    int count = Math.min(valueDtoList.length(), 3);

    for (int i = 0; i < count; i++) {
      String value = valueDtoList.get(i).getValue();

      if (!value.isEmpty()) {
        values.add(value);
      }
    }

    if (values.isEmpty()) {
        return "";
    }

    String value = Joiner.on(", ").join(values);

    if (valueDtoList.length() > count) {
      value += ", ...";
    }

    return value;
  }

}
