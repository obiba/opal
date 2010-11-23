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

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class ListItem extends ComplexPanel implements HasText {

  public ListItem() {
    super();
    setElement(Document.get().createLIElement());
  }

  public ListItem(Widget w) {
    this();
    add(w);
  }

  public void add(Widget w) {
    super.add(w, getElement());
  }

  public void insert(Widget w, int beforeIndex) {
    super.insert(w, getElement(), beforeIndex, true);
  }

  public String getText() {
    return DOM.getInnerText(getElement());
  }

  public void setText(String text) {
    DOM.setInnerText(getElement(), (text == null) ? "" : text);
  }

}
