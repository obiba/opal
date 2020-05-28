/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

public final class Strings {

  private Strings() {}

  private final static String NON_THIN = "[^iIl1\\.,']";

  public static String abbreviate(String text, int max) {

    if(textWidth(text) <= max) return text;

    // Start by chopping off at the word before max
    // This is an over-approximation due to thin-characters...
    int end = text.lastIndexOf(' ', max - 3);

    // Just one long word. Chop it off.
    if(end == -1) return text.substring(0, max - 3) + "...";

    // Step forward as long as textWidth allows.
    int newEnd = end;
    do {
      end = newEnd;
      newEnd = text.indexOf(' ', end + 1);

      // No more spaces.
      if(newEnd == -1) newEnd = text.length();

    } while(textWidth(text.substring(0, newEnd) + "...") < max);

    return text.substring(0, end) + "...";
  }

  private static int textWidth(String str) {
    return (int) (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
  }

}
