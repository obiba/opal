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
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.gwtplatform.mvp.client.ViewImpl;

public class ValuesTableView extends ViewImpl implements ValuesTablePresenter.Display {

  private static final String IMG_NEXT = "<img src=\"image/20/next.png\">";

  private static final String IMG_PREVIOUS = "<img src=\"image/20/previous.png\">";

  private static final String IMAGE_PREVIOUS_DISABLED = "<img src=\"image/20/previous-disabled.png\">";

  private static final String IMAGE_NEXT_DISABLED = "<img src=\"image/20/next-disabled.png\">";

  private static final int MAX_VISIBLE_COLUMNS = 10;

  @UiTemplate("ValuesTableView.ui.xml")
  interface ValuesTableViewUiBinder extends UiBinder<Widget, ValuesTableView> {
  }

  private static ValuesTableViewUiBinder uiBinder = GWT.create(ValuesTableViewUiBinder.class);

  private Translations translations = GWT.create(Translations.class);

  private final Widget widget;

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
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
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

    valuesTable.addColumn(createEmptyColumn(), createPreviousDisabledHeader());
  }

  private void initAfter() {
    valuesTable.addColumn(createEmptyColumn(), listVariable.size() > MAX_VISIBLE_COLUMNS ? createNextEnabledHeader() : createNextDisabledHeader());
  }

  private void enablePrevious(boolean enable) {
    valuesTable.removeColumn(1);
    valuesTable.insertColumn(1, createEmptyColumn(), enable ? createPreviousEnabledHeader() : createPreviousDisabledHeader());
  }

  private void enableNext(boolean enable) {
    valuesTable.removeColumn(valuesTable.getColumnCount() - 1);
    valuesTable.insertColumn(valuesTable.getColumnCount(), createEmptyColumn(), enable ? createNextEnabledHeader() : createNextDisabledHeader());
  }

  private SafeHtmlHeader createNextDisabledHeader() {
    return new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant(IMAGE_NEXT_DISABLED));
  }

  private SafeHtmlHeader createPreviousDisabledHeader() {
    return new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant(IMAGE_PREVIOUS_DISABLED));
  }

  private Header<String> createPreviousEnabledHeader() {
    SafeHtml safe = SafeHtmlUtils.fromSafeConstant(IMG_PREVIOUS);
    ActionCell<String> previousActionCell = new ActionCell<String>(safe, new Delegate<String>() {

      @Override
      public void execute(String object) {
        valuesTable.removeColumn(valuesTable.getColumnCount() - 2);
        valuesTable.insertColumn(2, createColumn(), getColumnLabel(firstVisibleIndex--));
        enableNext(true);
        if(firstVisibleIndex == 0) {
          enablePrevious(false);
        }
      }
    });
    return createHeader(previousActionCell);
  }

  private Header<String> createNextEnabledHeader() {
    SafeHtml safe = SafeHtmlUtils.fromSafeConstant(IMG_NEXT);
    ActionCell<String> nextActionCell = new ActionCell<String>(safe, new Delegate<String>() {

      @Override
      public void execute(String object) {
        valuesTable.removeColumn(2);
        valuesTable.insertColumn(valuesTable.getColumnCount() - 1, createColumn(), getColumnLabel(++firstVisibleIndex + MAX_VISIBLE_COLUMNS));
        enablePrevious(true);
        if(firstVisibleIndex + MAX_VISIBLE_COLUMNS >= listVariable.size() - 1) {
          enableNext(false);
        }
      }
    });
    return createHeader(nextActionCell);
  }

  private Header<String> createHeader(ActionCell<String> cell) {
    return new Header<String>(cell) {

      @Override
      public String getValue() {
        return null;
      }
    };
  }

  private TextColumn<ValueSetDto> createEmptyColumn() {
    return new TextColumn<ValueSetDto>() {

      @Override
      public String getValue(ValueSetDto object) {
        return null;
      }
    };
  }
}
