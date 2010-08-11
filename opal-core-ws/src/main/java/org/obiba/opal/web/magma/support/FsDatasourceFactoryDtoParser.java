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

import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.datasource.fs.support.FsDatasourceFactory;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class FsDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  @Autowired
  private OpalRuntime opalRuntime;

  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto) {
    FsDatasourceFactory factory = null;
    if(dto.hasFs()) {
      factory = new FsDatasourceFactory();
      factory.setFile(new File(dto.getFs().getFile()));
      if(dto.getFs().hasUnit()) {
        String unitName = dto.getFs().getUnit();
        FunctionalUnit unit = opalRuntime.getFunctionalUnit(unitName);
        if(unit == null) {
          throw new NoSuchFunctionalUnitException(unitName);
        }
        factory.setEncryptionStrategy(unit.getDatasourceEncryptionStrategy());
      }
    }
    return factory;
  }

  public void setOpalRuntime(OpalRuntime opalRuntime) {
    this.opalRuntime = opalRuntime;
  }

}
