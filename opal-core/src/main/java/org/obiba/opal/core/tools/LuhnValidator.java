/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
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

  /**
   * Generate an identifier that is Luhn valid.
   *
   * @return
   */
  public static String generate() {
    return generate(15);
  }

  /**
   * Generate an identifier that is Luhn valid.
   *
   * @param size
   * @return
   */
  public static String generate(int size) {
    IdentifierGeneratorImpl pId = new IdentifierGeneratorImpl();
    pId.setKeySize(size);
    pId.setAllowStartWithZero(false);
    pId.setPrefix("");
    String gen = null;
    while (gen == null) {
      long value = Long.parseLong(pId.generateIdentifier());
      String valueCd = "" + value + generateCheckDigit(value);
      if (validate(valueCd)) {
        gen = valueCd;
      }
    }
    return gen;
  }

  /**
   * Generate the check digit.
   *
   * @param l
   * @return
   */
  private static int generateCheckDigit(long l) {
    String str = Long.toString(l);
    int[] ints = new int[str.length()];
    for (int i = 0; i < str.length(); i++) {
      ints[i] = Integer.parseInt(str.substring(i, i + 1));
    }
    for (int i = ints.length - 2; i >= 0; i = i - 2) {
      int j = ints[i];
      j = j * 2;
      if (j > 9) {
        j = j % 10 + 1;
      }
      ints[i] = j;
    }
    int sum = 0;
    for (int i = 0; i < ints.length; i++) {
      sum += ints[i];
    }
    if (sum % 10 == 0) {
      return 0;
    } else return 10 - (sum % 10);
  }
}
