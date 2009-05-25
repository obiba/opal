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

/**
 * This service provides write access to the Participant Key Database.
 */
public interface IParticipantKeyWriteRegistry {

  /**
   * Method for adding or updating a key to the participant that can be identified by the reference owner/key pair. If
   * no participant is found with the reference entry, a new participant is created and both entries are added.
   * @param refOwner Existing owner. Will be added if it doesn't exist.
   * @param refKey Existing key. Will be added if it doesn't exist.
   * @param owner New owner, we are going to add.
   * @param key New key, we are going to add.
   * @throws IllegalArgumentException If any supplied argument is null.
   * @throws IllegalStateException If supplied {@code owner/key} pair are not unique.
   */
  public void registerEntry(String refOwner, String refKey, String owner, String key);

  /**
   * Method for deleting an entry and only the entry corresponding to the given owner. If it was participant's only
   * entry, the participant is deleted. If there was no participant, the call simply returns.
   * @param owner Owner to be deleted.
   * @param key Key to be deleted.
   * @throws IllegalArgumentException If any supplied argument is null.
   */
  public void unregisterEntry(String owner, String key);

  /**
   * Method for deleting a participant corresponding to the given owner/key and all its entries. If there was no
   * participant, the call simply returns. The use case is when a participant requests for being removed from the
   * system, after having completed or while completing the study. It is the responsibility of the caller to delete the
   * data associated to the participant (this works if data are in one system at a time).
   * @param owner Participant associated with this owner will be deleted.
   * @param key Participant associated with this key will be deleted.
   * @throws IllegalArgumentException If any supplied argument is null.
   */
  public void unregisterParticipant(String owner, String key);

  /**
   * Generates a unique key for the supplied {@code owner}. Uniqueness is determined by ensuring that the {@code
   * owner/key} pair does not currently exist in the Participant Key database.
   * @param owner A unique key will be generated for this owner.
   * @return A unique key.
   * @throws IllegalStateException If unable to generate a unique key.
   */
  public String generateUniqueKey(String owner);
}
