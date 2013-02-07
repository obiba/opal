/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.domain.participant.identifier.impl;

import java.security.SecureRandom;
import java.util.Random;

import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;

/**
 * Provides a method to generate a random 10 digit {@link Participant} id. Clients are responsible to ensure that the id
 * is unique prior to use. Example ids: 7515827901, 4398790660, 0042480736.
 */
public final class DefaultParticipantIdentifierImpl implements IParticipantIdentifier {

  private final Random generator = new SecureRandom();

  private int keySize = 10;

  private boolean allowStartWithZero = false;

  private String prefix;

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

  @Override
  public String generateParticipantIdentifier() {
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

}
