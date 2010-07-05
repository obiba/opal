/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.ui.HasFieldUpdater;
import org.obiba.opal.web.model.client.AttributeDto;
import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListView;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.ListView.Delegate;

public class TableView extends Composite implements TablePresenter.Display {

  @UiTemplate("TableView.ui.xml")
  interface TableViewUiBinder extends UiBinder<Widget, TableView> {
  }

  private static TableViewUiBinder uiBinder = GWT.create(TableViewUiBinder.class);

  @UiField
  Hyperlink parentLink;

  @UiField
  Label tableName;

  @UiField
  Label variablesTableTitle;

  @UiField
  Label entityType;

  @UiField
  FlowPanel toolbarPanel;

  @UiField
  CellTable<VariableDto> table;

  SelectionModel<VariableDto> selectionModel = new SingleSelectionModel<VariableDto>();

  SimplePager<VariableDto> pager;

  private Image spreadsheetDownloadImage;

  private Image previousImage;

  private Image nextImage;

  private VariableNameColumn variableNameColumn;

  private Translations translations = GWT.create(Translations.class);

  public TableView() {
    initWidget(uiBinder.createAndBindUi(this));
    addTableColumns();
    initToolbar();
  }

  private void addTableColumns() {
    table.addColumn(variableNameColumn = new VariableNameColumn() {
      @Override
      public String getValue(VariableDto object) {
        return object.getName();
      }
    }, translations.nameLabel());

    table.addColumn(new TextColumn<VariableDto>() {
      @Override
      public String getValue(VariableDto object) {
        JsArray<AttributeDto> attributes = object.getAttributesArray();
        for(int i = 0; i < attributes.length(); i++) {
          AttributeDto attribute = attributes.get(i);
          if(attribute.getName().equals("label")) {
            return attribute.getValue();
          }
        }
        return null;
      }
    }, translations.labelLabel());

    table.addColumn(new TextColumn<VariableDto>() {
      @Override
      public String getValue(VariableDto object) {
        return object.getValueType();
      }
    }, translations.valueTypeLabel());

    table.addColumn(new TextColumn<VariableDto>() {

      @Override
      public String getValue(VariableDto object) {
        return object.getUnit();
      }
    }, translations.unitLabel());

    table.setSelectionEnabled(true);
    table.setSelectionModel(selectionModel);
    table.setPageSize(50);
    pager = new SimplePager<VariableDto>(table);
    table.setPager(pager);

    ((InsertPanel) table.getParent()).insert(pager, 0);
    DOM.removeElementAttribute(pager.getElement(), "style");
    DOM.setStyleAttribute(pager.getElement(), "cssFloat", "right");
  }

  private void initToolbar() {
    nextImage = new Image("image/next.png");
    toolbarPanel.add(nextImage);
    previousImage = new Image("image/previous.png");
    toolbarPanel.add(previousImage);
    spreadsheetDownloadImage = new Image("image/document-export.png");
    toolbarPanel.add(spreadsheetDownloadImage);
  }

  @Override
  public void renderRows(final JsArray<VariableDto> rows) {
    final JsArray<VariableDto> variableRows = (rows != null) ? rows : (JsArray<VariableDto>) JsArray.createArray();

    variablesTableTitle.setText(translations.variablesLabel() + " (" + variableRows.length() + ")");

    table.setDelegate(new Delegate<VariableDto>() {

      @Override
      public void onRangeChanged(ListView<VariableDto> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, JsArrays.toList(variableRows, start, length));
      }

    });
    pager.firstPage();
    table.setData(0, table.getPageSize(), JsArrays.toList(variableRows, 0, table.getPageSize()));
    table.setDataSize(variableRows.length(), true);
    table.redraw();
  }

  @Override
  public void setVariableSelection(VariableDto variable, int index) {
    int pageIndex = (int) (index / table.getPageSize());
    if(pageIndex != pager.getPage()) {
      pager.setPage(pageIndex);
    }
    selectionModel.setSelected(variable, true);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void clear() {
    renderRows((JsArray<VariableDto>) JavaScriptObject.createArray());
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public HasText getTableName() {
    return tableName;
  }

  @Override
  public HasText getEntityTypeLabel() {
    return entityType;
  }

  @Override
  public HasClickHandlers getSpreadsheetIcon() {
    return spreadsheetDownloadImage;
  }

  @Override
  public HasText getParentName() {
    return parentLink;
  }

  @Override
  public HasClickHandlers getParentLink() {
    return parentLink;
  }

  @Override
  public HasClickHandlers getNextLink() {
    return nextImage;
  }

  @Override
  public HasClickHandlers getPreviousLink() {
    return previousImage;
  }

  private abstract class VariableNameColumn extends Column<VariableDto, String> implements HasFieldUpdater<VariableDto, String> {
    public VariableNameColumn() {
      super(new ClickableTextCell());
    }
  }

  @Override
  public HasFieldUpdater<VariableDto, String> getVariableNameColumn() {
    return variableNameColumn;
  }
}
