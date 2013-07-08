package org.obiba.opal.web.gwt.app.client.support;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.event.dom.client.ClickHandler;

public class BreadcrumbsBuilder {

  private List<Item> items = new ArrayList<Item>();

  public BreadcrumbsBuilder setItems(List<Item> items) {
    this.items = items;
    return this;
  }

  public Breadcrumbs build() {
    Breadcrumbs breadcrumbs = new Breadcrumbs();

    for (Item item : items) {
      NavLink link = new NavLink(item.getName());
      breadcrumbs.add(link);
      ClickHandler clickHandler = item.getClickHandler();
      if (clickHandler != null) {
        link.addClickHandler(clickHandler);
      }
    }

    return breadcrumbs;
  }


  public static class Item {
    private String name;
    private ClickHandler clickHandler;

    public Item(String name, ClickHandler clickHandler) {
      this.name = name;
      this.clickHandler = clickHandler;
    }

    public String getName() {
      return name;
    }

    public ClickHandler getClickHandler() {
      return clickHandler;
    }
  }

  public static class ItemsBuilder {

    public ItemsBuilder() {}

    List<Item> items = new ArrayList<Item>();

    public ItemsBuilder addItem(String name, ClickHandler clickHandler) {
      items.add(new Item(name, clickHandler));
      return this;
    }

    public ItemsBuilder addItem(String name) {
      return addItem(name, null);
    }

    public List<Item> build() {
      return items;
    }
  }

}
