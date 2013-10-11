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

import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.UnitKeyStore;

/**
 * Manage KeyStores.
 */
public interface UnitKeyStoreService extends SystemService {

  /**
   * Gets the {@link UnitKeyStore} for the specified {@link FunctionalUnit}.
   *
   * @param unitName functional unit name
   * @return the unit's keystore (or <code>null</code> if no keystore exists for that unit)
   */
  @Nullable
  UnitKeyStore getUnitKeyStore(@Nonnull String unitName);

  /**
   * Gets the {@link UnitKeyStore} for the specified {@link FunctionalUnit} or if it doesn't exist, create, persist and
   * return a new {@link UnitKeyStore} for the unit.
   *
   * @param unitName functional unit name
   * @return the unit's keystore
   * @throws NoSuchFunctionalUnitException if no unit exists with the specified name
   */
  UnitKeyStore getOrCreateUnitKeyStore(@Nonnull String unitName) throws NoSuchFunctionalUnitException;

  /**
   * Save a {@link FunctionalUnit}'s keystore. This will persist any keystore updates.
   *
   * @param unitKeyStore functional unit keystore
   */
  void saveUnitKeyStore(@Nonnull UnitKeyStore unitKeyStore);

  /**
   * Creates a new key or updates an existing key. It is the responsibility of the client to ensure that the caller
   * actually wishes to override an existing key.
   *
   * @param unitName name of the functional unit
   * @param alias name of the key
   * @param algorithm key algorithm (e.g. RSA, DSA)
   * @param size of the key
   * @param certificateInfo Certificate attributes as a String (e.g. CN=Administrator, OU=Bioinformatics, O=GQ,
   * L=Montreal, ST=Quebec, C=CA)
   * @throws NoSuchFunctionalUnitException if no unit exists with the specified name
   */
  void createOrUpdateKey(@Nonnull String unitName, @Nonnull String alias, @Nonnull String algorithm, int size,
      @Nonnull String certificateInfo) throws NoSuchFunctionalUnitException;

  /**
   * Returns true if the supplied alias exists in the specified functional unit's keystore.
   *
   * @param unitName name of the functional unit
   * @param alias test if this alias exists.
   * @return true is the alias exists.
   * @throws NoSuchFunctionalUnitException if no unit exists with the specified name
   */
  boolean aliasExists(@Nonnull String unitName, @Nonnull String alias) throws NoSuchFunctionalUnitException;

  /**
   * Deletes the specified public/private key pair from the specified functional unit's keystore.
   *
   * @param unitName name of the functional unit
   * @param alias name of the public/private key pair to be deleted
   * @throws NoSuchFunctionalUnitException if no unit exists with the specified name
   */
  void deleteKey(@Nonnull String unitName, @Nonnull String alias) throws NoSuchFunctionalUnitException;

  /**
   * Import a private key and its associated certificate into the specified keystore at the given alias.
   *
   * @param unitName name of the functional unit
   * @param alias name of the key
   * @param privateKey private key in the PEM format
   * @param certificate certificate in the PEM format
   * @throws NoSuchFunctionalUnitException if no unit exists with the specified name
   */
  void importKey(@Nonnull String unitName, @Nonnull String alias, @Nonnull FileObject privateKey,
      @Nonnull FileObject certificate) throws NoSuchFunctionalUnitException;

  /**
   * Import a private key and its associated certificate into the specified keystore at the given alias.
   *
   * @param unitName
   * @param alias
   * @param privateKey
   * @param certificate
   * @throws NoSuchFunctionalUnitException
   */
  void importKey(@Nonnull String unitName, @Nonnull String alias, @Nonnull InputStream privateKey,
      @Nonnull InputStream certificate) throws NoSuchFunctionalUnitException;

  /**
   * Import a private key into the specified keystore and generate an associated certificate at the given alias.
   *
   * @param unitName name of the functional unit
   * @param alias name of the key
   * @param privateKey private key in the PEM format
   * @param certificateInfo Certificate attributes as a String (e.g. CN=Administrator, OU=Bioinformatics, O=GQ,
   * L=Montreal, ST=Quebec, C=CA)
   * @throws NoSuchFunctionalUnitException if no unit exists with the specified name
   */
  void importKey(@Nonnull String unitName, @Nonnull String alias, @Nonnull FileObject privateKey,
      @Nonnull String certificateInfo) throws NoSuchFunctionalUnitException;

  /**
   * Import a private key into the specified keystore and generate an associated certificate at the given alias.
   *
   * @param unitName
   * @param alias
   * @param privateKey
   * @param certificateInfo
   * @throws NoSuchFunctionalUnitException
   */
  void importKey(@Nonnull String unitName, @Nonnull String alias, @Nonnull InputStream privateKey,
      @Nonnull String certificateInfo) throws NoSuchFunctionalUnitException;

  /**
   * @param unit
   * @param alias
   * @param byteArrayInputStream
   */
  void importCertificate(@Nonnull String unit, @Nonnull String alias, @Nonnull InputStream byteArrayInputStream);

}
