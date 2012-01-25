/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.ValuesTablePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.ValuesTablePresenter.DataFetcher;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.ValuesTablePresenter.ValueSetsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.IconActionCell;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.IconActionCell.Delegate;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ValueColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ValueColumn.ValueSelectionHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.NumericTextBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueSetDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.gwtplatform.mvp.client.ViewImpl;

public class ValuesTableView extends ViewImpl implements ValuesTablePresenter.Display {

  private static final int DEFAULT_MAX_VISIBLE_COLUMNS = 5;

  private static final int DEFAULT_PAGE_SIZE = 20;

  @UiTemplate("ValuesTableView.ui.xml")
  interface ValuesTableViewUiBinder extends UiBinder<Widget, ValuesTableView> {
  }

  private static ValuesTableViewUiBinder uiBinder = GWT.create(ValuesTableViewUiBinder.class);

  private final Widget widget;

  @UiField
  SimplePager pager;

  @UiField
  InlineLabel noValues;

  @UiField
  FlowPanel valuesPanel;

  @UiField
  PopupPanel navigationPopup;

  @UiField
  Table<ValueSetDto> valuesTable;

  @UiField
  TextBox filter;

  @UiField
  NumericTextBox pageSize;

  @UiField
  Button refreshButton;

  @UiField
  Image refreshPending;

  private ValueSetsDataProvider dataProvider;

  private List<VariableDto> listVariable;

  private TableDto table;

  private DataFetcher fetcher;

  private int firstVisibleIndex = 0;

  private String lastFilter = "";

