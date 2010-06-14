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
import org.obiba.opal.web.model.client.AttributeDto;
import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
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
  Label tableName;

  @UiField
  Label count;

  @UiField
  Label entityType;

  @UiField
  FlowPanel spreadsheetDownloadPanel;

  @UiField
  CellTable<VariableDto> table;

  SelectionModel<VariableDto> selectionModel = new SingleSelectionModel<VariableDto>();

  SimplePager<VariableDto> pager;

  private Image spreadsheetDownloadImage;

  private Translations translations = GWT.create(Translations.class);

  public TableView() {
    initWidget(uiBinder.createAndBindUi(this));
    addSpreadsheetDownloadImage();

    table.addColumn(new TextColumn<VariableDto>() {
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
        return "";
      }
    }, translations.unitLabel());

    table.setSelectionEnabled(true);
    table.setSelectionModel(selectionModel);
    table.setPageSize(50);
    pager = new SimplePager<VariableDto>(table);
    table.setPager(pager);
    LayoutPanel p = ((LayoutPanel) table.getParent());
    p.add(pager);
    p.setWidgetTopHeight(pager, 6, Unit.EM, 2, Unit.EM);
    p.setWidgetRightWidth(pager, 1, Unit.EM, 50, Unit.PCT);
    DOM.removeElementAttribute(pager.getElement(), "style");
    DOM.setStyleAttribute(pager.getElement(), "cssFloat", "right");
  }

  private void addSpreadsheetDownloadImage() {
    spreadsheetDownloadImage = new Image("image/spreadsheet-download-icon.png");
    spreadsheetDownloadPanel.add(spreadsheetDownloadImage);
    DOM.setStyleAttribute(spreadsheetDownloadImage.getElement(), "cssFloat", "right");
  }

  @Override
  public void renderRows(final JsArray<VariableDto> rows) {
    table.setDelegate(new Delegate<VariableDto>() {

      @Override
      public void onRangeChanged(ListView<VariableDto> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, JsArrays.toList(rows, start, length));
      }

    });
    pager.firstPage();
    table.setData(0, table.getPageSize(), JsArrays.toList(rows, 0, table.getPageSize()));
    table.setDataSize(rows.length(), true);
  }

  @Override
  public SelectionModel<VariableDto> getTableSelection() {
    return selectionModel;
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
  public Label getTableName() {
    return tableName;
  }

  @Override
  public Label getVariableCountLabel() {
    return count;
  }

  @Override
  public Label getEntityTypeLabel() {
    return entityType;
  }

  @Override
  public HasClickHandlers getSpreadsheetIcon() {
    return spreadsheetDownloadImage;
  }

}
