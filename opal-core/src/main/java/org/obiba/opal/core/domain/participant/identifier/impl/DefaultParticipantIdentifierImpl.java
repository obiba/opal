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

import java.util.Random;

import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;

/**
 * Provides a method to generate a random 10 digit {@link Participant} id. Clients are responsible to ensure that the id
 * is unique prior to use. Example ids: 7515827901, 4398790660, 0042480736.
 */
public class DefaultParticipantIdentifierImpl implements IParticipantIdentifier {

  public String generateParticipantIdentifier() {
    Random generator = new Random();
    StringBuilder sb = new StringBuilder(10);
    for(int i = 0; i < 10; i++) {
      sb.append(generator.nextInt(10));
    }
    return sb.toString();
  }

}
