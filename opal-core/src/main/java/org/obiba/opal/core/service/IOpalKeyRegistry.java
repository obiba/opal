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
 * A facade that presents {@link IParticipantKeyReadRegistry} and {@link IParticipantKeyWriteRegistry} as a single
 * interface for Opal keys only.
 */
public interface IOpalKeyRegistry {

  /**
   * Returns the Opal key for the specified onwer and onwerKey.
   * 
   * @param onwer
   * @param ownerKey
   * @return
   */
  public String findOpalKey(String owner, String ownerKey);

  /**
   * Returns true if
   * 
   * @param owner
   * @param ownerKey
   * @return
   */
  public boolean hasOpalKey(String owner, String ownerKey);

  /**
   * Generates a new Opal key and returns it
   * 
   * @return
   */
  public String registerNewOpalKey();

  /**
   * Generates a new Opal key and returns it, this method will also register the given owner key with the generated opal
   * key
   * 
   * @return
   */
  public String registerNewOpalKey(String owner, String ownerKey);

  /**
   * Registers a new key for the specified opalKey
   * 
   * @param opalKey
   * @param owner
   * @param ownerKey
   */
  public void registerKey(String opalKey, String owner, String ownerKey);

}
