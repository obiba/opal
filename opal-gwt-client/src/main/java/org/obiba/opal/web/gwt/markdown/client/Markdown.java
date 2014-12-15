/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.markdown.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Wrapper of marked Object in Marked. See https://github.com/chjj/marked
 */
public class Markdown extends JavaScriptObject {

  /**
   * Turns markdown to html.
   *
   * @param markdown markdown text
   * @return
   */
  public static native String parse(String markdown) /*-{
      return '<div class="markdown">' + $wnd.marked(markdown) + '</div>';
  }-*/;

  /**
   * Non directly instantiable.
   */
  protected Markdown() {}
}
