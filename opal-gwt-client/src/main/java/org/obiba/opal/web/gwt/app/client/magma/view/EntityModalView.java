/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.presenter.EntityModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.EntityModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.TableChooser;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ValueRenderer;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Strings;
import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class EntityModalView extends ModalPopupViewWithUiHandlers<EntityModalUiHandlers>
    implements EntityModalPresenter.Display {

  private static final int PAGE_SIZE = 20;

  private static final int MIN_WIDTH = 700;

  private static final int MIN_HEIGHT = 500;

  interface EntityViewUiBinder extends UiBinder<Widget, EntityModalView> {}

  private static final EntityViewUiBinder uiBinder = GWT.create(EntityViewUiBinder.class);

  @UiField
  Modal dialog;

  @UiField
  Label entityType;

  @UiField
  Label entityId;

  @UiField
  Panel valuesPanel;

  @UiField
  CellTable<EntityModalPresenter.VariableValueRow> table;

  @UiField
  OpalSimplePager pager;

  @UiField
  InlineLabel noValues;

  @UiField
  TableChooser tableChooser;

  @UiField
  TextBoxClearable filter;

  private final Widget widget;

  private final Translations translations = GWT.create(Translations.class);

  private final ListDataProvider<EntityModalPresenter.VariableValueRow> dataProvider
      = new ListDataProvider<EntityModalPresenter.VariableValueRow>();

  private final ValueSelectionHandlerImpl valueSelectionHandler;

  private EntityModalPresenter.ValueViewHandler valueViewHandler;

  @Inject
  public EntityModalView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    valueSelectionHandler = new ValueSelectionHandlerImpl();
    initializeTable();
    initializeDisplayOptions();
    dialog.setTitle(translations.entityDetailsModalTitle());
    dialog.setMinWidth(MIN_WIDTH);
    dialog.setMinHeight(MIN_HEIGHT);
  }

  private void initializeDisplayOptions() {
    filter.getClear().setTitle(translations.clearFilter());
    filter.getTextBox().setPlaceholder(translations.filterVariables());
  }

  @UiHandler("filter")
  public void onFilterSelected(KeyUpEvent event) {
    if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER || filter.getTextBox().getText().isEmpty()) {
      // variables list has changed so update all
      getUiHandlers().filterVariables(filter.getTextBox().getText());
    }
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
    valuesPanel.setVisible(tables.length() > 0);
    if(tables.length() > 0) {
      tableChooser.addTableSelections(tables);
      tableChooser.selectTable(selectedTable);
    }
  }

  @Override
  public void setValueViewHandler(EntityModalPresenter.ValueViewHandler handler) {
    valueViewHandler = handler;
  }

  @UiHandler("tableChooser")
  public void onTableChooserChanged(ChangeEvent event) {
    getUiHandlers().selectTable(getSelectedTable());
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @UiHandler("closeButton")
  public void onCloseButton(ClickEvent event) {
    dialog.hide();
  }

  @Override
  public void renderRows(List<EntityModalPresenter.VariableValueRow> rows) {
    dataProvider.setList(rows);
    pager.firstPage();
    dataProvider.refresh();
    pager.setPagerVisible(dataProvider.getList().size() > pager.getPageSize());
  }

  @UiHandler("filter")
  public void onFilterSelected(ClickEvent event) {
    getUiHandlers().loadVariables();
  }

  @Override
  public String getFilterText() {
    return filter.getText();
  }

  @Override
  public void setFilterText(String text) {
    filter.setText(text);
  }

  private void clear() {
    tableChooser.clear();

    while(table.getColumnCount() > 0) {
      table.removeColumn(0);
    }

    addTableColumns();
  }

  private void addTableColumns() {

    // Variable name column
    table.addColumn(new TextColumn<EntityModalPresenter.VariableValueRow>() {
      @Override
      public String getValue(EntityModalPresenter.VariableValueRow object) {
        return object.getVariableName();
      }
    }, translations.variableLabel());

    // Variable value column having each cell of different type (text, binary, repeatable)
    VariableValueRowClickableColumn valueColumn = new VariableValueRowClickableColumn();

    valueColumn.setFieldUpdater(
        new FieldUpdater<EntityModalPresenter.VariableValueRow, EntityModalPresenter.VariableValueRow>() {
          @Override
          public void update(int index, EntityModalPresenter.VariableValueRow variableValueRow,
              EntityModalPresenter.VariableValueRow value) {
            valueSelectionHandler.onValueSelection(variableValueRow);
          }
        });

    table.addColumn(valueColumn, translations.valueLabel());
  }

  private void initializeTable() {
    dataProvider.addDataDisplay(table);
    table.setPageSize(PAGE_SIZE);
    table.setEmptyTableWidget(noValues);
    pager.setDisplay(table);
  }

  private TableDto getSelectedTable() {
    List<TableDto> tables = tableChooser.getSelectedTables();
    // there is only one table since the chooser is not multi select
    return tables.isEmpty() ? null : tables.get(0);
  }

  /**
   * Class used to request the appropriate variable value view
   */
  private final class ValueSelectionHandlerImpl implements EntityModalPresenter.ValueSelectionHandler {

    @Override
    public void onValueSelection(EntityModalPresenter.VariableValueRow variableValueRow) {

      VariableDto variable = variableValueRow.getVariableDto();

      if(variable.getIsRepeatable()) {
        valueViewHandler.requestValueSequenceView(variable);
      } else if("binary".equalsIgnoreCase(variable.getValueType())) {
        valueViewHandler.requestBinaryValueView(variable);
      } else if(variable.getValueType().matches("point|linestring|polygon")) {
        valueViewHandler.requestGeoValueView(variable, variableValueRow.getValueDto());
      } else if("text".equalsIgnoreCase(variable.getValueType()) &&
          !Strings.isNullOrEmpty(variable.getReferencedEntityType())) {
        valueViewHandler.requestEntityView(variable, variableValueRow.getValueDto());
      }
    }
  }

  /**
   * Specialized column class using ClickableValueCell
   */
  private static class VariableValueRowClickableColumn
      extends Column<EntityModalPresenter.VariableValueRow, EntityModalPresenter.VariableValueRow> {

    private VariableValueRowClickableColumn() {
      super(new ClickableValueCell());
    }

    @Override
    public EntityModalPresenter.VariableValueRow getValue(EntityModalPresenter.VariableValueRow object) {
      return object;
    }

  }

  /**
   * Specialized class to render each cell depending on the variable type (text, repeatable, binary)
   */
  public static class ClickableValueCell extends AbstractSafeHtmlCell<EntityModalPresenter.VariableValueRow> {

    public ClickableValueCell() {
      this(new AbstractSafeHtmlRenderer<EntityModalPresenter.VariableValueRow>() {
        @Override
        public SafeHtml render(EntityModalPresenter.VariableValueRow object) {
          String valueStr = renderValue(object);
          if(valueStr == null || valueStr.trim().isEmpty()) return new SafeHtmlBuilder().toSafeHtml();
          if(object.getVariableDto().getIsRepeatable()) {
            return renderLink(valueStr, IconType.LIST);
          }
          if(object.getVariableDto().getValueType().equalsIgnoreCase("binary")) {
            return renderLink(valueStr, IconType.DOWNLOAD);
          }
          if(object.getVariableDto().getValueType().matches("point|linestring|polygon")) {
            return renderLink(valueStr, IconType.MAP_MARKER);
          }
          if(object.getVariableDto().getValueType().equalsIgnoreCase("text") &&
              !Strings.isNullOrEmpty(object.getVariableDto().getReferencedEntityType())) {
            return renderLink(valueStr, IconType.ELLIPSIS_VERTICAL);
          }
          return SimpleSafeHtmlRenderer.getInstance().render(valueStr);
        }

        private SafeHtml renderLink(String valueStr, IconType iconType) {
          Icon i = new Icon(iconType);
          i.setIconSize(IconSize.LARGE);
          i.addStyleName("xsmall-right-indent");
          return new SafeHtmlBuilder().appendHtmlConstant("<a class=\"iconb\">").appendHtmlConstant(i.toString())
              .appendEscaped(valueStr).appendHtmlConstant("</a>").toSafeHtml();
        }

        private String renderValue(EntityModalPresenter.VariableValueRow row) {
          ValueRenderer valueRender = ValueRenderer.valueOf(row.getVariableDto().getValueType().toUpperCase());
          return valueRender.render(row.getValueDto(), row.getVariableDto().getIsRepeatable());
        }
      });
    }

    public ClickableValueCell(SafeHtmlRenderer<EntityModalPresenter.VariableValueRow> renderer) {
      super(renderer, "click", "keydown");
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, EntityModalPresenter.VariableValueRow value,
        NativeEvent event, ValueUpdater<EntityModalPresenter.VariableValueRow> valueUpdater) {
      super.onBrowserEvent(context, parent, value, event, valueUpdater);
      if("click".equals(event.getType())) {
        onEnterKeyDown(context, parent, value, event, valueUpdater);
      }
    }

    @Override
    protected void onEnterKeyDown(Context context, Element parent, EntityModalPresenter.VariableValueRow value,
        NativeEvent event, ValueUpdater<EntityModalPresenter.VariableValueRow> valueUpdater) {
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
