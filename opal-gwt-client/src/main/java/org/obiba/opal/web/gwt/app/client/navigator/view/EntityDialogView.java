/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.navigator.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.EntityDialogPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ValueRenderer;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;
import org.obiba.opal.web.gwt.app.client.workbench.view.TableChooser;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
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

public class EntityDialogView extends PopupViewImpl implements EntityDialogPresenter.Display {

  private static final int PAGE_SIZE = 20;

  @UiTemplate("EntityDialogView.ui.xml")
  interface EntityViewUiBinder extends UiBinder<DialogBox, EntityDialogView> {}

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
  TextBox filter;

  @UiField
  Button closeButton;

  @UiField
  ResizeHandle resizeHandle;

  private final Widget widget;

  private final Translations translations = GWT.create(Translations.class);

  private final ListDataProvider<EntityDialogPresenter.VariableValueRow> dataProvider
      = new ListDataProvider<EntityDialogPresenter.VariableValueRow>();

  private final ValueSelectionHandlerImpl valueSelectionHandler;

  private EntityDialogPresenter.VariablesFilterHandler variablesFilterHandler;

  private EntityDialogPresenter.ValueViewHandler valueViewHandler;

  @Inject
  public EntityDialogView(EventBus eventBus) {
    super(eventBus);
    tableChooser = new TableChooser(false);
    widget = uiBinder.createAndBindUi(this);
    resizeHandle.makeResizable(content);
    valueSelectionHandler = new ValueSelectionHandlerImpl();
    initializeTable();
    initializeDisplayOptions();
    dialog.hide();
  }

  private void initializeDisplayOptions() {
    filter.setPlaceholder(translations.filterVariables());
    filter.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER || filter.getText().isEmpty()) {
          // variables list has changed so update all
          variablesFilterHandler.filterVariables(filter.getText());
        }
      }
    });
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
    clear();
    tableChooser.addTableSelections(tables);
    tableChooser.selectTable(selectedTable);
  }

  @Override
  public void setValueViewHandler(EntityDialogPresenter.ValueViewHandler handler) {
    valueViewHandler = handler;
  }

  @Override
  public void setVariablesFilterHandler(EntityDialogPresenter.VariablesFilterHandler handler) {
    variablesFilterHandler = handler;
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
  public void renderRows(List<EntityDialogPresenter.VariableValueRow> rows) {
    dataProvider.setList(rows);
    pager.firstPage();
    dataProvider.refresh();
  }

  @Override
  public void clearFilter() {
    filter.setText("");
  }

  private void clear() {
    tableChooser.clear();

    while(table.getColumnCount() > 0) {
      table.removeColumn(0);
    }

    clearFilter();
    addTableColumns();
  }

  private void addTableColumns() {

    // Variable name column
    table.addColumn(new TextColumn<EntityDialogPresenter.VariableValueRow>() {
      @Override
      public String getValue(EntityDialogPresenter.VariableValueRow object) {
        return object.getVariableName();
      }
    }, translations.variableLabel());

    // Variable value column having each cell of different type (text, binary, repeatable)
    VariableValueRowClickableColumn valueColumn = new VariableValueRowClickableColumn();

    valueColumn.setFieldUpdater(
        new FieldUpdater<EntityDialogPresenter.VariableValueRow, EntityDialogPresenter.VariableValueRow>() {
          @Override
          public void update(int index, EntityDialogPresenter.VariableValueRow variableValueRow,
              EntityDialogPresenter.VariableValueRow value) {
            valueSelectionHandler.onValueSelection(variableValueRow);
          }
        });

    table.addColumn(valueColumn, translations.valueLabel());
  }

  private void initializeTable() {
    dataProvider.addDataDisplay(table);
    table.setPageSize(PAGE_SIZE);
    table.setEmptyTableWidget(noTables);
    pager.setDisplay(table);
  }

  /**
   * Class used to request the appropriate variable value view
   */
  private final class ValueSelectionHandlerImpl implements EntityDialogPresenter.ValueSelectionHandler {

    @Override
    public void onValueSelection(EntityDialogPresenter.VariableValueRow variableValueRow) {

      VariableDto variable = variableValueRow.getVariableDto();

      if(variable.getIsRepeatable() || "binary".equalsIgnoreCase(variable.getValueType())) {
        if(variable.getIsRepeatable()) {
          valueViewHandler.requestValueSequenceView(variable);
        } else {
          valueViewHandler.requestBinaryValueView(variable);
        }
      }
    }
  }

  /**
   * Specialized column class using ClickableValueCell
   */
  private static class VariableValueRowClickableColumn
      extends Column<EntityDialogPresenter.VariableValueRow, EntityDialogPresenter.VariableValueRow> {

    private VariableValueRowClickableColumn() {
      super(new ClickableValueCell());
    }

    @Override
    public EntityDialogPresenter.VariableValueRow getValue(EntityDialogPresenter.VariableValueRow object) {
      return object;
    }

  }

  /**
   * Specialized class to render each cell depending on the variable type (text, repeatable, binary)
   */
  public static class ClickableValueCell extends AbstractSafeHtmlCell<EntityDialogPresenter.VariableValueRow> {

    public ClickableValueCell() {
      this(new AbstractSafeHtmlRenderer<EntityDialogPresenter.VariableValueRow>() {
        @Override
        public SafeHtml render(EntityDialogPresenter.VariableValueRow object) {
          String valueStr = renderValue(object);
          if(valueStr == null || valueStr.trim().isEmpty()) return new SafeHtmlBuilder().toSafeHtml();
          if(object.getVariableDto().getValueType().compareToIgnoreCase("binary") == 0) {
            return renderLink(valueStr, "i-down");
          }
          if(object.getVariableDto().getIsRepeatable()) {
            return renderLink(valueStr, "i-list");
          }
          return SimpleSafeHtmlRenderer.getInstance().render(valueStr);
        }

        private SafeHtml renderLink(String valueStr, String iconClass) {
          return new SafeHtmlBuilder().appendHtmlConstant("<a class=\"iconb " + iconClass + "\">")
              .appendEscaped(valueStr).appendHtmlConstant("</a>").toSafeHtml();
        }

        private String renderValue(EntityDialogPresenter.VariableValueRow row) {
          ValueRenderer valueRender = ValueRenderer.valueOf(row.getVariableDto().getValueType().toUpperCase());
          return valueRender.render(row.getValueDto(), row.getVariableDto().getIsRepeatable());
        }
      });
    }

    public ClickableValueCell(SafeHtmlRenderer<EntityDialogPresenter.VariableValueRow> renderer) {
      super(renderer, "click", "keydown");
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, EntityDialogPresenter.VariableValueRow value,
        NativeEvent event, ValueUpdater<EntityDialogPresenter.VariableValueRow> valueUpdater) {
      super.onBrowserEvent(context, parent, value, event, valueUpdater);
      if("click".equals(event.getType())) {
        onEnterKeyDown(context, parent, value, event, valueUpdater);
      }
    }

    @Override
    protected void onEnterKeyDown(Context context, Element parent, EntityDialogPresenter.VariableValueRow value,
        NativeEvent event, ValueUpdater<EntityDialogPresenter.VariableValueRow> valueUpdater) {
      if(valueUpdater != null) {
        valueUpdater.update(value);
      }
    }

    @Override
    protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
      if(value != null) {
        sb.append(value);
      }
    }
  }

}
