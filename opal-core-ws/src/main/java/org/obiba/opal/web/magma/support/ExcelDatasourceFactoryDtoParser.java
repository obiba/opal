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
import org.obiba.magma.datasource.excel.support.ExcelDatasourceFactory;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.ExcelDatasourceFactoryDto;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class ExcelDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto) {
    ExcelDatasourceFactory factory = new ExcelDatasourceFactory();
    ExcelDatasourceFactoryDto excelDto = dto.getExtension(ExcelDatasourceFactoryDto.params);
    factory.setFile(resolveLocalFile(excelDto.getFile()));
    factory.setReadOnly(excelDto.getReadOnly());
    return factory;
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(ExcelDatasourceFactoryDto.params);
  }

}
