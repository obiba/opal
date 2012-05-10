/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.prettify.client;

import org.obiba.opal.web.gwt.prettify.client.Resources.Languages;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;

/**
 * A label widget that pretty-prints its content.
 * <p>
 * Uses <a href="http://google-code-prettify.googlecode.com">google-code-prettify<a/>
 */
public class PrettyPrintLabel extends Widget {

  // TODO: allow specifying the language
  @SuppressWarnings("unused")
  private static final Languages langs = Resources.INSTANCE.langs();

  public PrettyPrintLabel() {
    setElement(Document.get().createPreElement());
    setStyleName("prettyprint");
  }

  public void setText(String text) {
    getElement().setInnerText(text);
    prettyPrint();
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    prettyPrint();
  }

  private final native void prettyPrint()
  /*-{
     $wnd.prettyPrint();
   }-*/;

}
