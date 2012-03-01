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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A list of closeable items.
 */
public class CloseableList extends UList {

  private List<ItemRemovedHandler> itemRemovedHandlers = new ArrayList<CloseableList.ItemRemovedHandler>();

  private ItemValidator itemValidator;

  public CloseableList() {
    super();
    addStyleName("closeables");
  }

  public boolean addItem(final String text) {
    return addItem(text, true);
  }

  public boolean addItem(final String text, boolean validate) {
    if(Strings.isNullOrEmpty(text)) return false;

    if(validate && itemValidator != null && !itemValidator.validate(text)) return false;

    addItemInternal(text);

    return true;
  }

  private void addItemInternal(final String text) {
    final ListItem item = new ListItem();

    item.add(new InlineLabel(text));
    Anchor close = new Anchor("x");
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
      handler.onItemRemoved(getItemText(item));
    }
  }

  private String getItemText(ListItem item) {
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
    ImmutableList.Builder<String> builder = ImmutableList.<String> builder();
    for(int i = 0; i < getWidgetCount(); i++) {
      builder.add(getItemText((ListItem) getWidget(i)));
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
      if(lastItem.getStyleName().indexOf("focus") != -1) {
        removeItem((ListItem) lastItem);
      } else {
        lastItem.addStyleName("focus");
      }
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
    this.itemValidator = validator;
  }

  public interface ItemRemovedHandler {
    public void onItemRemoved(String text);
  }

  public interface ItemValidator {
    public boolean validate(String text);
  }

  public interface ItemTransformer {
    public boolean addItem(String text);
  }

}
