/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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
 * Wrapper of Duration object in Moment.js. See http://momentjs.com
 */
public class Duration extends JavaScriptObject {

  /**
   * Duration from time in millis. Use negative number to represent a duration before now.
   *
   * @param millis
   * @return
   */
  public static native Duration create(int millis) /*-{
      return $wnd.moment.duration(millis);
  }-*/;

  /**
   * The format is an hour, minute, second string separated by colons like 23:59:59. The number of days can be prefixed
   * with a dot separator like so 7.23:59:59. Partial seconds are supported as well 23:59:59.999.
   */
  public static native Duration create(String ellapsed) /*-{
      return $wnd.moment.duration(ellapsed);
  }-*/;

  public static Duration create(Moment start, Moment end) {
    double millis = end.valueOf() - start.valueOf();
    return create((int)millis);
  }

  /**
   * Non directly instantiable.
   */
  protected Duration() {}

  /**
   * Get the text of the duration.
   * @return
   */
  public final native String humanize() /*-{
      return this.humanize();
  }-*/;

  /**
   * Get the text representing the duration with conjonction.
   * @param suffixe
   * @return
   */
  public final native String humanize(boolean suffixe) /*-{
      return this.humanize(suffixe);
  }-*/;

  /**
   * Get the number of milliseconds in the duration.
   *
   * @return
   */
  public final native int milliseconds() /*-{
      return this.milliseconds();
  }-*/;

  /**
   * Get the length of the duration in milliseconds.
   *
   * @return
   */
  public final native int asMilliseconds() /*-{
      return this.asMilliseconds();
  }-*/;

  /**
   * Get the number of seconds in the duration.
   *
   * @return
   */
  public final native int seconds() /*-{
      return this.seconds();
  }-*/;

  /**
   * Get the length of the duration in seconds.
   *
   * @return
   */
  public final native int asSeconds() /*-{
      return this.asSeconds();
  }-*/;

  /**
   * Get the number of minutes in the duration.
   *
   * @return
   */
  public final native int minutes() /*-{
      return this.minutes();
  }-*/;

  /**
   * Get the length of the duration in seconds.
   *
   * @return
   */
  public final native int asMinutes() /*-{
      return this.asMinutes();
  }-*/;

  /**
   * Get the number of hours in the duration.
   *
   * @return
   */
  public final native int hours() /*-{
      return this.hours();
  }-*/;

  /**
   * Get the length of the duration in hours.
   *
   * @return
   */
  public final native int asHours() /*-{
      return this.asHours();
  }-*/;

  /**
   * Get the number of days in the duration.
   *
   * @return
   */
  public final native int days() /*-{
      return this.days();
  }-*/;

  /**
   * Get the length of the duration in days.
   *
   * @return
   */
  public final native int asDays() /*-{
      return this.asDays();
  }-*/;

  /**
   * Get the number of months in the duration.
   *
   * @return
   */
  public final native int months() /*-{
      return this.months();
  }-*/;

  /**
   * Get the length of the duration in months.
   *
   * @return
   */
  public final native int asMonths() /*-{
      return this.asMonths();
  }-*/;

  /**
   * Get the number of years in the duration.
   *
   * @return
   */
  public final native int years() /*-{
      return this.years();
  }-*/;

  /**
   * Get the length of the years in months.
   *
   * @return
   */
  public final native int asYears() /*-{
      return this.asYears();
  }-*/;

}
