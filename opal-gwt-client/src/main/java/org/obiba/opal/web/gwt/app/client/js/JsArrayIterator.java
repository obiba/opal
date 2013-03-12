/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.js;

import java.util.Iterator;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 *
 */
public class JsArrayIterator<T extends JavaScriptObject> implements Iterator<T> {

  private final JsArray<T> array;

  private int index = 0;

  public JsArrayIterator(JsArray<T> array) {
    this.array = array;
  }

  @Override
  public boolean hasNext() {
    return index < array.length();
  }

  @Override
  public T next() {
    return array.get(index++);
  }

  @Override
  public void remove() {
  }
}
