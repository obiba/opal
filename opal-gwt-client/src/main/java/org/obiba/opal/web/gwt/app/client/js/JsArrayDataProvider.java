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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.view.client.ListDataProvider;

public class JsArrayDataProvider<T extends JavaScriptObject> extends ListDataProvider<T> {

  public void setArray(JsArray<T> array) {
    setList(JsArrays.toList(array));
  }

  public ColumnSortEvent.ListHandler<T> newSortHandler() {
    return new ColumnSortEvent.ListHandler<T>(getList());
  }

}
