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
import com.google.gwt.user.client.ui.Widget;

/**
 * A label in a &lt;pre&gt; tag
 */
public class PreformattedLabel extends Widget {

  public PreformattedLabel() {
    setElement(Document.get().createPreElement());
  }

  public PreformattedLabel(String text) {
    this();
    setText(text);
  }

  public void setText(String text) {
    getElement().setInnerText(text);
  }

}
