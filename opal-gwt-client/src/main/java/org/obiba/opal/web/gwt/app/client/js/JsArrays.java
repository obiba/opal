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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayBoolean;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.core.client.JsArrayString;

/**
 *
 */
public class JsArrays {

  private JsArrays() {
  }

  public static <T extends JavaScriptObject> Iterable<T> toIterable(final JsArray<T> values) {
    return new Iterable<T>() {

      @Override
      public Iterator<T> iterator() {
        return new JsArrayIterator<T>(values);
      }

    };
  }

  public static <T extends JavaScriptObject> T[] toArray(JsArray<? extends T> values) {
    if(GWT.isScript()) {
      return reinterpretCast(values);
    } else {
      int length = values.length();
      @SuppressWarnings("unchecked")
      T[] ret = (T[]) new JavaScriptObject[length];
      for(int i = 0, l = length; i < l; i++) {
        ret[i] = values.get(i);
      }
      return ret;
    }
  }

  private static native JsArrayString reinterpretCast(String[] value) /*-{ return value; }-*/;

  private static native String[] reinterpretCast(JsArrayString value) /*-{ return value; }-*/;

  private static native JsArrayBoolean reinterpretCast(boolean[] value) /*-{ return value; }-*/;

  private static native boolean[] reinterpretCast(JsArrayBoolean value) /*-{ return value; }-*/;

  private static native JsArrayInteger reinterpretCast(int[] value) /*-{ return value; }-*/;

  private static native int[] reinterpretCast(JsArrayInteger value) /*-{ return value; }-*/;

  private static native JsArrayNumber reinterpretCast(double[] value) /*-{ return value; }-*/;

  private static native double[] reinterpretCast(JsArrayNumber value) /*-{ return value; }-*/;

  private static native <T extends JavaScriptObject> JsArray<T> reinterpretCast(T[] value) /*-{ return value; }-*/;

  private static native <T extends JavaScriptObject> T[] reinterpretCast(JsArray<T> value) /*-{ return value; }-*/;

}
