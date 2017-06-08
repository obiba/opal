/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.view;

import java.util.AbstractList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ValuesTablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ValuesTablePresenter.DataFetcher;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ValuesTablePresenter.EntitySelectionHandler;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ValuesTableUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.CollapsiblePanel;
import org.obiba.opal.web.gwt.app.client.ui.CriteriaPanel;
import org.obiba.opal.web.gwt.app.client.ui.CriterionDropdown;
import org.obiba.opal.web.gwt.app.client.ui.NumericTextBox;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TableVariableSuggestOracle;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.VariableSuggestOracle;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ClickableColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.IconActionCell;
import org.obiba.opal.web.gwt.app.client.ui.celltable.IconActionCell.Delegate;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ValueColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ValueColumn.ValueSelectionHandler;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueSetDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

@SuppressWarnings("OverlyCoupledClass")
public class ValuesTableView extends ViewWithUiHandlers<ValuesTableUiHandlers> implements ValuesTablePresenter.Display {

  private static final int DEFAULT_MAX_VISIBLE_COLUMNS = 5;

  private static final int DEFAULT_PAGE_SIZE = 20;

  private int page = DEFAULT_PAGE_SIZE;

  @UiTemplate("ValuesTableView.ui.xml")
  interface ValuesTableViewUiBinder extends UiBinder<Widget, ValuesTableView> {}

  private static final ValuesTableViewUiBinder uiBinder = GWT.create(ValuesTableViewUiBinder.class);

  private final Widget widget;

  @UiField
  CollapsiblePanel addPanel;

  @UiField
  ControlGroup valuesFilterGroup;

  @UiField
  OpalSimplePager pager;

  @UiField
  InlineLabel noValues;

  @UiField
  FlowPanel valuesPanel;

  @UiField
  PopupPanel navigationPopup;

  @UiField
  Table<ValueSetDto> valuesTable;

  @UiField
  TextBoxClearable filter;

  @UiField
  NumericTextBox pageSize;

  @UiField
  Image refreshPending;

  @UiField
  FlowPanel searchPanel;

  @UiField
  TextBox searchBox;

  @UiField
  NumericTextBox visibleColumns;

  @UiField
  CriteriaPanel filters;

  @UiField(provided = true)
  Typeahead variableTypeahead;

  @UiField
  ControlGroup searchIdentifierGroup;

  private ValueSetsDataProvider dataProvider;

  private List<VariableDto> listVariable;

  private final List<VariableDto> visibleListVariable;

  private List<String> listValueSetVariable;

  private TableDto table;

  private DataFetcher fetcher;

  private VariableValueSelectionHandler variableValueSelectionHandler;

  private EntitySelectionHandler entitySelectionHandler;

  private int firstVisibleIndex = 0;

  private String lastFilter = "";

  private int maxVisibleColumns = DEFAULT_MAX_VISIBLE_COLUMNS;

  private ValuesTablePresenter.ViewMode viewMode = ValuesTablePresenter.ViewMode.DETAILED_MODE;

  private ValueUpdater<String> updater;

  private final VariableSuggestOracle oracle;

  private final Translations translations = GWT.create(Translations.class);

