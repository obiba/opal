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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.CollectionOfElements;

@Entity
public final class Participant {

  @SuppressWarnings("unused")
  @Id
  @GeneratedValue
  @Column
  private long id;

  @CollectionOfElements
  @OneToMany(cascade = { CascadeType.ALL })
  private final Map<String, ParticipantKeys> keyMap = new HashMap<String, ParticipantKeys>();

  public Participant() {
    super();
  }

  public void addEntry(String keyOwner, String uniqueIdentifyingKey) {
    if(keyOwner == null) throw new IllegalArgumentException("The keyOwner must not be null.");
    if(uniqueIdentifyingKey == null) throw new IllegalArgumentException("The uniqueIdentifyingKey must not be null.");
    if(hasEntry(keyOwner, uniqueIdentifyingKey)) throw new IllegalStateException("The key/value pair [" + keyOwner + "]=[" + uniqueIdentifyingKey + "] already exists. Duplicates are not permitted.");
    ParticipantKeys keys = keyMap.get(keyOwner);
    if(keys == null) {
      keys = new ParticipantKeys();
    }
    keys.addKey(uniqueIdentifyingKey);
    keyMap.put(keyOwner, keys);
  }

  /**
   * Returns the unique key the supplied collection center uses to identify this participant.
   * @param keyOwner Name of the collection center.
   * @return Unique key used to identify the participant.
   */
  public Collection<String> getKey(String keyOwner) {
    ParticipantKeys keys = keyMap.get(keyOwner);
    if(keys == null) {
      return Collections.emptySet();
    } else {
      return Collections.unmodifiableCollection(keys.getKeys());
    }
  }

  public boolean hasEntry(String keyOwner, String uniqueIdentifyingKey) {
    ParticipantKeys keys = keyMap.get(keyOwner);
    if(keys == null) return false;
    if(keys.contains(uniqueIdentifyingKey)) {
      return true;
    } else {
      return false;
    }
  }

  public void removeEntry(String keyOwner, String uniqueIdentifyingKey) {
    ParticipantKeys keys = keyMap.get(keyOwner);
    if(keys == null) return;
    if(keys.contains(uniqueIdentifyingKey)) {
      keys.remove(uniqueIdentifyingKey);
      if(keys.size() == 0) {
        keyMap.remove(keyOwner);
      }
    }
  }

  public int size() {
    return keyMap.size();
  }
}
