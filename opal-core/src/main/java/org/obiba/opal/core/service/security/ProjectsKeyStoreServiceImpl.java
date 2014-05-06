/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
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
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.security.OpalKeyStore;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

@Component
public class ProjectsKeyStoreServiceImpl extends AbstractKeyStoreService implements ProjectsKeyStoreService {

  private static final String PROJECTS_KEY_STORE_PREFIX = "projects:";

  @NotNull
  @Override
  public OpalKeyStore getKeyStore(@NotNull Project project) {
    //noinspection ConstantConditions
    Preconditions.checkArgument(project != null);
    return getOrCreateKeyStore(getStoreName(project));
  }

  @Override
  public boolean aliasExists(@NotNull Project project, @NotNull String alias) {
    return aliasExists(getStoreName(project), alias);
  }

  @Override
  public void saveKeyStore(@NotNull OpalKeyStore keyStore) {
    super.saveKeyStore(keyStore);
  }

  @Override
  public void createOrUpdateKey(@NotNull Project project, @NotNull String alias, @NotNull String algorithm, int size,
      @NotNull String certificateInfo) {
    createOrUpdateKey(getStoreName(project), alias, algorithm, size, certificateInfo);
  }

  @Override
  public void deleteKeyStore(@NotNull Project project, @NotNull String alias) {
    deleteKey(getStoreName(project), alias);
  }

  @Override
  public void deleteKeyStore(@NotNull Project project) {
    for (String alias : getKeyStore(project).listAliases()) {
      deleteKeyStore(project, alias);
    }
  }

  @Override
  public void importKey(@NotNull Project project, @NotNull String alias, @NotNull FileObject privateKey,
      @NotNull FileObject certificate) {
    importKey(getStoreName(project), alias, privateKey, certificate);
  }

  @Override
  public void importKey(@NotNull Project project, @NotNull String alias, @NotNull InputStream privateKey,
      @NotNull InputStream certificate) {
    importKey(getStoreName(project), alias, privateKey, certificate);
  }

  @Override
  public void importKey(@NotNull Project project, @NotNull String alias, @NotNull FileObject privateKey,
      @NotNull String certificateInfo) {
    importKey(getStoreName(project), alias, privateKey, certificateInfo);
  }

  @Override
  public void importKey(@NotNull Project project, @NotNull String alias, @NotNull InputStream privateKey,
      @NotNull String certificateInfo) {
    importKey(getStoreName(project), alias, privateKey, certificateInfo);
  }

  @Override
  public void importCertificate(@NotNull Project project, @NotNull String alias, @NotNull InputStream inputStream) {
    importCertificate(getStoreName(project), alias, inputStream);
  }

  private String getStoreName(@NotNull Project project) {
    return PROJECTS_KEY_STORE_PREFIX + project.getName();
  }

  @Override
  public void stop() {

  }
}
