/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.ItemSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ItemSelectorPresenter.ItemInputDisplay;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class ItemSelectorView extends Composite implements ItemSelectorPresenter.Display {
  //
  // Instance Variables
  //

  private Grid itemGrid;

  private ItemInputDisplay itemInputDisplay;

  //
  // Constructors
  //

  public ItemSelectorView() {
    itemGrid = new Grid(0, 2);
    itemGrid.addStyleName("itemSelector");
  }

  //
  // ItemSelectorPresenter.Display Methods
  //

  @Override
  public void setItemInputDisplay(ItemInputDisplay itemInputDisplay) {
    this.itemInputDisplay = itemInputDisplay;

    // Add a row for the input widget.
    itemGrid.resize(1, 2);

    // Add the input widget in the first column.
    itemGrid.setWidget(itemGrid.getRowCount() - 1, 0, itemInputDisplay.asWidget());

    // Put an "add" button in the second column.
    Image addWidget = createAddWidget();
    addWidget.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        String item = ItemSelectorView.this.itemInputDisplay.getItem();
        if(item.trim().length() != 0) {
          addItem(ItemSelectorView.this.itemInputDisplay.getItem());
        }
      }
    });
    itemGrid.setWidget(itemGrid.getRowCount() - 1, 1, addWidget);

    FlowPanel container = new FlowPanel();
    container.add(itemGrid);
    initWidget(container);
  }

  @Override
  public void addItem(final String item) {
    // Add a row for the new item.
    itemGrid.resize(itemGrid.getRowCount() + 1, 2);

    // Put the item in the new row's first column.
    itemGrid.setText(itemGrid.getRowCount() - 1, 0, item);

    // Put a "remove" button in the second column.
    Image removeWidget = createRemoveWidget();
    removeWidget.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        for(int i = 0; i < itemGrid.getRowCount(); i++) {
          if(itemGrid.getText(i, 0).equals(item)) {
            removeItem(i);
            break;
          }
        }
      }
    });
    itemGrid.setWidget(itemGrid.getRowCount() - 1, 1, removeWidget);
  }

  @Override
  public void removeItem(int row) {
    itemGrid.removeRow(row);
  }

  @Override
  public List<String> getItems() {
    List<String> items = new ArrayList<String>();
    for(int row = 0; row < itemGrid.getRowCount(); row++) {
      items.add(itemGrid.getText(row, 0));
    }

    return items;
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

  //
  // Methods
  //

  private Image createAddWidget() {
    Image addWidget = new Image("image/20/list-add.png");
    addWidget.addStyleName("clickable");
    addWidget.addStyleName("button");

    return addWidget;
  }

  private Image createRemoveWidget() {
    Image removeWidget = new Image("image/20/list-remove.png");
    removeWidget.addStyleName("clickable");
    removeWidget.addStyleName("button");

    return removeWidget;
  }
}
