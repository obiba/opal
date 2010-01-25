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

import org.obiba.opal.core.service.IOpalKeyRegistry;
import org.obiba.opal.core.service.IParticipantKeyReadRegistry;
import org.obiba.opal.core.service.IParticipantKeyWriteRegistry;

/**
 *
 */
public class DefaultOpalKeyRegistry implements IOpalKeyRegistry {

  /** Name used by Opal to store entries in the Participant Key database. */
  public static final String OPAL_KEY_NAME = "opal";

  private IParticipantKeyWriteRegistry keyWriter;

  private IParticipantKeyReadRegistry keyReader;

  public void setKeyReader(IParticipantKeyReadRegistry keyReader) {
    this.keyReader = keyReader;
  }

  public void setKeyWriter(IParticipantKeyWriteRegistry keyWriter) {
    this.keyWriter = keyWriter;
  }

  public String findOpalKey(String owner, String ownerKey) {
    if(owner == null) throw new IllegalArgumentException("owner cannot be null");
    if(ownerKey == null) throw new IllegalArgumentException("ownerKey cannot be null");

    Collection<String> keys = keyReader.getEntry(owner, ownerKey, OPAL_KEY_NAME);
    if(keys == null || keys.size() == 0) {
      return null;
    }
    if(keys.size() > 1) {
      throw new IllegalStateException("Unexpected multiple opal keys for owner " + owner + " and key " + ownerKey);
    }
    return keys.iterator().next();
  }

  public boolean hasOpalKey(String owner, String ownerKey) {
    if(owner == null) throw new IllegalArgumentException("owner cannot be null");
    if(ownerKey == null) throw new IllegalArgumentException("ownerKey cannot be null");

    return keyReader.hasParticipant(owner, ownerKey);
  }

  public String findOwnerKey(String owner, String opalKey) {
    if(owner == null) throw new IllegalArgumentException("owner cannot be null");
    if(opalKey == null) throw new IllegalArgumentException("opalKey cannot be null");

    Collection<String> keys = keyReader.getEntry(OPAL_KEY_NAME, opalKey, owner);
    if(keys == null || keys.size() == 0) {
      return null;
    }
    if(keys.size() > 1) {
      throw new IllegalStateException("Unexpected multiple keys for owner " + owner + " and opal key " + opalKey);
    }
    return keys.iterator().next();
  }

  public void registerKey(String opalKey, String owner, String ownerKey) {
    if(owner == null) throw new IllegalArgumentException("owner cannot be null");
    if(ownerKey == null) throw new IllegalArgumentException("ownerKey cannot be null");

    keyWriter.registerEntry(OPAL_KEY_NAME, opalKey, owner, ownerKey);
  }

  public synchronized String registerNewOpalKey() {
    return keyWriter.generateUniqueKey(OPAL_KEY_NAME);
  }

  public String registerNewOpalKey(String owner, String ownerKey) {
    if(owner == null) throw new IllegalArgumentException("owner cannot be null");
    if(ownerKey == null) throw new IllegalArgumentException("ownerKey cannot be null");

    String opalKey = registerNewOpalKey();
    registerKey(opalKey, owner, ownerKey);
    return opalKey;
  }

}
