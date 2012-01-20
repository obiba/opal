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
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ValueColumn;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueSetDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.gwtplatform.mvp.client.ViewImpl;

public class ValuesTableView extends ViewImpl implements ValuesTablePresenter.Display {

  private static final int MAX_VISIBLE_COLUMNS = 5;

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

  private String entityType;

  private int firstVisibleIndex = 0;

  public ValuesTableView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setEntityType(String type) {
    this.entityType = type;
  }

  @Override
  public void setVariables(JsArray<VariableDto> variables) {
    initBefore();

    listVariable = JsArrays.toList(variables);
    int visible = listVariable.size() < MAX_VISIBLE_COLUMNS ? listVariable.size() : MAX_VISIBLE_COLUMNS;
    for(int i = 0; i < visible; i++) {
      valuesTable.addColumn(createColumn(i, listVariable.get(i).getValueType()), getColumnLabel(i));
    }

    initAfter();
  }

  private String getColumnLabel(int i) {
    return listVariable.get(i).getName();
  }

  private String getColumnValueType(int i) {
    return listVariable.get(i).getValueType();
  }

  private ValueColumn createColumn(int pos, String type) {
    return new ValueColumn(pos, type);
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

    TextColumn<ValueSetDto> participantColumn = new TextColumn<ValueSetDto>() {

      @Override
      public String getValue(ValueSetDto value) {
        return value.getIdentifier();
      }
    };
    setMinimumWidth(participantColumn);

    valuesTable.addColumn(participantColumn, entityType);
    valuesTable.addColumn(createEmptyColumn(), createHeader(new PreviousActionCell()));
  }

  private void initAfter() {
    valuesTable.addColumn(createEmptyColumn(), createHeader(new NextActionCell()));
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

  private final class PreviousActionCell extends ActionCell<String> {

    private static final String IMG_PREVIOUS = "<a class=\"icon icon-previous\"/>";

    private static final String IMAGE_PREVIOUS_DISABLED = "<span class=\"icon icon-previous disabled\"/>";

    private PreviousActionCell() {
      super("", new Delegate<String>() {

        @Override
        public void execute(String object) {
          if(firstVisibleIndex == 0) return;

          valuesTable.removeColumn(valuesTable.getColumnCount() - 2);
          int idx = firstVisibleIndex--;
          valuesTable.insertColumn(2, createColumn(idx, getColumnValueType(idx)), getColumnLabel(idx));
          valuesTable.redrawHeaders();
        }
      });
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {
      if(firstVisibleIndex == 0) {
        sb.append(SafeHtmlUtils.fromSafeConstant(IMAGE_PREVIOUS_DISABLED));
      } else {
        sb.append(SafeHtmlUtils.fromSafeConstant(IMG_PREVIOUS));
      }
    }

  }

  private final class NextActionCell extends ActionCell<String> {

    private static final String IMG_NEXT = "<a class=\"icon icon-next\"/>";

    private static final String IMAGE_NEXT_DISABLED = "<span class=\"icon icon-next disabled\"/>";

    private NextActionCell() {
      super("", new Delegate<String>() {

        @Override
        public void execute(String object) {
          if(firstVisibleIndex + MAX_VISIBLE_COLUMNS >= listVariable.size() - 1) return;

          valuesTable.removeColumn(2);
          int idx = ++firstVisibleIndex + MAX_VISIBLE_COLUMNS;
          valuesTable.insertColumn(valuesTable.getColumnCount() - 1, createColumn(idx, getColumnValueType(idx)), getColumnLabel(idx));
          valuesTable.redrawHeaders();
        }

      });
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {
      if(firstVisibleIndex + MAX_VISIBLE_COLUMNS >= listVariable.size() - 1) {
        sb.append(SafeHtmlUtils.fromSafeConstant(IMAGE_NEXT_DISABLED));
      } else {
        sb.append(SafeHtmlUtils.fromSafeConstant(IMG_NEXT));
      }
    }
  }

}
