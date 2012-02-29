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

import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.fs.support.FsDatasourceFactory;
import org.obiba.magma.support.MultiplexingDatasource.VariableAttributeMultiplexer;
import org.obiba.magma.support.MultiplexingDatasource.VariableNameTransformer;
import org.obiba.magma.support.MultiplexingDatasourceFactory;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.FsDatasourceFactoryDto;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class FsDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto) {
    FsDatasourceFactory fsFactory = new FsDatasourceFactory();
    FsDatasourceFactoryDto fsDto = dto.getExtension(FsDatasourceFactoryDto.params);
    fsFactory.setFile(resolveLocalFile(fsDto.getFile()));
    if(fsDto.hasUnit()) {
      String unitName = fsDto.getUnit();
      FunctionalUnit unit = getFunctionalUnitService().getFunctionalUnit(unitName);
      if(unit == null) {
        throw new NoSuchFunctionalUnitException(unitName);
      }
      fsFactory.setEncryptionStrategy(unit.getDatasourceEncryptionStrategy());
    }

    DatasourceFactory factory = fsFactory;
    if(fsDto.hasOldOnyx() && fsDto.getOldOnyx()) {

      factory = new MultiplexingDatasourceFactory(fsFactory, new VariableAttributeMultiplexer("stage"), new VariableNameTransformer() {

        @Override
        protected String transformName(Variable variable) {
          if(variable.hasAttribute("stage")) {
            return variable.getName().replaceFirst("^.*\\.?" + variable.getAttributeStringValue("stage") + "\\.", "");
          }
          return variable.getName();
        }

      });
    }

    factory.setName(dto.getName());

    return factory;
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(FsDatasourceFactoryDto.params);
  }
}
