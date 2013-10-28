package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;

public class OpalNavLink extends NavLink {

  public OpalNavLink() {
  }

  public OpalNavLink(String text, String historyToken) {
    super(text);
    if (!Strings.isNullOrEmpty(historyToken)) setHistoryToken(historyToken);
  }

  public void setHistoryToken(final String historyToken) {
    addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        History.newItem(historyToken);
      }
    });
  }

}
