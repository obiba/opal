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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.annotations.CollectionOfElements;

/**
 * This class maps collection center names to unique keys for a participant.
 */
@Entity
public final class Participant {

  @CollectionOfElements
  private final Map<String, String> keyMap = new HashMap<String, String>();

  public void addEntry(String collectionCenterName, String uniqueIdentifyingKey) {
    if(collectionCenterName == null) throw new IllegalArgumentException("The collectionCenterName must not be null.");
    if(uniqueIdentifyingKey == null) throw new IllegalArgumentException("The uniqueIdentifyingKey must not be null.");
    keyMap.put(collectionCenterName, uniqueIdentifyingKey);
  }

  /**
   * Returns the unique key the supplied collection center uses to identify this participant.
   * @param collectionCenterName Name of the collection center.
   * @return Unique key used to identify the participant.
   */
  public String getKey(String collectionCenterName) {
    return keyMap.get(collectionCenterName);
  }

  public boolean hasEntry(String collectionCenterName, String uniqueIdentifyingKey) {
    String retrievedKey = getKey(collectionCenterName);
    return retrievedKey == null ? false : retrievedKey.equals(uniqueIdentifyingKey);
  }

}
