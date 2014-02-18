/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import java.util.ArrayList;
import java.util.Collection;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;

import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasRows;

public class OpalSimplePager extends SimplePager {

  protected final TranslationMessages translationMessages = GWT.create(TranslationMessages.class);
  private final HTML label = new HTML();
  private final Panel panel;
  private final Collection<Widget> pagerWidgets = new ArrayList<>();

  @UiConstructor
  public OpalSimplePager(TextLocation location) {
    super(location);

    label.addStyleName("celltable-total-count");
    panel = (Panel)getWidget();
    assert panel != null;

    for(Widget child : panel) {
      pagerWidgets.add(child);
    }
  }

  public void setPagerVisible(boolean visible) {
    for(Widget child : pagerWidgets) {
      child.setVisible(visible);
    }

    HasRows display = getDisplay();
    assert display != null;
    int total = display.getRowCount();

    if (visible) {
      panel.remove(label);
    } else if (total > 0) {
      label.setText(translationMessages.cellTableTotalCount(total));
      panel.add(label);
    }

  }
}
