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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.vfs2.FileObject;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.OpalKeyStore;

/**
 * Manage KeyStores.
 */
public interface KeyStoreService extends SystemService {

//  @NotNull
//  OpalKeyStore getSystemKeyStore();
//
//  @NotNull
//  OpalKeyStore getCredentialsKeyStore();
//
//  @NotNull
//  OpalKeyStore getProjectsKeyStore(String projectName);

  /**
   * Gets the {@link org.obiba.opal.core.unit.OpalKeyStore} for the specified {@link FunctionalUnit}.
   *
   * @param unitName functional unit name
   * @return the unit's keystore (or <code>null</code> if no keystore exists for that unit)
   */
  @Nullable
  @Deprecated
  OpalKeyStore getUnitKeyStore(@NotNull String unitName);

  @Nullable
  @Deprecated
  OpalKeyStore getKeyStore(@NotNull String unitName, boolean create);

  @Nullable
  @Deprecated
  OpalKeyStore getKeyStore(@NotNull String unitName);

  /**
   * Gets the {@link org.obiba.opal.core.unit.OpalKeyStore} for the specified {@link FunctionalUnit} or if it doesn't exist, create, persist and
   * return a new {@link org.obiba.opal.core.unit.OpalKeyStore} for the unit.
   *
   * @param unitName functional unit name
   * @return the unit's keystore
   * @throws NoSuchFunctionalUnitException if no unit exists with the specified name
   */
  @Deprecated
  OpalKeyStore getOrCreateUnitKeyStore(@NotNull String unitName) throws NoSuchFunctionalUnitException;

  /**
   * Save a {@link FunctionalUnit}'s keystore. This will persist any keystore updates.
   *
   * @param opalKeyStore functional unit keystore
   */
  void saveUnitKeyStore(@NotNull OpalKeyStore opalKeyStore);

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
  void createOrUpdateKey(@NotNull String unitName, @NotNull String alias, @NotNull String algorithm, int size,
      @NotNull String certificateInfo) throws NoSuchFunctionalUnitException;

  /**
   * Returns true if the supplied alias exists in the specified functional unit's keystore.
   *
   * @param unitName name of the functional unit
   * @param alias test if this alias exists.
   * @return true is the alias exists.
   * @throws NoSuchFunctionalUnitException if no unit exists with the specified name
   */
  boolean aliasExists(@NotNull String unitName, @NotNull String alias) throws NoSuchFunctionalUnitException;

  /**
   * Deletes the specified public/private key pair from the specified functional unit's keystore.
   *
   * @param unitName name of the functional unit
   * @param alias name of the public/private key pair to be deleted
   * @throws NoSuchFunctionalUnitException if no unit exists with the specified name
   */
  void deleteKey(@NotNull String unitName, @NotNull String alias) throws NoSuchFunctionalUnitException;

  /**
   * Import a private key and its associated certificate into the specified keystore at the given alias.
   *
   * @param unitName name of the functional unit
   * @param alias name of the key
   * @param privateKey private key in the PEM format
   * @param certificate certificate in the PEM format
   * @throws NoSuchFunctionalUnitException if no unit exists with the specified name
   */
  void importKey(@NotNull String unitName, @NotNull String alias, @NotNull FileObject privateKey,
      @NotNull FileObject certificate) throws NoSuchFunctionalUnitException;

  /**
   * Import a private key and its associated certificate into the specified keystore at the given alias.
   *
   * @param unitName
   * @param alias
   * @param privateKey
   * @param certificate
   * @throws NoSuchFunctionalUnitException
   */
  void importKey(@NotNull String unitName, @NotNull String alias, @NotNull InputStream privateKey,
      @NotNull InputStream certificate) throws NoSuchFunctionalUnitException;

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
  void importKey(@NotNull String unitName, @NotNull String alias, @NotNull FileObject privateKey,
      @NotNull String certificateInfo) throws NoSuchFunctionalUnitException;

  /**
   * Import a private key into the specified keystore and generate an associated certificate at the given alias.
   *
   * @param unitName
   * @param alias
   * @param privateKey
   * @param certificateInfo
   * @throws NoSuchFunctionalUnitException
   */
  void importKey(@NotNull String unitName, @NotNull String alias, @NotNull InputStream privateKey,
      @NotNull String certificateInfo) throws NoSuchFunctionalUnitException;

  /**
   * @param unit
   * @param alias
   * @param byteArrayInputStream
   */
  void importCertificate(@NotNull String unit, @NotNull String alias, @NotNull InputStream byteArrayInputStream);

}
