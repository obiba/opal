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
    setPropertyHeader(translations.property());
    setValueHeader(translations.value());
    super.add(innerTable);
  }

  @Override
  public void add(Widget child) {
    if(child instanceof IndexedPanel) {
      IndexedPanel children = (IndexedPanel) child;
      if(children.getWidgetCount() != 2) {
        throw new IllegalArgumentException("PropertiesGrid expects a pair of key/value widgets at row " + innerTable.getRowCount());
      }
      addProperty(children.getWidget(0), children.getWidget(1));
    } else {
      throw new IllegalArgumentException("PropertiesGrid expects child widgets being IndexedPanels");
    }
  }

  public void addProperty(Widget key, Widget value) {
    int numRows = innerTable.getRowCount();

    if(key != null) {
      key.removeFromParent();
      innerTable.setWidget(numRows, 0, key);
      if(!Strings.isNullOrEmpty(keyStyleNames)) {
        innerTable.getFlexCellFormatter().setStyleName(numRows, 0, keyStyleNames);
      }
      key.addStyleName("property-key");
    }

    if(value != null) {
      value.removeFromParent();
      innerTable.setWidget(numRows, 1, value);
      if(!Strings.isNullOrEmpty(valueStyleNames)) {
        innerTable.getFlexCellFormatter().setStyleName(numRows, 1, valueStyleNames);
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

  public void setValueHeader(String text) {
    innerTable.setHeader(1, text);
  }

  public void setValueHeaderWidget(Widget widget) {
    innerTable.setHeaderWidget(1, widget);
  }

  public void setKeyStyleNames(String keyStyleNames) {
    this.keyStyleNames = keyStyleNames;
    for(int i = 0; i < innerTable.getRowCount(); i++) {
      innerTable.getFlexCellFormatter().setStyleName(i, 0, keyStyleNames);
    }
  }

  public void setValueStyleNames(String valueStyleNames) {
    this.valueStyleNames = valueStyleNames;
    for(int i = 0; i < innerTable.getRowCount(); i++) {
      innerTable.getFlexCellFormatter().setStyleName(i, 0, valueStyleNames);
    }
  }

}
