/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service;

import java.util.Collection;

/**
 * This service provides read only access to the Participant Key Database.
 */
public interface IParticipantKeyReadRegistry {

  /** Name used by Opal to store entries in the Participant Key database. */
  public static final String PARTICIPANT_KEY_DB_OPAL_NAME = "opal";

  /**
   * Method for getting the key list for a owner from a participant that can be identified by the reference owner/key
   * pair. If no participant is found with the reference entry, null is returned. If the participant was found but has
   * no entry for the given additional owner, null is returned too (then see hasParticipant() method for
   * disambiguation).
   * @param refOwner Existing owner.
   * @param refKey Existing key.
   * @param owner Existing owner. We would like the keys associated with this owner.
   * @return All the keys associated with the owner.
   * @throws IllegalArgumentException If any supplied argument is null.
   */
  public Collection<String> getEntry(String refOwner, String refKey, String owner);

  /**
   * Method for checking if there is a participant corresponding to the given owner/key.
   * @param owner Owner associated with the Participant we are looking for.
   * @param key Key associated with the Participant we are looking for.
   * @return True if the Participant exists.
   * @throws IllegalArgumentException If any supplied argument is null.
   */
  public boolean hasParticipant(String owner, String key);

}
