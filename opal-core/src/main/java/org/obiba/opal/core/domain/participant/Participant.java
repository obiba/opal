/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.domain.participant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Represents a participant in the Participant Key Database.
 */
@Entity
public final class Participant {
  //
  // Instance Variables
  //

  @SuppressWarnings("unused")
  @Id
  @GeneratedValue
  @Column
  private long id;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "participant")
  private List<ParticipantKey> keys;

  //
  // Constructors
  //

  public Participant() {
    keys = new ArrayList<ParticipantKey>();
  }

  //
  // Methods
  //

  /**
   * Adds the {@code owner/key} pair.
   * @param owner The owner of the key.
   * @param key The key associated with the owner.
   * @throws IllegalArgumentException If any supplied argument is null.
   * @throws IllegalStateException If supplied {@code owner/key} pair is not unique.
   */
  public void addEntry(String owner, String key) {
    if(owner == null) throw new IllegalArgumentException("The owner must not be null.");
    if(key == null) throw new IllegalArgumentException("The key must not be null.");
    if(hasEntry(owner, key)) throw new IllegalStateException("The owner/key pair [" + owner + "]=[" + key + "] already exists. Duplicates are not permitted.");

    ParticipantKey participantKey = new ParticipantKey();
    participantKey.setParticipant(this);
    participantKey.setOwner(owner);
    participantKey.setValue(key);
    keys.add(participantKey);
  }

  /**
   * Returns a {@link Collection} of keys associated with the owner.
   * @param owner The owner of the keys.
   * @return A {@code Collection} of keys.
   */
  public Collection<String> getKeys(String owner) {
    List<String> ownerKeys = new ArrayList<String>();
    for(ParticipantKey key : keys) {
      if(key.getOwner().equals(owner)) {
        ownerKeys.add(key.getValue());
      }
    }

    return Collections.unmodifiableCollection(ownerKeys);
  }

  /**
   * Returns true if the {@code owner/key} pair exists.
   * @param owner The owner of the key.
   * @param key The key associated with the owner.
   * @return True if the {@code owner/key} pair exists, false otherwise.
   */
  public boolean hasEntry(String owner, String key) {
    for(ParticipantKey aKey : keys) {
      if(aKey.getOwner().equals(owner) && aKey.getValue().equals(key)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Removes the {@code owner/key} pair if they exist. This method does nothing if the {@code owner/key} pair does not
   * exist.
   * @param owner The owner of the key.
   * @param key The key associated with the owner.
   */
  public void removeEntry(String owner, String key) {
    ParticipantKey matchingEntry = null;

    for(ParticipantKey aKey : keys) {
      if(aKey.getOwner().equals(owner) && aKey.getValue().equals(key)) {
        matchingEntry = aKey;
        break;
      }
    }

    if(matchingEntry != null) {
      keys.remove(matchingEntry);
    }
  }

  /**
   * Returns the total number of owners.
   * @return Total number of owners.
   */
  public int size() {
    return keys.size();
  }
}
