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

import java.io.File;

import org.obiba.opal.core.domain.unit.UnitKeyStore;
import org.obiba.opal.core.unit.FunctionalUnit;

/**
 * Manage KeyStores.
 */
public interface UnitKeyStoreService {

  public static final String OPAL_INSTANCE_KEYSTORE = "OpalInstance";

  /**
   * Gets the {@link UnitKeyStore} for the specified {@link FunctionalUnit}.
   * 
   * @param unitName functional unit name
   * @return the unit's keystore (or <code>null</code> if no keystore exists for that unit)
   */
  public UnitKeyStore getUnitKeyStore(String unitName);

  /**
   * Gets the {@link UnitKeyStore} for the specified {@link FunctionalUnit} or if it doesn't exist, create, persist and
   * return a new {@link UnitKeyStore} for the unit.
   * 
   * @param unitName functional unit name
   * @return the unit's keystore
   */
  public UnitKeyStore getOrCreateUnitKeyStore(String unitName);

  /**
   * Save a {@link FunctionalUnit}'s keystore. This will persist any keystore updates.
   * 
   * @param unitKeyStore functional unit keystore
   */
  public void saveUnitKeyStore(UnitKeyStore unitKeyStore);

  /**
   * Creates a new key or updates an existing key. It is the responsibility of the client to ensure that the caller
   * actually wishes to override an existing key.
   * @param unitName name of the functional unit
   * @param alias name of the key
   * @param algorithm key algorithm (e.g. RSA, DSA)
   * @param size of the key
   * @param certificateInfo Certificate attributes as a String (e.g. CN=Administrator, OU=Bioinformatics, O=GQ,
   * L=Montreal, ST=Quebec, C=CA)
   */
  public void createOrUpdateKey(String unitName, String alias, String algorithm, int size, String certificateInfo);

  /**
   * Returns true if the supplied alias exists in the specified functional unit's keystore.
   * @param unitName name of the functional unit
   * @param alias test if this alias exists.
   * @return true is the alias exists.
   */
  public boolean aliasExists(String unitName, String alias);

  /**
   * Deletes the specified public/private key pair from the specified functional unit's keystore.
   * @param unitName name of the functional unit
   * @param alias name of the public/private key pair to be deleted
   */
  public void deleteKey(String unitName, String alias);

  /**
   * Import a private key and its associated certificate into the specified keystore at the given alias.
   * @param unitName name of the functional unit
   * @param alias name of the key
   * @param privateKey private key in the PEM format
   * @param certificate certificate in the PEM format
   */
  public void importKey(String unitName, String alias, File privateKey, File certificate);

  /**
   * Import a private key into the specified keystore and generate an associated certificate at the given alias.
   * @param unitName name of the functional unit
   * @param alias name of the key
   * @param privateKey private key in the PEM format
   * @param certificateInfo Certificate attributes as a String (e.g. CN=Administrator, OU=Bioinformatics, O=GQ,
   * L=Montreal, ST=Quebec, C=CA)
   */
  public void importKey(String unitName, String alias, File privateKey, String certificateInfo);

}
