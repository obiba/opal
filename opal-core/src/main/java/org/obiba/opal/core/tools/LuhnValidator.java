/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.tools;

import com.google.common.base.Strings;
import org.obiba.opal.core.identifiers.IdentifierGeneratorImpl;

/**
 * Luhn number validator.
 */
public class LuhnValidator {

  public static boolean validate(String ccNumber, int length) {
    if (!validate(ccNumber)) return false;
    return ccNumber.length() == length;
  }

  /**
   * Luhn validation and exclude number with all zeros.
   *
   * @param ccNumber
   * @return
   */
  public static boolean validate(String ccNumber) {
    String toValidate = ccNumber == null ? "" : ccNumber.trim();
    if (Strings.isNullOrEmpty(toValidate)) return false;

    int zeros = 0;
    try {
      int sum = 0;
      boolean alternate = false;
      for (int i = toValidate.length() - 1; i >= 0; i--) {
        int n = Integer.parseInt(toValidate.substring(i, i + 1));
        if (n == 0) zeros++;
        if (alternate) {
          n *= 2;
          if (n > 9) {
            n = (n % 10) + 1;
          }
        }
        sum += n;
        alternate = !alternate;
      }
      return zeros < toValidate.length() && (sum % 10 == 0);
    } catch (NumberFormatException e) {
      return false;
    }
  }

}
