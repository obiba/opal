/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r;

import org.apache.commons.lang3.LocaleUtils;

import java.io.File;
import java.util.Locale;

public class RUtils {

  /**
   * Make the R symbol corresponding to the provided string.
   *
   * @param name
   * @return
   */
  public static String getSymbol(String name) {
    String symbol = name.replaceAll(" ", "_");
    int suffix = symbol.lastIndexOf(".");
    if (suffix > 0) {
      symbol = symbol.substring(0, suffix);
    }
    return symbol;
  }

  /**
   * Make the R symbol that will refer to the data file.
   *
   * @param file
   * @return
   */
  public static String getSymbol(File file) {
    return getSymbol(file.getName());
  }

  /**
   * Validate locale 2-letters string extracted from label.
   *
   * @param localeStr
   * @return
   */
  public static boolean isLocaleValid(String localeStr) {
    try {
      Locale locale = Locale.forLanguageTag(localeStr);
      return LocaleUtils.isAvailableLocale(locale);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Escape some special characters.
   *
   * @param label
   * @return
   */
  public static String normalizeLabel(String label) {
    return label.replace("\\", "\\\\").replace("'", "\\'");
  }
}
