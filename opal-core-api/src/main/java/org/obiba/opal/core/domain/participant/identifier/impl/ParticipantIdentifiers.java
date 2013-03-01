/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.domain.participant.identifier.impl;

import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;

/**
 *
 */
public class ParticipantIdentifiers {

  public static final IParticipantIdentifier UNSUPPORTED = new IParticipantIdentifier() {
    @Override
    public String generateParticipantIdentifier() {
      throw new UnsupportedOperationException("cannot generate identifier");
    }
  };

  private ParticipantIdentifiers() {}
}
