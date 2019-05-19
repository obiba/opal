/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.datetime.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Wrapper of Moment object in Moment.js. See http://momentjs.com
 */
public class Moment extends JavaScriptObject {

  /**
   * Set globally the language of the date strings.
   * @param locale
   */
  public static native void lang(String locale) /*-{
      $wnd.moment.lang(locale);
  }-*/;

  /**
   * Current date and time.
   */
  public static native Moment now() /*-{
     return $wnd.moment();
  }-*/;

  /**
   * Parse date and time with ISO-8601 format auto detection.
   */
  public static native Moment create(String date) /*-{
      return $wnd.moment(date);
  }-*/;

  /**
   * Parse date and time with provided format.
   */
  public static native Moment create(String date, String format) /*-{
      return $wnd.moment(date, format);
  }-*/;

  /**
   * Parse the date from the Unix EPOCH time in seconds.
   * @param epoch seconds since the Unix EPOCH
   * @return
   */
  public static native Moment create(int epoch) /*-{
      return $wnd.moment.unix(epoch);
  }-*/;

  /**
   * Non directly instantiable.
   */
  protected Moment() {}

  /**
   * Check if is valid after date string parsing.
   * @return
   */
  public final native boolean isValid() /*-{
      return this.isValid();
  }-*/;

  /**
   * Get the text about time elapsed from now.
   * @return
   */
  public final native String fromNow() /*-{
      return this.fromNow();
  }-*/;

  /**
   * Get the text about time elapsed from the given moment.
   * @param from
   * @return
   */
  public final native String from(Moment from) /*-{
      return this.from(from);
  }-*/;


  /**
   * Get the calendar.
   * @return
   */
  public final native String calendar() /*-{
      return this.calendar();
  }-*/;

  /**
   * Get the localized text representation of the date/time.
   * @param type
   * @return
   */
  public final String format(FormatType type) {
    return format(type.get());
  }

  /**
   * Get the text representation of the date/time.
   * @param type
   * @return
   */
  public final native String format(String type) /*-{
      return this.format(type);
  }-*/;

  /**
   * Get the Unix EPOCH time in seconds.
   * @return
   */
  public final native int unix() /*-{
      return this.unix();
  }-*/;

  /**
   * Get the Unix EPOCH time in milliseconds.
   * @return
   */
  public final native double valueOf() /*-{
      return this.valueOf();
  }-*/;

}
