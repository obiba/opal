/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavWidget;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;
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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class AbstractTabPanel extends FlowPanel
    implements IndexedPanel, HasSelectionHandlers<Integer>, HasBeforeSelectionHandlers<Integer> {

  private final FlowPanel menuPanel;

  private final UnorderedList menu;

  private final TabDeckPanel contentContainer;

  private Heading heading;

  private int selectedIndex = -1;

  protected AbstractTabPanel(UnorderedList menu) {
    menuPanel = new FlowPanel();
    this.menu = menu;
    menuPanel.add(menu);
    super.add(menuPanel);
    super.add(contentContainer = new TabDeckPanel());
    contentContainer.addStyleName("content");
    getWidgetCount();
  }

  public void setHeading(int size, String text, String subtext) {
    if(heading != null) menuPanel.remove(heading);
    heading = new Heading(size, text);
    heading.setSubtext(subtext);
    heading.addStyleName("inline-block small-right-indent");
    menuPanel.insert(heading, 0);
    menu.addStyleName("inline");
  }

  protected UnorderedList getMenu() {
    return menu;
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

  public void setMenuVisible(boolean visible) {
    menu.setVisible(visible);
  }

  public boolean isMenuVisible() {
    return menu.isVisible();
  }

  protected void setAnimationEnabled(boolean enable) {
    contentContainer.setAnimationEnabled(enable);
  }

  protected boolean isAnimationEnabled() {
    return contentContainer.isAnimationEnabled();
  }

  protected boolean isAnimationRunning() {
    return contentContainer.isAnimationRunning();
  }

  @Override
  public void add(Widget widget) {
    if(menu.getWidgetCount() == contentContainer.getWidgetCount()) {
      addTabHeader(widget);
    } else {
      addTabContent(widget);
    }
  }

  private void addContent(Widget content) {
    insertContent(content, contentContainer.getWidgetCount());
  }

  private void insertItem(HasClickHandlers item, int beforeIndex) {
    if(beforeIndex < 0 || beforeIndex > getTabCount()) {
      throw new IndexOutOfBoundsException("cannot insert before " + beforeIndex);
    }
    final NavWidget li = newListItem((Widget) item, beforeIndex);

    if(beforeIndex == menu.getWidgetCount()) {
      menu.add(li);
    } else {
      menu.insert(li, beforeIndex);
    }

    li.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        selectTab(menu.getWidgetIndex(li));
      }
    });
  }

  @SuppressWarnings("ChainOfInstanceofChecks")
  protected NavWidget newListItem(Widget item, int beforeIndex) {
    if(item instanceof NavWidget) {
      return (NavWidget) item;
    }
    if(item instanceof HasText) {
      return new NavLink(((HasText) item).getText());
    }
    return new NavWidget(item);
  }

  private void insertContent(Widget content, int beforeIndex) {
    contentContainer.insert(content, beforeIndex);
  }

  public void add(Widget widget, String text) {
    add(widget, new NavLink(text));
  }

  public void add(Widget widget, HasClickHandlers item) {
    insert(widget, item, menu.getWidgetCount());
  }

  public void insert(Widget content, HasClickHandlers tab, int beforeIndex) {
    insertItem(tab, beforeIndex);
    insertContent(content, beforeIndex);

    if(selectedIndex < 0 || beforeIndex <= selectedIndex) {
      setSelectedIndex(beforeIndex);
    }
  }

  public void appendMenuWidget(IsWidget widget) {
    menuPanel.add(widget);
  }

  public void prependMenuWidget(IsWidget widget) {
    menuPanel.insert(widget,0);
  }

  private void setSelectedIndex(int index) {
    int i = 0;
    for(Widget child : menu) {
      child.removeStyleName("active");
      if(i++ == index) {
        child.addStyleName("active");
      }
    }
    contentContainer.showWidget(index);
    selectedIndex = index;
  }

  @Override
  public void clear() {
    menu.clear();
    contentContainer.clear();
    selectedIndex = -1;
  }

  /**
   * Make a tab visible or not. In the case of the tab to hide is the selected one, make sure one of its visible
   * neighbour is selected in place of it.
   *
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
   *
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
   *
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
   *
   * @param index
   * @return
   */
  public boolean isTabVisible(int index) {
    checkIndex(index);
    return menu.getWidget(index).isVisible();
  }

  private void checkIndex(int index) {
    assert index >= 0 && index < menu.getWidgetCount() : "Index out of bounds";
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
      if(event != null && event.isCanceled()) {
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
    return contentContainer.getWidget(index);
  }

  @Override
  public int getWidgetCount() {
    return contentContainer.getWidgetCount();
  }

  @Override
  public int getWidgetIndex(Widget child) {
    return contentContainer.getWidgetIndex(child);
  }

  @Override
  @SuppressWarnings("PMD.NcssMethodCount")
  public boolean remove(int index) {
    if(index < 0 || index >= menu.getWidgetCount()) return false;

    menu.remove(index);
    contentContainer.remove(index);

    int newSize = menu.getWidgetCount();

    if(newSize == 0) {
      menu.clear();
      contentContainer.clear();
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
