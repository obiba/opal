/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.identifiers;

import java.security.SecureRandom;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provides a method to generate a random n digit, optionally prefixed . Clients are responsible to ensure that the id
 * is unique prior to use. Example ids: 7515827901, 4398790660, 0042480736.
 */
@Component
public final class IdentifierGeneratorImpl implements IdentifierGenerator {

  private final Random generator = new SecureRandom();

  @Value("${org.obiba.opal.identifiers.length}")
  private int keySize = 10;

  @Value("${org.obiba.opal.identifiers.zeros}")
  private boolean allowStartWithZero = false;

  @Value("${org.obiba.opal.identifiers.prefix}")
  private String prefix;

  @Value("${org.obiba.opal.identifiers.checksum}")
  private boolean withCheckDigit = false;

  public void setKeySize(int keySize) {
    this.keySize = keySize;
  }

  public void setAllowStartWithZero(boolean allowStartWithZero) {
    this.allowStartWithZero = allowStartWithZero;
  }

  public boolean isAllowStartWithZero() {
    return allowStartWithZero;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  private int getPrefixLength() {
    return prefix != null ? prefix.length() : 0;
  }

  public boolean isWithCheckDigit() {
    return withCheckDigit;
  }

  public void setWithCheckDigit(boolean withCheckDigit) {
    this.withCheckDigit = withCheckDigit;
  }

  @Override
  public String generateIdentifier() {
    return withCheckDigit ? generateLuhnValidRandomIdentifier() : generateRandomIdentifier();
  }

  private String generateRandomIdentifier() {
    if(keySize < 1) {
      throw new IllegalStateException("keySize must be at least 1: " + keySize);
    }

    StringBuilder sb = new StringBuilder(keySize + getPrefixLength());

    if(getPrefixLength() > 0) {
      sb.append(prefix);
    }

    sb.append(allowStartWithZero //
        ? generator.nextInt(10) //
        : generator.nextInt(9) + 1 // Generate a random number between 0 and 8, then add 1.
    );
    for(int i = 1; i < keySize; i++) {
      sb.append(generator.nextInt(10));
    }

    return sb.toString();
  }

  private String generateLuhnValidRandomIdentifier() {
    if(keySize < 2) {
      throw new IllegalStateException("keySize must be at least 2: " + keySize);
    }

    StringBuilder sb = new StringBuilder(keySize + getPrefixLength());

    sb.append(generator.nextInt(9) + 1); // First digit shouldn't be 0

    for(int i = 1; i < keySize - 1; i++) {
      sb.append(generator.nextInt(10));
    }

    sb.append(generateCheckDigit(Long.parseLong(sb.toString())));

    if (getPrefixLength() > 0) sb.insert(0, prefix);

    return sb.toString();
  }

  /**
   * Generate Luhn check digit.
   *
   * @param input
   * @return
   */
  private int generateCheckDigit(long input) {
    String[] sLong = Long.toString(input).split("");
    int length = sLong.length;
    boolean lengthIsPair = length % 2 == 0;
    int sumDigits = 0;

    for(int i = length - 1; i > -1; i--) {
      int digit = Integer.valueOf(sLong[i]);
      if ((lengthIsPair && i % 2 != 0) || (!lengthIsPair && i % 2 == 0)) digit *= 2;
      if (digit > 9) digit -= 9;
      sumDigits += digit;
    }

    return (9 * sumDigits) % 10;
  }

}
