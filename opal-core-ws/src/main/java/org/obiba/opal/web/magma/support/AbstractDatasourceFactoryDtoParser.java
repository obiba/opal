/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.support;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.support.Initialisables;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.unit.FunctionalUnitService;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDatasourceFactoryDtoParser implements DatasourceFactoryDtoParser {

  @Autowired
  private FunctionalUnitService functionalUnitService;

  @Autowired
  private OpalRuntime opalRuntime;

  @Override
  public DatasourceFactory parse(DatasourceFactoryDto dto) {
    DatasourceFactory factory = internalParse(dto);
    if(factory != null) {
      Initialisables.initialise(factory);
    }
    return factory;
  }

  protected abstract DatasourceFactory internalParse(DatasourceFactoryDto dto);

  protected OpalRuntime getOpalRuntime() {
    return opalRuntime;
  }

  protected FunctionalUnitService getFunctionalUnitService() {
    return functionalUnitService;
  }

  protected FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return opalRuntime.getFileSystem().getRoot().resolveFile(path);
  }

  protected File resolveLocalFile(String path) {
    try {
      // note: does not ensure that file exists
      return opalRuntime.getFileSystem().getLocalFile(resolveFileInFileSystem(path));
    } catch(FileSystemException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
