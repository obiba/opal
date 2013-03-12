/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class BreadCrumbList extends UList {

  private String divider;

  public BreadCrumbList() {
    this("");
  }

  public BreadCrumbList(String divider) {
    super();
    setStyleName("breadcrumb");
    this.divider = divider;

  }

  public void add(Widget w) {
    if(getWidgetCount() == 0) {
      super.add(new ListItem(w));
    } else {
      ListItem item = new ListItem(newDivider());
      item.add(w);
      super.add(item);
    }
  }

  public void insert(Widget w, int beforeIndex) {
    ListItem item;
    if(beforeIndex > 0) {
      item = new ListItem(w);
    } else {
      item = new ListItem(newDivider());
      item.add(w);
    }
    super.insert(item, beforeIndex);
  }

  private InlineLabel newDivider() {
    InlineLabel div = new InlineLabel(divider);
    div.setStyleName("divider");
    // div.setVisible(Strings.isNullOrEmpty(divider) == false);
    return div;
  }

}
