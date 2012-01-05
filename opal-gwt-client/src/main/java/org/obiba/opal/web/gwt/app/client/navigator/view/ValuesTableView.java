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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.ValuesTablePresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.ValueSetDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class ValuesTableView extends Composite implements ValuesTablePresenter.Display {

  private static final int MAX_VISIBLE_COLUMNS = 10;

  @UiTemplate("ValuesTableView.ui.xml")
  interface ValuesTableViewUiBinder extends UiBinder<Widget, ValuesTableView> {
  }

  private static ValuesTableViewUiBinder uiBinder = GWT.create(ValuesTableViewUiBinder.class);

  private Translations translations = GWT.create(Translations.class);

  @UiField
  SimplePager pager;

  @UiField
  InlineLabel noValues;

  @UiField
  FlowPanel valuesPanel;

  Table<ValueSetDto> valuesTable;

  private ListDataProvider<ValueSetDto> dataProvider = new ListDataProvider<ValueSetDto>();

  private List<VariableDto> listVariable;

  private int firstVisibleIndex = 0;

  public ValuesTableView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void setVariables(JsArray<VariableDto> variables) {
    initBefore();

    listVariable = JsArrays.toList(variables);
    int visible = listVariable.size() < MAX_VISIBLE_COLUMNS ? listVariable.size() : MAX_VISIBLE_COLUMNS;
    for(int i = 0; i < visible; i++) {
      valuesTable.addColumn(createColumn(), getColumnLabel(i));
    }

    initAfter();
  }

  private String getColumnLabel(int i) {
    return listVariable.get(i).getName();
  }

  private TextColumn<ValueSetDto> createColumn() {
    return new TextColumn<ValueSetDto>() {

      @Override
      public String getValue(ValueSetDto value) {
        return null;
      }
    };
  }

  private void initBefore() {
    if(valuesTable != null) valuesPanel.remove(valuesTable);
    valuesTable = new Table<ValueSetDto>();
    valuesPanel.add(valuesTable);
    valuesTable.setEmptyTableWidget(noValues);
    valuesTable.addStyleName("left-aligned");
    valuesTable.setWidth("100%");
    dataProvider.addDataDisplay(valuesTable);
    pager.setDisplay(valuesTable);

    valuesTable.addColumn(new TextColumn<ValueSetDto>() {

      @Override
      public String getValue(ValueSetDto value) {
        return value.getEntity().getIdentifier();
      }
    }, translations.participant());

    EmptyColumn<ValueSetDto> col = new EmptyColumn<ValueSetDto>();
    valuesTable.addColumn(col, createPreviousHeader());
  }

  private void initAfter() {
    valuesTable.addColumn(new EmptyColumn<ValueSetDto>(), createNextHeader());
  }

  private Header<String> createNextHeader() {

    SafeHtml safe = SafeHtmlUtils.fromSafeConstant("<img src=\"image/20/next.png\">");
    ActionCell<String> nextActionCell = new ActionCell<String>(safe, new Delegate<String>() {

      @Override
      public void execute(String object) {
        if(firstVisibleIndex + MAX_VISIBLE_COLUMNS > listVariable.size() - 1) return;
        valuesTable.removeColumn(2);
        valuesTable.insertColumn(valuesTable.getColumnCount() - 1, createColumn(), getColumnLabel(++firstVisibleIndex + MAX_VISIBLE_COLUMNS));
      }
    });

    Header<String> nextHeader = new Header<String>(nextActionCell) {

      @Override
      public String getValue() {
        return null;
      }
    };
    return nextHeader;
  }

  private Header<String> createPreviousHeader() {
    SafeHtml safe = SafeHtmlUtils.fromSafeConstant("<img src=\"image/20/previous.png\">");

    ActionCell<String> previousActionCell = new ActionCell<String>(safe, new Delegate<String>() {

      @Override
      public void execute(String object) {
        if(firstVisibleIndex == 0) return;
        valuesTable.removeColumn(valuesTable.getColumnCount() - 2);
        valuesTable.insertColumn(2, createColumn(), getColumnLabel(--firstVisibleIndex));
      }
    });

    Header<String> previousHeader = new Header<String>(previousActionCell) {

      @Override
      public String getValue() {
        return null;
      }
    };
    return previousHeader;
  }

  public static class EmptyColumn<T> extends TextColumn<T> {

    @Override
    public String getValue(T object) {
      return null;
    }
  }
}
