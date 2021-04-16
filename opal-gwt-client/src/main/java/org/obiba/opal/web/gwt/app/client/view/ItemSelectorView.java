/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.presenter.ItemSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ItemSelectorPresenter.EnterKeyHandler;
import org.obiba.opal.web.gwt.app.client.presenter.ItemSelectorPresenter.ItemInputDisplay;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class ItemSelectorView extends ViewImpl implements ItemSelectorPresenter.Display {

  private Widget widget;

  private final Grid itemGrid;

  private ItemInputDisplay itemInputDisplay;

  private List<String> itemList = Lists.newArrayList();

  public ItemSelectorView() {
    itemGrid = new Grid(0, 1);
  }

  @Override
  public void setItems(Iterable<String> items) {
    clear();
    for(String s : items) {
      addItem(s);
    }
  }

  @Override
  public void setItemInputDisplay(ItemInputDisplay itemInputDisplay) {
    this.itemInputDisplay = itemInputDisplay;

    // Add a row for the input widget.
    itemGrid.resize(1, 1);

    FlowPanel itemWidget = new FlowPanel();
    Widget itemInputWidget = itemInputDisplay.asWidget();
    itemInputWidget.addStyleName("inline-block");
    itemWidget.add(itemInputWidget);

    // Put an "add" button in the second column.
    Button addWidget = createAddWidget();
    addWidget.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        addItemAndClear();
      }
    });
    addWidget.addStyleName("small-indent neg-top-margin");
    itemWidget.add(addWidget);

    // Add the input widget in the first column.
    itemGrid.setWidget(itemGrid.getRowCount() - 1, 0, itemWidget);

    // Put the Grid in a FlowPanel (so that it doesn't expand to fill its parent) and set that as the widget.
    FlowPanel container = new FlowPanel();
    container.add(itemGrid);
    widget = container;

    setEnterKeyHandler();
  }

  @Override
  public void addItem(final String item) {
    // Add a row for the new item.
    itemGrid.resize(itemGrid.getRowCount() + 1, 1);

    itemList.add(item);
    FlowPanel itemWidget = new FlowPanel();
    Label label = new Label(itemInputDisplay.renderItem(item));
    label.addStyleName("inline-block");
    itemWidget.add(label);

    // Put a "remove" button in the second column.
    IconAnchor removeWidget = createRemoveWidget();
    removeWidget.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        for(int i = 0; i < itemList.size(); i++) {
          if(itemList.get(i).equals(item)) {
            removeItem(i);
            break;
          }
        }
      }
    });
    removeWidget.addStyleName("small-indent");
    itemWidget.add(removeWidget);
    itemGrid.setWidget(itemGrid.getRowCount() - 1, 0, itemWidget);
  }

  /**
   * Removes the item at the specified row.
   *
   * @param row row index of item to remove (zero-based)
   */
  @Override
  public void removeItem(int row) {
    itemList.remove(row);
    itemGrid.removeRow(row + 1); // +1 to skip the input widget row
  }

  @Override
  public void clear() {
    itemList.clear();
    while(itemGrid.getRowCount() > 1) { // 1 is for the input widget
      removeItem(0);
    }
  }

  @Override
  public int getItemCount() {
    return itemList.size();
  }

  @Override
  public List<String> getItems() {
    return itemList;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  //
  // Methods
  //

  private Button createAddWidget() {
    Button btn = new Button("Add");
    btn.setType(ButtonType.SUCCESS);
    return btn;
  }

  private IconAnchor createRemoveWidget() {
    IconAnchor btn = new IconAnchor();
    btn.setIcon(IconType.REMOVE);
    return btn;
  }

  private void addItemAndClear() {
    String item = itemInputDisplay.getItem();
    if(!item.trim().isEmpty()) {
      addItem(item);
      itemInputDisplay.clear();
    }
  }

  private void setEnterKeyHandler() {
    itemInputDisplay.setEnterKeyHandler(new EnterKeyHandler() {

      @Override
      public void enterKeyPressed() {
        addItemAndClear();
      }
    });
  }
}
