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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class VerticalTabLayoutPanel extends FlowPanel {

  private UList menu;

  private SimplePanel content;

  private List<ListItem> items;

  private List<Widget> contents;

  private int active = 0;

  public VerticalTabLayoutPanel() {
    super();
    addStyleName("vertical-tabs");
    add(menu = new UList());
    menu.addStyleName("vertical tabs");
    add(content = new SimplePanel());
    content.addStyleName("content");

    items = new ArrayList<ListItem>();
    contents = new ArrayList<Widget>();
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
      public void onClick(ClickEvent arg0) {
        items.get(active).removeStyleName("active");
        active = index;
        items.get(active).addStyleName("active");
        content.setWidget(contents.get(index));
      }
    });
    if(items.size() == 1) {
      li.addStyleName("first active");
    }
  }

  public void addTab(HasClickHandlers item, Widget w) {
    addItem(item);
    add(w);
  }

  public void setActive(int index) {
    items.get(active).removeStyleName("active");
    items.get(index).addStyleName("active");
    content.setWidget(contents.get(index));
  }

  public void removeTab(int index) {
    items.remove(index);
    contents.remove(index);
    if(active == index) {
      setActive(index);
    } else if(active > index) {
      active--;
    }
  }

}
