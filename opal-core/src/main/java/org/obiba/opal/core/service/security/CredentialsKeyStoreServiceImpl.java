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
import org.springframework.stereotype.Component;

@Component("credentialsKeyStoreService")
public class CredentialsKeyStoreServiceImpl extends AbstractKeyStoreService implements CredentialsKeyStoreService {

  private static final String CREDENTIALS_KEY_STORE = "credentials";

  @NotNull
  @Override
  public OpalKeyStore getKeyStore() {
    return getOrCreateKeyStore(CREDENTIALS_KEY_STORE);
  }

  @Override
  public boolean aliasExists(@NotNull String alias) {
    return aliasExists(CREDENTIALS_KEY_STORE, alias);
  }

  @Override
  public void saveKeyStore(@NotNull OpalKeyStore keyStore) {
    super.saveKeyStore(keyStore);
  }

  @Override
  public void createOrUpdateKey(@NotNull String alias, @NotNull String algorithm, int size,
      @NotNull String certificateInfo) {
    createOrUpdateKey(CREDENTIALS_KEY_STORE, alias, algorithm, size, certificateInfo);
  }

  @Override
  public void deleteKeyStore(@NotNull String alias) {
    deleteKey(CREDENTIALS_KEY_STORE, alias);
  }

  @Override
  public void importKey(@NotNull String alias, @NotNull FileObject privateKey, @NotNull FileObject certificate) {
    importKey(CREDENTIALS_KEY_STORE, alias, privateKey, certificate);
  }

  @Override
  public void importKey(@NotNull String alias, @NotNull InputStream privateKey, @NotNull InputStream certificate) {
    importKey(CREDENTIALS_KEY_STORE, alias, privateKey, certificate);
  }

  @Override
  public void importKey(@NotNull String alias, @NotNull FileObject privateKey, @NotNull String certificateInfo) {
    importKey(CREDENTIALS_KEY_STORE, alias, privateKey, certificateInfo);
  }

  @Override
  public void importKey(@NotNull String alias, @NotNull InputStream privateKey, @NotNull String certificateInfo) {
    importKey(CREDENTIALS_KEY_STORE, alias, privateKey, certificateInfo);
  }

  @Override
  public void importCertificate(@NotNull String alias, @NotNull InputStream inputStream) {
    importCertificate(CREDENTIALS_KEY_STORE, alias, inputStream);
  }

  @Override
  public void stop() {

  }

}
