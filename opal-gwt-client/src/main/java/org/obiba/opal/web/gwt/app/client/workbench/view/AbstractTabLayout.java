/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.HasBeforeSelectionHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class AbstractTabLayout extends FlowPanel implements IndexedPanel, HasSelectionHandlers<Integer>, HasBeforeSelectionHandlers<Integer> {

  protected UList menu;

  private SimplePanel content;

  private List<ListItem> items;

  private List<Widget> contents;

  private int selectedIndex = 0;

  protected AbstractTabLayout() {
    super();
    menu = new UList();
    super.add(getMenu());
    menu.addStyleName("tabz");
    super.add(content = new SimplePanel());
    content.addStyleName("content");

    items = new ArrayList<ListItem>();
    contents = new ArrayList<Widget>();
  }

  protected Widget getMenu() {
    return menu;
  }

  @Override
  public void add(Widget w) {
    if(items.size() == contents.size()) {
      if(w instanceof HasClickHandlers) {
        addItem((HasClickHandlers) w);
      } else {
        throw new IllegalArgumentException("HasClickHandlers expected at index " + items.size());
      }
    } else if(contents.size() == items.size() - 1) {
      contents.add(w);
      if(contents.size() == 1) {
        content.setWidget(w);
        selectedIndex = 0;
      } else {
        w.removeFromParent();
      }
    } else {
      throw new IllegalArgumentException("Alternate list of HasClickHandlers and Widget is expected.");
    }
  }

  private void addItem(HasClickHandlers item) {

    ListItem li;
    menu.add(li = new ListItem((Widget) item));
    items.add(li);

    final int index = items.size() - 1;
    item.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        selectTab(index);
      }
    });
    if(items.size() == 1) {
      li.addStyleName("first active");
    }
  }

  public void add(Widget w, String text) {
    add(w, new Anchor(text));
  }

  public void add(Widget w, HasClickHandlers item) {
    add((Widget) item);
    add(w);
  }

  private void setSelectedIndex(int index) {
    if(selectedIndex != -1) items.get(selectedIndex).removeStyleName("active");
    items.get(index).addStyleName("active");
    content.setWidget(contents.get(index));
    selectedIndex = index;
  }

  @Override
  public void clear() {
    menu.clear();
    content.clear();
    items.clear();
    contents.clear();
    selectedIndex = -1;
  }

  /**
   * Make a tab visible or not. In the case of the tab to hide is the selected one, make sure one of its visible
   * neighbour is selected in place of it.
   * @param index
   * @param visible
   */
  public void setTabVisible(int index, boolean visible) {
    checkIndex(index);
    if(!visible && index == selectedIndex) {

      // select the closest visible tab with lower index
      boolean selectionChanged = selectClosestLowerVisibleTab(index);

      // failed, so select the closest visible tab with higher index
      if(!selectionChanged) {
        selectionChanged = selectClosestHigherVisibleTab(index);
      }

      // failed, wont hide if selection cannot be replaced
      if(!selectionChanged) {
        return;
      }
    }

    items.get(index).setVisible(visible);
  }

  /**
   * Select the closest visible tab with lower index.
   * @param index
   * @return true if selection changed
   */
  private boolean selectClosestLowerVisibleTab(int index) {
    boolean selectionChanged = false;
    int idx = index - 1;
    while(!selectionChanged || idx < 0) {
      if(isTabVisible(idx)) {
        setSelectedIndex(idx);
        selectionChanged = true;
      } else {
        idx--;
      }
    }
    return selectionChanged;
  }

  /**
   * Select the closest visible tab with higher index.
   * @param index
   * @return true if selection changed
   */
  private boolean selectClosestHigherVisibleTab(int index) {
    boolean selectionChanged = false;
    int idx = index + 1;
    while(!selectionChanged || idx >= items.size()) {
      if(isTabVisible(idx)) {
        setSelectedIndex(idx);
        selectionChanged = true;
      } else {
        idx++;
      }
    }
    return selectionChanged;
  }

  /**
   * Is the tab at provided index is visible.
   * @param index
   * @return
   */
  public boolean isTabVisible(int index) {
    checkIndex(index);
    return items.get(index).isVisible();
  }

  private void checkIndex(int index) {
    assert (index >= 0) && (index < contents.size()) : "Index out of bounds";
  }

  /**
   * Programmatically selects the specified tab and fires events.
   * 
   * @param child the child whose tab is to be selected
   */
  public void selectTab(Widget child) {
    selectTab(getWidgetIndex(child));
  }

  /**
   * Programmatically selects the specified tab.
   * 
   * @param child the child whose tab is to be selected
   * @param fireEvents true to fire events, false not to
   */
  public void selectTab(Widget child, boolean fireEvents) {
    selectTab(getWidgetIndex(child), fireEvents);
  }

  /**
   * Programmatically selects the specified tab and fires events.
   * 
   * @param index the index of the tab to be selected
   */
  public void selectTab(int index) {
    selectTab(index, true);
  }

  /**
   * Programmatically selects the specified tab.
   * 
   * @param index the index of the tab to be selected
   * @param fireEvents true to fire events, false not to
   */
  public void selectTab(int index, boolean fireEvents) {
    checkIndex(index);
    if(index == selectedIndex) {
      return;
    }

    // Fire the before selection event, giving the recipients a chance to
    // cancel the selection.
    if(fireEvents) {
      BeforeSelectionEvent<Integer> event = BeforeSelectionEvent.fire(this, index);
      if((event != null) && event.isCanceled()) {
        return;
      }
    }

    // Update the tabs being selected and unselected.
    setSelectedIndex(index);

    // Fire the selection event.
    if(fireEvents) {
      SelectionEvent.fire(this, index);
    }

  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  //
  // IndexedPanel
  //

  @Override
  public Widget getWidget(int index) {
    return contents.get(index);
  }

  @Override
  public int getWidgetCount() {
    return contents.size();
  }

  @Override
  public int getWidgetIndex(Widget child) {
    return contents.indexOf(child);
  }

  @Override
  public boolean remove(int index) {
    if(index < 0 || index >= contents.size()) return false;

    items.remove(index);
    menu.remove(index);
    contents.remove(index);

    if(contents.size() == 0) {
      content.clear();
      selectedIndex = -1;
    } else if(selectedIndex > index) {
      selectedIndex--;
    } else if(selectedIndex > contents.size() - 1) {
      setSelectedIndex(contents.size() - 1);
    } else if(selectedIndex == index) {
      setSelectedIndex(index);
    }

    return true;
  }

  //
  // HasBeforeSelectionHandlers<Integer>
  //

  @Override
  public HandlerRegistration addBeforeSelectionHandler(BeforeSelectionHandler<Integer> handler) {
    return addHandler(handler, BeforeSelectionEvent.getType());
  }

  //
  // HasSelectionHandlers<Integer>
  //

  @Override
  public HandlerRegistration addSelectionHandler(SelectionHandler<Integer> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

}
