/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.sql;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

public class SQLResult extends JavaScriptObject {
  protected SQLResult() {}

  // JSNI methods to get data.
  public final native String getError() /*-{ return this.error; }-*/;
  public final native JsArrayString getColumns() /*-{ return this.columns; }-*/;
  public final native JsArray<JsArray<JavaScriptObject>> getRows() /*-{ return this.rows; }-*/;

  public final boolean hasError() {
    return !Strings.isNullOrEmpty(getError());
  }
}
