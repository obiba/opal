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

import java.util.List;

import com.google.common.collect.Lists;
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
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class AbstractTabLayout extends FlowPanel implements IndexedPanel, HasSelectionHandlers<Integer>, HasBeforeSelectionHandlers<Integer> {

  private final UList menu;

  private final SimplePanel contentContainer;

  private final List<Widget> tabContents = Lists.newLinkedList();

  private int selectedIndex = -1;

  protected AbstractTabLayout(String menuStyleName) {
    super();
    menu = new UList();
    menu.addStyleName(menuStyleName);
    menu.addStyleName("tabz");
    super.add(menu);
    super.add(contentContainer = new SimplePanel());
    contentContainer.addStyleName("content");
  }

  @UiChild(tagname = "tab")
  public void addTabHeader(Widget tab) {
    insertItem((HasClickHandlers) tab, menu.getWidgetCount());
  }

  @UiChild(tagname = "content")
  public void addTabContent(Widget content) {
    addContent(content);
    if(getTabCount() == 1) {
      selectTab(0);
    }
  }

  @Override
  public void add(Widget w) {
    if(menu.getWidgetCount() == tabContents.size()) {
      addTabHeader(w);
    } else {
      addTabContent(w);
    }
  }

  private void addContent(Widget content) {
    insertContent(content, tabContents.size());
  }

  private void insertItem(HasClickHandlers item, int beforeIndex) {
    if(beforeIndex < 0 || beforeIndex > getTabCount()) {
      throw new IndexOutOfBoundsException("cannot insert before " + beforeIndex);
    }
    final ListItem li;
    menu.insert(li = new ListItem((Widget) item), beforeIndex);

    item.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        selectTab(menu.getWidgetIndex(li));
      }
    });
  }

  private void insertContent(Widget content, int beforeIndex) {
    tabContents.add(beforeIndex, content);
  }

  public void add(Widget w, String text) {
    add(w, new Anchor(text));
  }

  public void add(Widget w, HasClickHandlers item) {
    insert(w, item, menu.getWidgetCount());
  }

  public void insert(Widget content, HasClickHandlers tab, int beforeIndex) {
    insertItem((HasClickHandlers) tab, beforeIndex);
    insertContent(content, beforeIndex);

    if(selectedIndex < 0 || beforeIndex <= selectedIndex) {
      setSelectedIndex(beforeIndex);
    }
  }

  private void setSelectedIndex(int index) {
    int i = 0;
    for(Widget child : menu) {
      child.removeStyleName("active");
      if(i++ == index) {
        child.addStyleName("active");
      }
    }
    contentContainer.setWidget(tabContents.get(index));
    selectedIndex = index;
  }

  @Override
  public void clear() {
    menu.clear();
    contentContainer.clear();
    tabContents.clear();
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

    menu.getWidget(index).setVisible(visible);
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
    while(!selectionChanged || idx >= menu.getWidgetCount()) {
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
    return menu.getWidget(index).isVisible();
  }

  private void checkIndex(int index) {
    assert (index >= 0) && (index < menu.getWidgetCount()) : "Index out of bounds";
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

  public int getTabCount() {
    return menu.getWidgetCount();
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  //
  // IndexedPanel
  //

  @Override
  public Widget getWidget(int index) {
    return tabContents.get(index);
  }

  @Override
  public int getWidgetCount() {
    return tabContents.size();
  }

  @Override
  public int getWidgetIndex(Widget child) {
    return tabContents.indexOf(child);
  }

  @Override
  @SuppressWarnings("PMD.NcssMethodCount")
  public boolean remove(int index) {
    if(index < 0 || index >= menu.getWidgetCount()) return false;

    menu.remove(index);
    tabContents.remove(index);

    int newSize = menu.getWidgetCount();

    if(newSize == 0) {
      menu.clear();
      tabContents.clear();
      selectedIndex = -1;
    } else if(selectedIndex > index) {
      selectedIndex--;
    } else if(selectedIndex > newSize - 1) {
      setSelectedIndex(newSize - 1);
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
