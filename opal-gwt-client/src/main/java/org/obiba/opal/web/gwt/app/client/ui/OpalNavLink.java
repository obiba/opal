/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
