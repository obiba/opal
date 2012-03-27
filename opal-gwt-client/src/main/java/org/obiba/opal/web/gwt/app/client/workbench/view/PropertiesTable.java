/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A table for displaying key value pairs.
 */
public class PropertiesTable extends FlowPanel {

  private static Translations translations = GWT.create(Translations.class);

  private DefaultFlexTable innerTable;

  private String keyStyleNames;

  private String valueStyleNames;

  public PropertiesTable() {
    super();
    innerTable = new DefaultFlexTable();
    super.add(innerTable);
  }

  @Override
  public void add(Widget child) {
    if(child instanceof IndexedPanel) {
      IndexedPanel children = (IndexedPanel) child;
      int column = 0;
      int span = 1;
      if(child instanceof PropertyPanel) {
        column = ((PropertyPanel) child).getColumn();
        span = ((PropertyPanel) child).getSpan();
      }
      if(children.getWidgetCount() != 2) {
        throw new IllegalArgumentException("PropertiesGrid expects a pair of key/value widgets at row " + innerTable.getRowCount());
      }
      addProperty(children.getWidget(0), children.getWidget(1), column, span);
    } else {
      throw new IllegalArgumentException("PropertiesGrid expects child widgets being IndexedPanels");
    }
  }

  public void addProperty(String key, String value) {
    addProperty(new Label(key), new Label(value));
  }

  public void addProperty(String key, String value, int column) {
    addProperty(new Label(key), new Label(value), column);
  }

  public void addProperty(String key, String value, int column, int span) {
    addProperty(new Label(key), new Label(value), column, span);
  }

  public void addProperty(Widget key, Widget value) {
    addProperty(key, value, 0, 1);
  }

  public void addProperty(Widget key, Widget value, int column) {
    addProperty(key, value, column, 1);
  }

  public void addProperty(Widget key, Widget value, int column, int span) {
    int numRows = innerTable.getRowCount();
    int col = 2 * column;
    int row = col == 0 ? numRows : numRows - 1;
    if(key != null) {
      key.removeFromParent();
      innerTable.setWidget(row, col, key);
      if(!Strings.isNullOrEmpty(keyStyleNames)) {
        innerTable.getFlexCellFormatter().setStyleName(row, col, keyStyleNames);
      }
      key.addStyleName("property-key");
    }

    if(value != null) {
      value.removeFromParent();
      innerTable.setWidget(row, col + 1, value);
      if(!Strings.isNullOrEmpty(valueStyleNames)) {
        innerTable.getFlexCellFormatter().setStyleName(row, col + 1, valueStyleNames);
      }
      if(span > 1) {
        innerTable.getFlexCellFormatter().setColSpan(row, col + 1, span + 1);
      }
      value.addStyleName("property-value");
    }
  }

  public void setStyled(boolean styled) {
    innerTable.setStyled(styled);
  }

  public void setBordered(boolean bordered) {
    innerTable.setBordered(bordered);
  }

  public void setBorderedCell(boolean bordered) {
    innerTable.setBorderedCell(bordered);
  }

  public void setCondensed(boolean condensed) {
    innerTable.setCondensed(condensed);
  }

  public void setZebra(boolean zebra) {
    innerTable.setZebra(zebra);
  }

  public void setPropertyHeader(String text) {
    innerTable.setHeader(0, text);
  }

  public void setPropertyHeaderWidget(Widget widget) {
    innerTable.setHeaderWidget(0, widget);
  }

  public void setHeaderVisible(boolean visible) {
    if(visible) {
      setPropertyHeader(translations.property());
      setValueHeader(translations.value());
    } else {
      // TODO
    }
  }

  public void setValueHeader(String text) {
    innerTable.setHeader(1, text);
  }

  public void setValueHeaderWidget(Widget widget) {
    innerTable.setHeaderWidget(1, widget);
  }

  public void setKeyStyleNames(String keyStyleNames) {
    this.keyStyleNames = keyStyleNames;
    for(int i = 0; i < innerTable.getRowCount(); i++) {
      for(int j = 0; j < innerTable.getCellCount(i); j++) {
        if(j % 2 == 0) {
          innerTable.getFlexCellFormatter().setStyleName(i, j, keyStyleNames);
        }
      }
    }
  }

  public void setValueStyleNames(String valueStyleNames) {
    this.valueStyleNames = valueStyleNames;
    for(int i = 0; i < innerTable.getRowCount(); i++) {
      for(int j = 0; j < innerTable.getCellCount(i); j++) {
        if(j % 2 != 0) {
          innerTable.getFlexCellFormatter().setStyleName(i, j, valueStyleNames);
        }
      }
    }
  }

}
