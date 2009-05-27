/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.impl;

import java.util.Collection;
import java.util.Collections;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.opal.core.domain.participant.Participant;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.service.IParticipantKeyReadRegistry;
import org.obiba.opal.core.service.IParticipantKeyWriteRegistry;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class DefaultParticipantKeyRegistryImpl extends PersistenceManagerAwareService implements IParticipantKeyReadRegistry, IParticipantKeyWriteRegistry {

  private IParticipantIdentifier participantIndentifier;

  public void setParticipantIndentifier(IParticipantIdentifier participantIndentifier) {
    this.participantIndentifier = participantIndentifier;
  }

  /**
   * Returns a {@link Participant} that matches the owner/key pair, or null if one does not exist. This is available to
   * service level code which must be taken when manipulating this object as all changes will be persisted.
   * @param owner The {@code Participant} keyMap must have this value associated with the key.
   * @param key The {@code Participant} keyMap must have this value associated with the owner.
   * @return A matching {@code Participant}, or null.
   */
  protected abstract Participant getParticipant(String owner, String key);

  @Transactional(propagation = Propagation.SUPPORTS)
  public boolean hasParticipant(String owner, String key) {
    if(owner == null) throw new IllegalArgumentException("The owner must not be null.");
    if(key == null) throw new IllegalArgumentException("The key must not be null.");
    if(getParticipant(owner, key) == null) {
      return false;
    } else {
      return true;
    }
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public Collection<String> getEntry(String refOwner, String refKey, String owner) {
    if(refOwner == null) throw new IllegalArgumentException("The refOwner must not be null.");
    if(refKey == null) throw new IllegalArgumentException("The refKey must not be null.");
    if(owner == null) throw new IllegalArgumentException("The owner must not be null.");
    Participant participant = getParticipant(refOwner, refKey);
    if(participant != null) {
      return participant.getKeys(owner);
    } else {
      return Collections.emptySet();
    }

  }

  public void registerEntry(String refOwner, String refKey, String owner, String key) {
    if(refOwner == null) throw new IllegalArgumentException("The refOwner must not be null.");
    if(refKey == null) throw new IllegalArgumentException("The refKey must not be null.");
    if(owner == null) throw new IllegalArgumentException("The owner must not be null.");
    if(key == null) throw new IllegalArgumentException("The key must not be null.");
    // TODO The following constraints need to be added to the database.
    if(hasParticipant(owner, key)) throw new IllegalStateException("Cannot register non unique owner/key pair [" + owner + "]=[" + key + "].");

    Participant participant = getParticipant(refOwner, refKey);
    if(participant == null) {
      participant = new Participant();
      participant.addEntry(refOwner, refKey);
    }
    participant.addEntry(owner, key);
    getPersistenceManager().save(participant);
  }

  public void unregisterEntry(String owner, String key) {
    if(owner == null) throw new IllegalArgumentException("The owner must not be null.");
    if(key == null) throw new IllegalArgumentException("The key must not be null.");
    Participant participant = getParticipant(owner, key);
    if(participant == null) return;
    participant.removeEntry(owner, key);
    if(participant.size() == 0) {
      getPersistenceManager().delete(participant);
    }

  }

  public void unregisterParticipant(String owner, String key) {
    if(owner == null) throw new IllegalArgumentException("The owner must not be null.");
    if(key == null) throw new IllegalArgumentException("The key must not be null.");
    Participant participant = getParticipant(owner, key);
    if(participant == null) return;
    getPersistenceManager().delete(participant);
  }

  public String generateUniqueKey(String owner) {
    for(int i = 0; i < 100; i++) {
      String uniqueId = participantIndentifier.generateParticipantIdentifier();
      if(!hasParticipant(owner, uniqueId)) {
        return uniqueId;
      }
    }
    throw new IllegalStateException("Unable to generate a unique id for the owner [" + owner + "]. One hundred attempts made.");
  }

}
