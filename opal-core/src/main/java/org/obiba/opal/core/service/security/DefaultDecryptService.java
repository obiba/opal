/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.security;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.crypt.EncryptedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.FunctionalUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link DecryptService}.
 */
@Transactional
@Component
public class DefaultDecryptService implements DecryptService {

//  private static final Logger log = LoggerFactory.getLogger(DefaultDecryptService.class);

  @Autowired
  private FunctionalUnitService functionalUnitService;

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private ProjectsKeyStoreService projectsKeyStoreService;

  @Autowired
  private SystemKeyStoreService systemKeyStoreService;

  @Autowired
  private ProjectService projectService;

  @Override
  public void decryptData(String unitName, String datasourceName, FileObject file)
      throws NoSuchFunctionalUnitException, NoSuchDatasourceException, IllegalArgumentException, IOException {
    Assert.notNull(file, "file is null");
    Assert.isTrue(file.getType() == FileType.FILE, "No such file (" + file.getName().getPath() + ")");

    // Validate the datasource name.
    Datasource destinationDatasource = MagmaEngine.get().getDatasource(datasourceName);

    FunctionalUnit unit = functionalUnitService.getFunctionalUnit(unitName);
    if(!FunctionalUnit.OPAL_INSTANCE.equals(unitName) && unit == null) {
      throw new NoSuchFunctionalUnitException(unitName);
    }
    // Create an FsDatasource for the specified file.
    Datasource sourceDatasource = new FsDatasource(file.getName().getBaseName(),
        opalRuntime.getFileSystem().getLocalFile(file),
        unit != null ? getDatasourceEncryptionStrategy(unit) : getOpalInstanceEncryptionStrategy());
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
    decryptData(FunctionalUnit.OPAL_INSTANCE, datasourceName, file);
  }

  private DatasourceEncryptionStrategy getDatasourceEncryptionStrategy(FunctionalUnit unit) {
    DatasourceEncryptionStrategy encryptionStrategy = unit.getDatasourceEncryptionStrategy();
    if(encryptionStrategy == null) {
      encryptionStrategy = new EncryptedSecretKeyDatasourceEncryptionStrategy();
      encryptionStrategy.setKeyProvider(projectsKeyStoreService.getKeyStore(projectService.getProject(unit.getName())));
      unit.setDatasourceEncryptionStrategy(encryptionStrategy);
    }
    return encryptionStrategy;
  }

  private DatasourceEncryptionStrategy getOpalInstanceEncryptionStrategy() {
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
