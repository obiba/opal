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

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

/**
 *
 */
public class JsArrays {

  private JsArrays() {
  }

  /**
   * Concatenates the provided arrays into a newly constructed array. Returns the new array.
   */
  public static <T extends JavaScriptObject> JsArray<T> concat(JsArray<T>... arrays) {
    JsArray<T> concat = JavaScriptObject.createArray().cast();
    return pushAll(concat, arrays);
  }

  /**
   * Pushes elements from all provided arrays into {@code lhs}
   */
  public static <T extends JavaScriptObject> JsArray<T> pushAll(JsArray<T> lhs, JsArray<T>... arrays) {
    for(JsArray<T> array : arrays) {
      pushAll(lhs, array);
    }
    return lhs;
  }

  /**
   * Pushes elements from {@code rhs} into {@code lhs}
   */
  public static <T extends JavaScriptObject> JsArray<T> pushAll(JsArray<T> lhs, JsArray<T> rhs) {
    if(rhs != null) {
      for(int i = 0; i < rhs.length(); i++) {
        lhs.push(rhs.get(i));
      }
    }
    return lhs;
  }

  public static Iterable<String> toIterable(JsArrayString values) {
    final JsArrayString array = values != null ? values : (JsArrayString) JsArrayString.createArray();
    return new Iterable<String>() {

      @Override
      public Iterator<String> iterator() {
        return new Iterator<String>() {

          private int next = 0;

          @Override
          public boolean hasNext() {
            return next < array.length();
          }

          @Override
          public String next() {
            return array.get(next++);
          }

          @Override
          public void remove() {

          }
        };
      }

    };
  }

  public static <T extends JavaScriptObject> Iterable<T> toIterable(JsArray<T> values) {
    final JsArray<T> array = toSafeArray(values);
    return new Iterable<T>() {

      @Override
      public Iterator<T> iterator() {
        return new JsArrayIterator<T>(array);
      }

    };
  }

  public static <T extends JavaScriptObject> T[] toArray(JsArray<? extends T> values) {
    if(GWT.isScript()) {
      return reinterpretCast(values);
    }
    int length = values.length();
    @SuppressWarnings("unchecked")
    T[] ret = (T[]) new JavaScriptObject[length];
    for(int i = 0, l = length; i < l; i++) {
      ret[i] = values.get(i);
    }
    return ret;
  }

  public static <T extends JavaScriptObject> List<T> toList(JsArray<T> jsArray) {
    final JsArray<T> array = toSafeArray(jsArray);
    return new AbstractList<T>() {

      @Override
      public T get(int index) {
        return array.get(index);
      }

      @Override
      public T set(int index, T item) {
        array.set(index, item);
        return item;
      }

      @Override
      public int size() {
        return array.length();
      }

    };
  }

  public static List<String> toList(JsArrayString jsArray) {
    final JsArrayString array = jsArray == null ? (JsArrayString) JsArrayString.createArray() : jsArray;
    return new AbstractList<String>() {

      @Override
      public String get(int index) {
        return array.get(index);
      }

      @Override
      public String set(int index, String element) {
        array.set(index, element);
        return element;
      }

      @Override
      public int size() {
        return array.length();
      }

    };
  }

  /**
   * Creates a {@code List} that is a view of a portion of the supplied array. This method does not copy the supplied
   * array. As such, any modifications made to the array will be reflected in the sub-list. The returned list is
   * immutable.
   * @param <T> the type of element in the array
   * @param array the array used to back the returned list
   * @param start the index within the array that will become the 0th element in the returned list
   * @param length the size of the returned list
   * @return a view of the array as a {@code List} that contains elements array[start] to array[start + length] (or
   * array[array.length] if start + lenght > array.length)
   */
  public static <T extends JavaScriptObject> List<T> toList(final JsArray<T> array, final int start, final int length) {
    if(array == null) throw new IllegalArgumentException("array cannot be null");
    if(start < 0 || start > array.length()) throw new IndexOutOfBoundsException("start index '" + start + "'is invalid");
    if(length < 0) throw new IndexOutOfBoundsException("length '" + length + "'is invalid");
    return new AbstractList<T>() {

      transient int size = -1;

      @Override
      public T get(int index) {
        return array.get(index + start);
      }

      @Override
      public int size() {
        if(size == -1) {
          // size is either "length" or the number of elements that exist between "start" and the array's last item
          // "array.lenght()" (array.length() - start)
          size = (start + length) > array.length() ? array.length() - start : length;
        }
        return size;
      }

    };
  }

  @SuppressWarnings("unchecked")
  public static <T extends JavaScriptObject> JsArray<T> create() {
    return (JsArray<T>) JavaScriptObject.createArray();
  }

  public static <T extends JavaScriptObject> JsArray<T> toSafeArray(JsArray<T> array) {
    if(array == null) {
      return create();
    }
    return array;
  }

  public static <T extends JavaScriptObject> JsArrayString toSafeArray(JsArrayString array) {
    if(array == null) {
      return (JsArrayString) JavaScriptObject.createArray();
    }
    return array;
  }

  private static native <T extends JavaScriptObject> T[] reinterpretCast(JsArray<T> value) /*-{ return value; }-*/;

}
