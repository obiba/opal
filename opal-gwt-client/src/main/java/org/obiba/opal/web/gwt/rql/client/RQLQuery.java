/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.rql.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class RQLQuery extends JavaScriptObject {

  public final native String getName() /*-{ return this.name; }-*/;

  public final native int getArgumentsSize() /*-{
    return this.args === undefined ? 0 : this.args.length;
  }-*/;

  public final native boolean isRQLQuery(int idx) /*-{
    return this.args === undefined ? false : (this.args[idx] instanceof $wnd.RqlQuery);
  }-*/;

  public final native RQLQuery getRQLQuery(int idx) /*-{
    return this.args === undefined ? undefined : this.args[idx];
  }-*/;

  public final native String getString(int idx) /*-{
    return this.args === undefined ? undefined : "" + this.args[idx];
  }-*/;

  public final native boolean isArray(int idx) /*-{
    return this.args === undefined ? false : (this.args[idx] instanceof $wnd.Array);
  }-*/;

  public final native JsArrayString getArray(int idx) /*-{
    return this.args === undefined ? undefined : this.args[idx];
  }-*/;

  public final native String asString() /*-{
    var stringifier = function (part) {
      var encodeStr = function (s) {
        if (typeof s === "string") {
          s = encodeURIComponent(s);
          if (s.match(/[\(\)]/)) {
            s = s.replace("(", "%28").replace(")", "%29");
          };
        }
        return encodeURIComponent(s);
      };
      var serializeArguments = function(array, delimiter) {
        var results = [];
        for (var i = 0, l = array.length; i < l; i++) {
          results.push(stringifier(array[i]));
        }
        return results.join(delimiter);
      };
      if (part && part.name && part.args) {
        return [
          part.name,
          "(",
          serializeArguments(part.args, ","),
          ")"
        ].join("");
      }
      if (part instanceof Array || typeof part === "object") {
        return '(' + serializeArguments(part, ",") + ')';
      }
      return encodeStr(part);
    };
    var qStr = stringifier(this);
    return decodeURIComponent(qStr.split("string:").join(""));
  }-*/;

  public final native String asStringOriginal() /*-{
    return decodeURIComponent(this.queryToString(this).split("string:").join(""));
  }-*/;

  public final native String stringify() /*-{
    return $wnd.JSON.stringify(this);
  }-*/;

  protected RQLQuery() {
  }
}