  @Inject
  public ValuesTableView(EventBus eventBus) {
    // populate Typeahead
    oracle = new TableVariableSuggestOracle(eventBus);
    variableTypeahead = new Typeahead(oracle);
    variableTypeahead.setMinLength(2);
    variableTypeahead.setUpdaterCallback(new Typeahead.UpdaterCallback() {
      @Override
      public String onSelection(SuggestOracle.Suggestion selectedSuggestion) {
        getUiHandlers().onVariableFilter(((VariableSuggestOracle.VariableSuggestion) selectedSuggestion).getVariable());
        return "";
      }
    });

    widget = uiBinder.createAndBindUi(this);
    valuesTable.setEmptyTableWidget(noValues);
    pager.setDisplay(valuesTable);
    pager.setPageSize(DEFAULT_PAGE_SIZE);
    navigationPopup.hide();

    pageSize.setValue(Integer.toString(DEFAULT_PAGE_SIZE), false);
    pageSize.setMin(1);
    pageSize.addKeyUpHandler(new OnEnterSubmitKeyUpHandler());

    visibleColumns.setValue(Integer.toString(DEFAULT_MAX_VISIBLE_COLUMNS), false);
    visibleColumns.setMin(1);
    visibleColumns.addKeyUpHandler(new OnEnterSubmitKeyUpHandler());

    filter.getTextBox().addKeyUpHandler(new FilterOnEnterSubmitKeyUpHandler());
    filter.getClear().setTitle(translations.clearFilter());

    visibleListVariable = new AbstractList<VariableDto>() {

      @Override
      public VariableDto get(int i) {
        return listVariable.get(firstVisibleIndex + i);
      }

      @Override
      public int size() {
        return Math.min(maxVisibleColumns, listVariable.size() - firstVisibleIndex);
      }
    };

    addPanel.setText(translations.displayOptionsLabel());
  }

