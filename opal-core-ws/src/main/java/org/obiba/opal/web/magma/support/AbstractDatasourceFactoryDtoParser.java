/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma.support;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.support.BatchDatasourceFactory;
import org.obiba.magma.support.IncrementalDatasourceFactory;
import org.obiba.magma.support.Initialisables;
import org.obiba.opal.core.identifiers.IdentifierGenerator;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.constraints.NotNull;
import java.io.File;

public abstract class AbstractDatasourceFactoryDtoParser implements DatasourceFactoryDtoParser {

  @Autowired
  private OpalFileSystemService opalFileSystemService;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private IdentifierGenerator identifierGenerator;

  @Override
  public DatasourceFactory parse(DatasourceFactoryDto dto, DatasourceEncryptionStrategy encryptionStrategy) {
    DatasourceFactory factory = internalParse(dto, encryptionStrategy);
    // apply wrappers
    factory = applyIncremental(dto, factory);
    factory = applyBatch(dto, factory);

    Initialisables.initialise(factory);
    return factory;
  }

  private DatasourceFactory applyIncremental(DatasourceFactoryDto dto, DatasourceFactory factory) {
    if (!dto.hasIncrementalConfig()) return factory;
    Magma.DatasourceIncrementalConfigDto incrementalConfig = dto.getIncrementalConfig();
    if (incrementalConfig.getIncremental() && incrementalConfig.hasIncrementalDestinationName()) {
      return new IncrementalDatasourceFactory(factory, incrementalConfig.getIncrementalDestinationName());
    }
    return factory;
  }

  private DatasourceFactory applyBatch(DatasourceFactoryDto dto, DatasourceFactory factory) {
    if (!dto.hasBatchConfig()) return factory;
    return new BatchDatasourceFactory(factory, dto.getBatchConfig().getLimit());
  }

  @NotNull
  protected abstract DatasourceFactory internalParse(DatasourceFactoryDto dto, DatasourceEncryptionStrategy encryptionStrategy);

  @SuppressWarnings("UnusedDeclaration")
  protected OpalFileSystemService getOpalFileSystemService() {
    return opalFileSystemService;
  }

  protected FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return opalFileSystemService.getFileSystem().getRoot().resolveFile(path);
  }

  /**
   * The native file of a path of the Opal file system. A file that does not exist yet is handed out, the datasource
   * will report on it; an existing one must be readable by the current subject, whatever the datasource permission
   * that led here.
   */
  protected File resolveLocalFile(String path) {
    try {
      FileObject file = resolveFileInFileSystem(path);
      if (file.exists() && !file.isReadable()) {
        throw new IllegalArgumentException("File cannot be read: " + path);
      }
      return opalFileSystemService.getFileSystem().getLocalFile(file);
    } catch (FileSystemException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
