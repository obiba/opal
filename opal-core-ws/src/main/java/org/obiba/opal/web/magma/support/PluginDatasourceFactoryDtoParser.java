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

import com.google.common.base.Strings;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.json.JSONObject;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.spi.datasource.DatasourceService;
import org.obiba.opal.spi.datasource.DatasourceUsage;
import org.obiba.opal.spi.r.ROperationTemplate;
import org.obiba.opal.spi.r.datasource.RDatasourceService;
import org.obiba.opal.spi.r.datasource.RSessionHandler;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Plugin datasource is used as a transient datasource.
 */
@Component
public class PluginDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  private static final Logger log = LoggerFactory.getLogger(PluginDatasourceFactoryDtoParser.class);

  private final OpalRuntime opalRuntime;

  private final OpalFileSystemService opalFileSystemService;

  private final OpalRSessionManager opalRSessionManager;

  @Autowired
  public PluginDatasourceFactoryDtoParser(OpalRuntime opalRuntime, OpalFileSystemService opalFileSystemService, OpalRSessionManager opalRSessionManager) {
    this.opalRuntime = opalRuntime;
    this.opalFileSystemService = opalFileSystemService;
    this.opalRSessionManager = opalRSessionManager;
  }

  @NotNull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto, DatasourceEncryptionStrategy encryptionStrategy) {
    Magma.PluginDatasourceFactoryDto pluginDto = dto.getExtension(Magma.PluginDatasourceFactoryDto.params);
    DatasourceService datasourceService = (DatasourceService) opalRuntime.getServicePlugin(pluginDto.getName());
    JSONObject parameters = null;
    try {
      if (pluginDto.hasParameters() && !Strings.isNullOrEmpty(pluginDto.getParameters()))
        parameters = new JSONObject(pluginDto.getParameters());
    } catch (Exception e) {
      log.warn("Cannot parse datasource plugin {} parameters: {}", pluginDto.getName(), pluginDto.getParameters(), e);
    }

    datasourceService.setOpalFileSystemPathResolver(path -> {
      try {
        FileObject fileObject = opalFileSystemService.getFileSystem().getRoot().resolveFile(path);
        // check security
        if (!fileObject.exists() || !fileObject.isReadable()) {
          throw new IllegalArgumentException("File cannot be read: " + path);
        }
        return opalFileSystemService.getFileSystem().getLocalFile(fileObject);
      } catch (FileSystemException e) {
        throw new IllegalArgumentException("Failed resolving file path: " + path);
      }
    });

    if (datasourceService instanceof RDatasourceService) {
      final RServerSession rSession = opalRSessionManager.newSubjectRSession();
      rSession.setExecutionContext("Import");
      ((RDatasourceService) datasourceService).setRSessionHandler(new RSessionHandler() {
        @Override
        public ROperationTemplate getSession() {
          return rSession;
        }

        @Override
        public void onDispose() {
          opalRSessionManager.removeRSession(rSession.getId());
        }
      });
    }

    return datasourceService.createDatasourceFactory(DatasourceUsage.IMPORT, parameters == null ? new JSONObject() : parameters);
  }

  /**
   * Verifies the dto type and whether an associated {@link DatasourceService} plugin can be found from the specified name.
   *
   * @param dto
   * @return
   */
  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    if (!dto.hasExtension(Magma.PluginDatasourceFactoryDto.params)) return false;
    Magma.PluginDatasourceFactoryDto pluginDto = dto.getExtension(Magma.PluginDatasourceFactoryDto.params);
    return opalRuntime.hasServicePlugin(pluginDto.getName()) && opalRuntime.getServicePlugin(pluginDto.getName()) instanceof DatasourceService;
  }

}
