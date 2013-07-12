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

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A list of closeable items.
 */
public class CloseableList extends UList {

  private final List<ItemRemovedHandler> itemRemovedHandlers = new ArrayList<ItemRemovedHandler>();

  private ItemValidator itemValidator;

  public CloseableList() {
    addStyleName("closeables");
  }

  public boolean addItem(String text, VariableSearchListItem.ItemType type) {
    return addItem(text, true, type);
  }

  public boolean addItem(String text) {
    return addItem(text, true);
  }

  public boolean addItem(String text, boolean validate, VariableSearchListItem.ItemType type) {
    if(Strings.isNullOrEmpty(text)) return false;

    if(validate && itemValidator != null && !itemValidator.validate(text)) return false;

    addItemInternal(text, type);

    return true;
  }

  public boolean addItem(String text, boolean validate) {
    if(Strings.isNullOrEmpty(text)) return false;

    if(validate && itemValidator != null && !itemValidator.validate(text)) return false;

    addItemInternal(text);

    return true;
  }

  private void addItemInternal(String text, VariableSearchListItem.ItemType type) {
    final VariableSearchListItem item = new VariableSearchListItem(type);
    item.setItemTitle(text);

    InlineLabel label = new InlineLabel(text);
    label.addStyleName("label");
    item.add(label);
    IconAnchor close = new IconAnchor();
    close.setIcon(IconType.REMOVE);

    close.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        removeItem(item);
      }
    });
    item.add(close);

    clearLastItemFocus();

    add(item);
  }

  private void addItemInternal(String text) {
    final ListItem item = new ListItem();

    InlineLabel label = new InlineLabel(text);
    label.addStyleName("label");
    item.add(label);
    IconAnchor close = new IconAnchor();
    close.setIcon(IconType.REMOVE);

    close.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        removeItem(item);
      }
    });
    item.add(close);

    clearLastItemFocus();

    add(item);
  }

  public void removeItem(String text) {
    ListItem it = getItem(text);

    if(it != null) {
      removeItem(it);
    }
  }

  private void removeItem(ListItem item) {
    remove(item);
    clearLastItemFocus();
    for(ItemRemovedHandler handler : itemRemovedHandlers) {
      handler.onItemRemoved(item);
    }
  }

  @Override
  public void clear() {
    // inform listeners about items removal
    for(int i = 0; i < getWidgetCount(); i++) {
      ListItem item = (ListItem) getWidget(i);
      for(ItemRemovedHandler handler : itemRemovedHandlers) {
        handler.onItemRemoved(item);
      }
    }
    super.clear();
  }

  private String getItemText(IndexedPanel item) {
    Widget label = item.getWidget(0);
    return ((HasText) label).getText();
  }

  private ListItem getItem(String text) {
    ListItem it = null;
    for(int i = 0; i < getWidgetCount(); i++) {
      ListItem item = (ListItem) getWidget(i);
      if(getItemText(item).equals(text)) {
        it = item;
        break;
      }
    }
    return it;
  }

  public List<String> getItemTexts() {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    for(int i = 0; i < getWidgetCount(); i++) {
      builder.add(getItemText((IndexedPanel) getWidget(i)));
    }
    return builder.build();
  }

  public void clearLastItemFocus() {
    if(getWidgetCount() > 0) {
      Widget lastItem = getWidget(getWidgetCount() - 1);
      lastItem.removeStyleName("focus");
    }
  }

  public void focusOrRemoveLastItem() {
    if(getWidgetCount() > 0) {
      Widget lastItem = getWidget(getWidgetCount() - 1);
      if(lastItem.getStyleName().contains("focus")) {
        removeItem((ListItem) lastItem);
      } else {
        lastItem.addStyleName("focus");
      }
    }
  }

  public void removeLastItemFocus() {
    if(getWidgetCount() > 0) {
      Widget lastItem = getWidget(getWidgetCount() - 1);
      lastItem.removeStyleName("focus");
    }
  }

  public void addItemRemovedHandler(ItemRemovedHandler handler) {
    if(itemRemovedHandlers.contains(handler)) return;
    itemRemovedHandlers.add(handler);
  }

  public void removeItemRemovedHandler(ItemRemovedHandler handler) {
    itemRemovedHandlers.remove(handler);
  }

  public void setItemValidator(ItemValidator validator) {
    itemValidator = validator;
  }

  public interface ItemRemovedHandler {
    void onItemRemoved(ListItem item);
  }

  public interface ItemValidator {
    boolean validate(String text);
  }
}
