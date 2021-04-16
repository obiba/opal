/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security;

import java.io.InputStream;

import javax.validation.constraints.NotNull;

import org.apache.commons.vfs2.FileObject;
import org.obiba.opal.core.security.OpalKeyStore;

public interface SystemKeyStoreService extends KeyStoreService {

  @NotNull
  OpalKeyStore getKeyStore();

  boolean aliasExists(@NotNull String alias);

  void createOrUpdateKey(@NotNull String alias, @NotNull String algorithm, int size, @NotNull String certificateInfo);

  /**
   * Deletes the specified public/private key pair
   */
  void deleteKeyStore(@NotNull String alias);

  /**
   * Import a private key and its associated certificate into the specified keystore at the given alias.
   *
   * @param alias name of the key
   * @param privateKey private key in the PEM format
   * @param certificate certificate in the PEM format
   */
  void importKey(@NotNull String alias, @NotNull FileObject privateKey, @NotNull FileObject certificate);

  /**
   * Import a private key and its associated certificate into the specified keystore at the given alias.
   *
   * @param alias
   * @param privateKey
   * @param certificate
   */
  void importKey(@NotNull String alias, @NotNull InputStream privateKey, @NotNull InputStream certificate);

  /**
   * Import a private key into the specified keystore and generate an associated certificate at the given alias.
   *
   * @param alias name of the key
   * @param privateKey private key in the PEM format
   * @param certificateInfo Certificate attributes as a String (e.g. CN=Administrator, OU=Bioinformatics, O=GQ,
   * L=Montreal, ST=Quebec, C=CA)
   */
  void importKey(@NotNull String alias, @NotNull FileObject privateKey, @NotNull String certificateInfo);

  /**
   * Import a private key into the specified keystore and generate an associated certificate at the given alias.
   */
  void importKey(@NotNull String alias, @NotNull InputStream privateKey, @NotNull String certificateInfo);

  void importCertificate(@NotNull String alias, @NotNull InputStream inputStream);

}
