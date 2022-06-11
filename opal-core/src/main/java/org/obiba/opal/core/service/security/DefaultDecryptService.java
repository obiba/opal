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

import com.google.common.collect.Iterables;
import org.apache.commons.vfs2.FileObject;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.crypt.EncryptedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.NoSuchIdentifiersMappingException;
import org.obiba.opal.core.service.NoSuchProjectException;
import org.obiba.opal.core.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * Default implementation of {@link DecryptService}.
 */
@Transactional
@Component
public class DefaultDecryptService implements DecryptService {

  @Autowired
  private OpalFileSystemService opalFileSystemService;

  @Autowired
  private ProjectsKeyStoreService projectsKeyStoreService;

  @Autowired
  private SystemKeyStoreService systemKeyStoreService;

  @Autowired
  private ProjectService projectService;

  @Override
  public void decryptData(String projectName, String datasourceName, FileObject file)
      throws NoSuchIdentifiersMappingException, NoSuchProjectException, IllegalArgumentException, IOException {
    // Validate the datasource name.
    Datasource destinationDatasource = MagmaEngine.get().getDatasource(datasourceName);

    // Create an FsDatasource for the specified file.
    Datasource sourceDatasource = new FsDatasource(file.getName().getBaseName(),
        opalFileSystemService.getFileSystem().getLocalFile(file),
        projectName != null ? getProjectEncryptionStrategy(projectName) : getSystemEncryptionStrategy());
    try {
      MagmaEngine.get().addDatasource(sourceDatasource);
      copyValueTables(sourceDatasource, destinationDatasource);
    } finally {
      MagmaEngine.get().removeDatasource(sourceDatasource);
    }
  }

  @Override
  public void decryptData(String datasourceName, FileObject file)
      throws NoSuchDatasourceException, IllegalArgumentException, IOException {
    decryptData(null, datasourceName, file);
  }

  private DatasourceEncryptionStrategy getProjectEncryptionStrategy(String projectName) {
    Project project = projectService.getProject(projectName);
    OpalKeyStore keyStore = projectsKeyStoreService.getKeyStore(project);

    DatasourceEncryptionStrategy encryptionStrategy = null;

    if(!Iterables.isEmpty(keyStore.listKeyPairs())) {
      encryptionStrategy = getDefaultEncryptionStrategy();
      encryptionStrategy.setKeyProvider(keyStore);
    }
    return encryptionStrategy;
  }

  private DatasourceEncryptionStrategy getSystemEncryptionStrategy() {
    DatasourceEncryptionStrategy dsEncryptionStrategy = getDefaultEncryptionStrategy();
    dsEncryptionStrategy.setKeyProvider(systemKeyStoreService.getKeyStore());
    return dsEncryptionStrategy;
  }

  private DatasourceEncryptionStrategy getDefaultEncryptionStrategy() {
    return new EncryptedSecretKeyDatasourceEncryptionStrategy();
  }

  private void copyValueTables(Datasource source, Datasource destination) throws IOException {
    DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().build();
    copier.copy(source, destination);
  }

}
