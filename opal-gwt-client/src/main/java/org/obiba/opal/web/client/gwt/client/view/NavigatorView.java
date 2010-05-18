/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.client.gwt.client.view;

import java.util.List;

import org.obiba.opal.web.client.gwt.client.presenter.NavigatorPresenter.Display;
import org.obiba.opal.web.model.client.AttributeDto;
import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.gen2.table.client.AbstractColumnDefinition;
import com.google.gwt.gen2.table.client.DefaultTableDefinition;
import com.google.gwt.gen2.table.client.SelectionGridBulkRenderer;
import com.google.gwt.gen2.table.client.SortableGrid;
import com.google.gwt.gen2.table.event.client.HasRowSelectionHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class NavigatorView extends Composite implements Display {

  @UiTemplate("NavigatorView.ui.xml")
  interface NavigatorViewUiBinder extends UiBinder<DockLayoutPanel, NavigatorView> {
  }

  private static NavigatorViewUiBinder uiBinder = GWT.create(NavigatorViewUiBinder.class);

  @UiField
  Tree tree;

  @UiField
  SortableGrid table;

  @UiField
  Panel east;

  SelectionGridBulkRenderer<VariableDto> bulkRenderer;

  public NavigatorView() {
    initWidget(uiBinder.createAndBindUi(this));
    tree.setAnimationEnabled(true);

    DefaultTableDefinition<VariableDto> tableDefinition = new DefaultTableDefinition<VariableDto>();
    AbstractColumnDefinition<VariableDto, String> cd = new AbstractColumnDefinition<VariableDto, String>() {

      @Override
      public String getCellValue(VariableDto rowValue) {
        return rowValue.getName();
      }

      @Override
      public void setCellValue(VariableDto rowValue, String cellValue) {

      }

    };
    cd.setColumnSortable(true);
    cd.setHeader(0, "Name");
    cd.setHeaderCount(1);
    tableDefinition.addColumnDefinition(cd);

    tableDefinition.addColumnDefinition(new AbstractColumnDefinition<VariableDto, String>() {

      @Override
      public String getCellValue(VariableDto rowValue) {
        return rowValue.getValueType();
      }

      @Override
      public void setCellValue(VariableDto rowValue, String cellValue) {

      }

    });

    tableDefinition.addColumnDefinition(new AbstractColumnDefinition<VariableDto, String>() {

      @Override
      public String getCellValue(VariableDto rowValue) {
        JsArray<AttributeDto> attributes = rowValue.getAttributesArray();
        for(int i = 0; i < attributes.length(); i++) {
          AttributeDto attribute = attributes.get(i);
          if(attribute.getName().equals("label")) {
            return attribute.getValue();
          }
        }
        return null;
      }

      @Override
      public void setCellValue(VariableDto rowValue, String cellValue) {

      }

    });

    table.setSelectionEnabled(true);
    bulkRenderer = new SelectionGridBulkRenderer<VariableDto>(table, tableDefinition);
  }

  public Panel getDetailsPanel() {
    return east;
  }

  @Override
  public void setItems(List<TreeItem> items) {
    tree.clear();
    for(TreeItem item : items) {
      tree.addItem(item);
    }
  }

  @Override
  public HasSelectionHandlers<TreeItem> getTree() {
    return tree;
  }

  @Override
  public void renderRows(Iterable<VariableDto> rows) {
    bulkRenderer.renderRows(rows);
  }

  @Override
  public HasRowSelectionHandlers getTable() {
    return table;
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

}