  @Override
  public void addEntitySearchHandler(final ValuesTablePresenter.EntitySearchHandler handler) {
    searchBox.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          handler.onSearch(searchBox.getText());
        }
      }
    });
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void clearTable() {
    valuesTable.setVisible(false);
    pager.setPagerVisible(false);
    setRefreshing(true);
  }

  @Override
  public void setTable(TableDto table) {
    valuesTable.setEmptyTableWidget(noValues);
    this.table = table;
    valuesTable.setRowCount(table.getValueSetCount());
    valuesTable.setPageStart(0);

    if(dataProvider != null) {
      dataProvider.removeDataDisplay(valuesTable);
      dataProvider = null;
    }

    searchBox.setText("");
    filter.setText("");
    filter.getTextBox().setPlaceholder(translations.filterVariables());
    lastFilter = "";
    filter.getTextBox().setValue(lastFilter, false);
    setRefreshing(false);

    oracle.setTable("\"" + table.getName() + "\"");
    oracle.setDatasource("\"" + table.getDatasourceName() + "\"");
  }

  @Override
  public void setVariables(JsArray<VariableDto> variables) {
    valuesTable.setVisible(true);
    setVariables(JsArrays.toList(variables));
  }

  private String escape(String string) {
    return string.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");
  }

  @Override
  public void setValueSetsFetcher(DataFetcher provider) {
    fetcher = provider;
  }

  @Override
  public void setViewMode(ValuesTablePresenter.ViewMode mode) {
    viewMode = mode;
    searchPanel.setVisible(viewMode == ValuesTablePresenter.ViewMode.DETAILED_MODE);

    if(listVariable != null && !listVariable.isEmpty()) {
      setTable(table);
      setVariables(listVariable);
    }
  }

  public void setRefreshing(boolean refresh) {
    refreshPending.setVisible(refresh);
  }

  //
  // Private methods
  //

  public int getMaxVisibleColumns() {
    return maxVisibleColumns;
  }

  private String getColumnLabel(int i) {
    return listVariable.get(i).getName();
  }

  static class VariableHeaderHtmlRenderer implements SafeHtmlRenderer<String> {
    @Override
    public SafeHtml render(String object) {
      if(object == null) return SafeHtmlUtils.EMPTY_SAFE_HTML;

      return new SafeHtmlBuilder().appendHtmlConstant("<a>").appendEscaped(object).appendHtmlConstant("</a>")
          .toSafeHtml();
    }

    @Override
    public void render(String object, SafeHtmlBuilder builder) {
      builder.append(new SafeHtmlBuilder().appendHtmlConstant("<a>").appendEscaped(object).appendHtmlConstant("</a>")
          .toSafeHtml());
    }
  }

  private Header<String> getColumnHeader(final int i) {

    Header<String> header = new Header<String>(createColumnHeaderCell()) {
      @Override
      public String getValue() {
        return listVariable.get(i).getName();
      }
    };

    header.setUpdater(updater);

    return header;
  }

  @Override
  public void setVariableLabelFieldUpdater(ValueUpdater<String> updater) {
    this.updater = updater;
  }

  private AbstractSafeHtmlCell<String> createColumnHeaderCell() {
    if(viewMode == ValuesTablePresenter.ViewMode.SIMPLE_MODE) {
      return new TextCell();
    }

    return new ClickableTextCell(new VariableHeaderHtmlRenderer());
  }

  private VariableDto getVariableAt(int i) {
    return listVariable.get(i);
  }

  private ValueColumn createColumn(final VariableDto variable) {
    ValueColumn col = new ValueColumn(variable) {
      @Override
      protected int getPosition() {
        // get the position from the list of variable names provided with the value sets
        return listValueSetVariable.indexOf(variable.getName());
      }
    };
    if(variableValueSelectionHandler == null) {
      variableValueSelectionHandler = new VariableValueSelectionHandler();
    }
    col.setValueSelectionHandler(variableValueSelectionHandler);
    return col;
  }

  private void setVariables(List<VariableDto> variables) {
    initValuesTable();
    boolean isExactMatch = false;
    addPanel.setVisible(true);

    insertColumns(variables);

    if(listVariable.size() == 1 && table.getVariableCount() != 1 && filter.getTextBox().getText().isEmpty()) {
      lastFilter = escape(listVariable.get(0).getName());
      filter.getTextBox().setValue(lastFilter, false);
      isExactMatch = true;

      // hide the filter box since we want to show the values only for the current variable
      addPanel.setVisible(false);
    }

    if(dataProvider != null) {
      dataProvider.removeDataDisplay(valuesTable);
      dataProvider = null;
    }
    valuesTable.setPageSize(pageSize.getNumberValue().intValue());
    dataProvider = new ValueSetsDataProvider(isExactMatch);
    dataProvider.addDataDisplay(valuesTable);
    pager.setPagerVisible(valuesTable.getRowCount() > pager.getPageSize());
  }

  private void insertColumns(List<VariableDto> variables) {
    listVariable = variables;
    int visible = listVariable.size() < getMaxVisibleColumns() ? listVariable.size() : getMaxVisibleColumns();
    for(int i = 0; i < visible; i++) {
      valuesTable.addColumn(createColumn(getVariableAt(i)), getColumnHeader(i));
    }

    if(listVariable.size() > getMaxVisibleColumns()) {
      valuesTable.insertColumn(1, createEmptyColumn(), createHeader(new PreviousActionCell()));
      valuesTable.insertColumn(valuesTable.getColumnCount(), createEmptyColumn(), createHeader(new NextActionCell()));
    }
  }

  private void initValuesTable() {
    while(valuesTable.getColumnCount() > 0) {
      valuesTable.removeColumn(0);
    }
    firstVisibleIndex = 0;

    Column<ValueSetDto, ?> entityColumn = viewMode == ValuesTablePresenter.ViewMode.SIMPLE_MODE
        ? createTextEntityColumn()
        : createClickableEntityColumn();

    setMinimumWidth(entityColumn);

    valuesTable.addColumn(entityColumn, "ID");
  }

  private Column<ValueSetDto, ?> createClickableEntityColumn() {
    ClickableColumn<ValueSetDto> entityColumn = new ClickableColumn<ValueSetDto>() {

      @Override
      public String getValue(ValueSetDto value) {
        return value.getIdentifier();
      }

    };

    if(entitySelectionHandler == null) {
      entitySelectionHandler = new EntitySelectionHandlerImpl();
    }

    entityColumn.setFieldUpdater(new FieldUpdater<ValueSetDto, String>() {
      @Override
      public void update(int index, ValueSetDto valueSetDto, String value) {
        entitySelectionHandler.onEntitySelection(table.getEntityType(), valueSetDto.getIdentifier());
      }
    });

    entityColumn.setCellStyleNames("clickable-entity-id");

    return entityColumn;
  }

  private Column<ValueSetDto, ?> createTextEntityColumn() {
    return new TextColumn<ValueSetDto>() {

      @Override
      public String getValue(ValueSetDto value) {
        return value.getIdentifier();
      }
    };
  }

  private Header<String> createHeader(AbstractCell<String> cell) {
    return new Header<String>(cell) {

      @Override
      public String getValue() {
        return null;
      }
    };
  }

  private TextColumn<ValueSetDto> createEmptyColumn() {
    TextColumn<ValueSetDto> emptyColumn = new TextColumn<ValueSetDto>() {

      @Override
      public String getValue(ValueSetDto object) {
        return null;
      }
    };
    setMinimumWidth(emptyColumn);
    return emptyColumn;
  }

  private void setMinimumWidth(Column<ValueSetDto, ?> column) {
    valuesTable.setColumnWidth(column, 1, Unit.PX);
  }

  @Override
  public void setFilterText(String text) {
    if(text != null && !text.isEmpty()) {
      filter.setText(text);

      fetcher.updateVariables(text);
      if(!text.isEmpty()) {
        addPanel.setOpen(true);
      }
    }
  }

  @Override
  public String getFilterText() {
    return filter.getTextBox().getText();
  }

  @Override
  public TextBoxClearable getFilter() {
    return filter;
  }

  @Override
  public void populateValues(int offset, ValueSetsDto resource) {
    if(dataProvider != null) {
      dataProvider.populateValues(offset, resource);
    }
  }

  @Override
  public void clearCriteria() {
    filters.clear();
  }

  @Override
  public boolean hasCriteria() {
    return filters.hasCriteria();
  }

  @Override
  public void addVariableFilter(CriterionDropdown criterion) {
    filters.addCriterion(criterion);
  }

  @Override
  public void addVariableFilter(CriterionDropdown criterion, boolean removeable, boolean opened) {
    filters.addCriterion(criterion, removeable, opened);
  }

  @Override
  public String getQueryString() {
    return filters.getQueryString();
  }

  @Override
  public String getQueryText() {
    return filters.getQueryText();
  }

  @Override
  public ControlGroup getValuesFilterGroup() {
    return valuesFilterGroup;
  }

  @Override
  public int getPageSize() {
    return page;
  }

  @Override
  public void setRowCount(int totalHits) {
    valuesTable.setRowCount(totalHits);
  }

  @Override
  public ControlGroup getSearchIdentifierGroup() {
    return searchIdentifierGroup;
  }

  //
  // Inner classes
  //

  private final class PreviousActionCell extends IconActionCell<String> {

    private PreviousActionCell() {
      super(IconType.CHEVRON_LEFT, new PreviousDelegate());
    }

    @Override
    public boolean isEnabled() {
      return !refreshPending.isVisible() && firstVisibleIndex > 0;
    }

  }

  private final class NextActionCell extends IconActionCell<String> {

    private NextActionCell() {
      super(IconType.CHEVRON_RIGHT, new NextDelegate());
    }

    @Override
    public boolean isEnabled() {
      return !refreshPending.isVisible() && listVariable.size() - firstVisibleIndex > getMaxVisibleColumns();
    }

  }

  private abstract class NavigationDelegate implements Delegate<String> {

    protected static final int MAX_NUMBER_OF_ITEMS = 15;

    private static final int POPUP_DELAY = 500;

    private Timer timer;

    @Override
    public void executeClick(NativeEvent event, String value) {
      if(timer != null) {
        timer.cancel();
      }
      if(navigationPopup.isShowing()) return;

      navigate(1);
      refreshRows();
    }

    @Override
    public void executeMouseDown(final NativeEvent event, String value) {
      navigationPopup.hide();
      timer = new Timer() {

        @Override
        public void run() {
          showMenu(event);
        }

      };
      timer.schedule(POPUP_DELAY);
    }

    protected void showMenu(NativeEvent event) {
      navigationPopup.clear();

      MenuBar menuBar = createMenuBar();
      menuBar.setVisible(true);
      navigationPopup.add(menuBar);

      navigationPopup.setPopupPosition(event.getClientX(), event.getClientY());
      navigationPopup.show();
    }

    protected Command createCommand(final int steps) {
      return new Command() {

        @Override
        public void execute() {
          navigate(steps);
          refreshRows();
          navigationPopup.hide();
        }
      };
    }

    private void refreshRows() {
      setRefreshing(true);
      fetcher.request(visibleListVariable, pager.getPageStart(), pager.getPageSize());
    }

    protected abstract MenuBar createMenuBar();

    protected abstract void navigate(int steps);

  }

  private final class NextDelegate extends NavigationDelegate {

    @Override
    protected MenuBar createMenuBar() {
      MenuBar menuBar = new MenuBar(true);
      int currentIdx = firstVisibleIndex + getMaxVisibleColumns();
      for(int i = currentIdx; i < Math.min(currentIdx + MAX_NUMBER_OF_ITEMS, listVariable.size()); i++) {
        int increment = i - currentIdx + 1;
        menuBar.addItem(new MenuItem(getColumnLabel(i), createCommand(increment)));
      }
      if(Math.min(currentIdx + MAX_NUMBER_OF_ITEMS, listVariable.size()) < listVariable.size()) {
        MenuItem more = new MenuItem("...", (Command) null);
        more.setEnabled(false);
        menuBar.addItem(more);
      }
      return menuBar;
    }

    @Override
    protected void navigate(int steps) {
      for(int i = 0; i < steps; i++) {
        valuesTable.removeColumn(2);
        int idx = firstVisibleIndex++ + getMaxVisibleColumns();
        valuesTable
            .insertColumn(valuesTable.getColumnCount() - 1, createColumn(getVariableAt(idx)), getColumnHeader(idx));
      }
      valuesTable.redrawHeaders();
    }

    @Override
    protected void showMenu(NativeEvent event) {
      super.showMenu(event);
      // adjust the position when we know the popup width
      navigationPopup.setPopupPosition(event.getClientX() - navigationPopup.getOffsetWidth(), event.getClientY());
    }

  }

  private final class PreviousDelegate extends NavigationDelegate {

    @Override
    protected MenuBar createMenuBar() {
      MenuBar menuBar = new MenuBar(true);
      int currentIdx = firstVisibleIndex;
      for(int i = currentIdx - 1; i >= Math.max(currentIdx - MAX_NUMBER_OF_ITEMS, 0); i--) {
        int decrement = currentIdx - i;
        menuBar.addItem(new MenuItem(getColumnLabel(i), createCommand(decrement)));
      }
      if(Math.max(currentIdx - MAX_NUMBER_OF_ITEMS, 0) > 0) {
        MenuItem more = new MenuItem("...", (Command) null);
        more.setEnabled(false);
        menuBar.addItem(more);
      }
      return menuBar;
    }

    @Override
    protected void navigate(int steps) {
      for(int i = 0; i < steps; i++) {
        valuesTable.removeColumn(valuesTable.getColumnCount() - 2);
        int idx = --firstVisibleIndex;
        valuesTable.insertColumn(2, createColumn(getVariableAt(idx)), getColumnLabel(idx));
      }
      valuesTable.redrawHeaders();
    }

  }

  private final class ValueSetsDataProvider extends AbstractDataProvider<ValueSetsDto.ValueSetDto>
      implements ValuesTablePresenter.ValueSetsProvider {

    Range range;

    boolean exactMatch = false;

    private ValueSetsDataProvider(boolean exactMatch) {
      this.exactMatch = exactMatch;
    }

    @Override
    protected void onRangeChanged(HasData<ValueSetDto> display) {
      // Get the new range.
      if(range == null || display.getVisibleRange().getStart() != range.getStart()) {
        range = display.getVisibleRange();

        // Range has changed

        // query the valueSets
        int start = range.getStart();

        if(start > table.getValueSetCount()) return;

        if(filter.getTextBox().getText().isEmpty()) {
          setRefreshing(true);
          fetcher.request(visibleListVariable, start, pager.getPageSize());
        } else {
          setRefreshing(true);
          fetcher.request(filter.getTextBox().getText(), start, pager.getPageSize(), exactMatch);
        }
      }
    }

    @Override
    public void populateValues(int offset, ValueSetsDto valueSets) {
      setRefreshing(false);

      listValueSetVariable = JsArrays.toList(valueSets.getVariablesArray());
      updateRowData(offset, JsArrays.toList(valueSets.getValueSetsArray()));

      valuesTable.setVisibleRange(offset, getPageSize());
    }
  }

  private final class VariableValueSelectionHandler implements ValueSelectionHandler {
    @Override
    public void onBinaryValueSelection(VariableDto variable, int row, int column, ValueSetDto valueSet) {
      fetcher.requestBinaryValue(variable, valueSet.getIdentifier());
    }

    @Override
    public void onGeoValueSelection(VariableDto variable, int row, int column, ValueSetDto valueSet,
        ValueSetsDto.ValueDto value) {
      fetcher.requestGeoValue(variable, valueSet.getIdentifier(), value);
    }

    @Override
    public void onEntityIDSelection(VariableDto variableDto, int row, int column, ValueSetDto valueSet,
        ValueSetsDto.ValueDto value) {
      entitySelectionHandler.onEntitySelection(variableDto.getReferencedEntityType(), value.getValue());
    }

    @Override
    public void onValueSequenceSelection(VariableDto variable, int row, int column, ValueSetDto valueSet) {
      fetcher.requestValueSequence(variable, valueSet.getIdentifier());
    }
  }

  private final class EntitySelectionHandlerImpl implements ValuesTablePresenter.EntitySelectionHandler {

    @Override
    public void onEntitySelection(String entityType, String entityId) {
      fetcher.requestEntitySearch(entityType, entityId);
    }
  }

  private class FilterOnEnterSubmitKeyUpHandler implements KeyUpHandler {

    @Override
    public void onKeyUp(KeyUpEvent event) {
      if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER || filter.getTextBox().getText().isEmpty()) {

        int page = DEFAULT_PAGE_SIZE;
        int columns = DEFAULT_MAX_VISIBLE_COLUMNS;

        if(!pageSize.getText().isEmpty()) {
          page = pageSize.getNumberValue().intValue();
        }
        if(!visibleColumns.getText().isEmpty()) {
          columns = visibleColumns.getNumberValue().intValue();
        }

        if(!lastFilter.equals(filter.getTextBox().getText()) || maxVisibleColumns != columns) {
          // variables list has changed so update all
          lastFilter = filter.getTextBox().getText();
          maxVisibleColumns = columns;
          setRefreshing(true);
          fetcher.updateVariables(filter.getTextBox().getText());
        } else if(valuesTable.getPageSize() != page) {
          // page size only has changed
          setRefreshing(true);
          valuesTable.setPageSize(page);
        }
        // else nothing to refresh
      }
    }
  }

  private class OnEnterSubmitKeyUpHandler implements KeyUpHandler {

    @Override
    public void onKeyUp(KeyUpEvent event) {
      if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {

        int columns = DEFAULT_MAX_VISIBLE_COLUMNS;

        if(!pageSize.getText().isEmpty()) {
          page = pageSize.getNumberValue().intValue();
        }
        if(!visibleColumns.getText().isEmpty()) {
          columns = visibleColumns.getNumberValue().intValue();
        }

        if(!lastFilter.equals(filter.getTextBox().getText()) || maxVisibleColumns != columns) {
          // variables list has changed so update all
          lastFilter = filter.getTextBox().getText();
          maxVisibleColumns = columns;
          setRefreshing(true);
          valuesTable.setPageSize(page);
          fetcher.updateVariables(filter.getTextBox().getText());
        } else if(valuesTable.getPageSize() != page) {
          // page size only has changed
          setRefreshing(true);
          valuesTable.setPageSize(page);
          fetcher.updateVariables(filter.getTextBox().getText());
        }
        setRefreshing(false);
        // else nothing to refresh
      }
    }
  }
}