  public ValuesTableView() {
    widget = uiBinder.createAndBindUi(this);
    valuesTable.setEmptyTableWidget(noValues);
    pager.setDisplay(valuesTable);
    pager.setPageSize(DEFAULT_PAGE_SIZE);
    navigationPopup.hide();

    pageSize.setValue(Integer.toString(DEFAULT_PAGE_SIZE), false);

    refreshButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if(lastFilter.equals(filter.getText()) == false) {
          // variables list has changed so update all
          lastFilter = filter.getText();
          String select = "";
          if(filter.getText().isEmpty() == false) {
            select = "name().matches(/" + filter.getText() + "/)";
          }
          setRefreshing(true);
          fetcher.updateVariables(select);
        } else if(valuesTable.getPageSize() != pageSize.getNumberValue().intValue()) {
          // page size only has changed
          setRefreshing(true);
          valuesTable.setPageSize(pageSize.getNumberValue().intValue());
        }
        // else nothing to refresh
      }
    });
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setTable(TableDto table) {
    this.table = table;
    valuesTable.setRowCount(table.getValueSetCount());
    valuesTable.setPageStart(0);

    if(dataProvider != null) {
      dataProvider.removeDataDisplay(valuesTable);
      dataProvider = null;
    }

    lastFilter = "";
    filter.setValue(lastFilter, false);
    setRefreshing(false);
  }

  @Override
  public void setVariables(JsArray<VariableDto> variables) {
    initValuesTable();

    listVariable = JsArrays.toList(variables);
    int visible = listVariable.size() < DEFAULT_MAX_VISIBLE_COLUMNS ? listVariable.size() : DEFAULT_MAX_VISIBLE_COLUMNS;
    for(int i = 0; i < visible; i++) {
      valuesTable.addColumn(createColumn(getVariableAt(i)), getColumnLabel(i));
    }

    if(listVariable.size() > DEFAULT_MAX_VISIBLE_COLUMNS + 1) {
      valuesTable.insertColumn(1, createEmptyColumn(), createHeader(new PreviousActionCell()));
      valuesTable.insertColumn(valuesTable.getColumnCount(), createEmptyColumn(), createHeader(new NextActionCell()));
    }

    if(listVariable.size() == 1 && table.getVariableCount() != 1 && filter.getText().isEmpty()) {
      lastFilter = "^" + listVariable.get(0).getName() + "$";
      filter.setValue(lastFilter, false);
    }

    if(dataProvider != null) {
      dataProvider.removeDataDisplay(valuesTable);
      dataProvider = null;
    }
    valuesTable.setPageSize(pageSize.getNumberValue().intValue());
    dataProvider = new ValueSetsDataProvider();
    dataProvider.addDataDisplay(valuesTable);
  }

  @Override
  public ValueSetsProvider getValueSetsProvider() {
    return dataProvider;
  }

  @Override
  public void setValueSetsFetcher(DataFetcher provider) {
    this.fetcher = provider;
  }

  //
  // Private methods
  //

  private void setRefreshing(boolean refresh) {
    refreshPending.setVisible(refresh);
    refreshButton.setEnabled(refresh == false);
  }

  private String getColumnLabel(int i) {
    return listVariable.get(i).getName();
  }

  private VariableDto getVariableAt(int i) {
    return listVariable.get(i);
  }

  private ValueColumn createColumn(final VariableDto variable) {
    ValueColumn col = new ValueColumn(variable) {
      @Override
      protected int getPosition() {
        return listVariable.indexOf(variable);
      }
    };
    col.setValueSelectionHandler(new VariableValueSelectionHandler());
    return col;
  }

  private void initValuesTable() {
    while(valuesTable.getColumnCount() > 0) {
      valuesTable.removeColumn(0);
    }
    firstVisibleIndex = 0;

    TextColumn<ValueSetDto> entityColumn = new TextColumn<ValueSetDto>() {

      @Override
      public String getValue(ValueSetDto value) {
        return value.getIdentifier();
      }
    };
    setMinimumWidth(entityColumn);

    valuesTable.addColumn(entityColumn, table.getEntityType());
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

  //
  // Inner classes
  //

  private final class PreviousActionCell extends IconActionCell<String> {

    private PreviousActionCell() {
      super("icon-previous", new PreviousDelegate());
    }

    @Override
    public boolean isEnabled() {
      return firstVisibleIndex > 0;
    }

  }

  private final class NextActionCell extends IconActionCell<String> {

    private NextActionCell() {
      super("icon-next", new NextDelegate());
    }

    @Override
    public boolean isEnabled() {
      return (firstVisibleIndex + DEFAULT_MAX_VISIBLE_COLUMNS >= listVariable.size() - 1) == false;
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
          navigationPopup.hide();
        }
      };
    }

    protected abstract MenuBar createMenuBar();

    protected abstract void navigate(int steps);

  }

  private final class NextDelegate extends NavigationDelegate {

    @Override
    protected MenuBar createMenuBar() {
      MenuBar menuBar = new MenuBar(true);
      int currentIdx = firstVisibleIndex + DEFAULT_MAX_VISIBLE_COLUMNS;
      for(int i = currentIdx + 1; i < Math.min(currentIdx + MAX_NUMBER_OF_ITEMS + 1, listVariable.size()); i++) {
        final int increment = i - currentIdx;
        menuBar.addItem(new MenuItem(getColumnLabel(i), createCommand(increment)));
      }
      if(Math.min(currentIdx + MAX_NUMBER_OF_ITEMS + 1, listVariable.size()) < listVariable.size()) {
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
        int idx = ++firstVisibleIndex + DEFAULT_MAX_VISIBLE_COLUMNS;
        valuesTable.insertColumn(valuesTable.getColumnCount() - 1, createColumn(getVariableAt(idx)), getColumnLabel(idx));
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
      int currentIdx = firstVisibleIndex + 1;
      for(int i = currentIdx - 1; i >= Math.max(currentIdx - MAX_NUMBER_OF_ITEMS, 0); i--) {
        final int decrement = currentIdx - i;
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
        int idx = firstVisibleIndex--;
        valuesTable.insertColumn(2, createColumn(getVariableAt(idx)), getColumnLabel(idx));
      }
      valuesTable.redrawHeaders();
    }

  }

  private final class ValueSetsDataProvider extends AbstractDataProvider<ValueSetsDto.ValueSetDto> implements ValuesTablePresenter.ValueSetsProvider {

    @Override
    protected void onRangeChanged(HasData<ValueSetDto> display) {
      // Get the new range.
      final Range range = display.getVisibleRange();

      // query the valuesets
      int start = range.getStart();

      if(start > table.getValueSetCount()) return;

      int length = range.getLength();
      if(start + length > table.getValueSetCount()) {
        length = table.getValueSetCount() - start;
      }
      if(filter.getText().isEmpty()) {
        setRefreshing(true);
        fetcher.request(listVariable, start, length);
      } else {
        setRefreshing(true);
        fetcher.request(filter.getText(), start, length);
      }
    }

    @Override
    public void populateValues(int offset, ValueSetsDto valueSets) {
      setRefreshing(false);
      updateRowData(offset, JsArrays.toList(valueSets.getValueSetsArray()));
    }
  }

  private final class VariableValueSelectionHandler implements ValueSelectionHandler {
    @Override
    public void onValueSelection(int row, int column, ValueSetDto valueSet) {
      fetcher.request(listVariable.get(column), valueSet.getIdentifier());
    }
  }

}
