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
import org.obiba.magma.datasource.csv.support.CsvDatasourceFactory;
import org.obiba.opal.web.model.Magma.CsvDatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.CsvDatasourceTableBundleDto;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;

/**
 *
 */
public class CsvDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto) {
    CsvDatasourceFactory factory = null;
    if(dto.hasCsv()) {
      factory = new CsvDatasourceFactory();
      CsvDatasourceFactoryDto csvDto = dto.getCsv();
      if(csvDto.hasBundle()) {
        factory.setBundle(new File(dto.getCsv().getBundle()));
      }
      if(csvDto.hasSeparator()) {
        factory.setSeparator(csvDto.getSeparator());
      }
      if(csvDto.hasQuote()) {
        factory.setQuote(csvDto.getQuote());
      }
      if(csvDto.hasFirstRow()) {
        factory.setFirstRow(csvDto.getFirstRow());
      }
      if(csvDto.hasCharacterSet()) {
        factory.setCharacterSet(csvDto.getCharacterSet());
      }
      addTableBundles(factory, csvDto);
    }
    return factory;
  }

  private void addTableBundles(CsvDatasourceFactory factory, CsvDatasourceFactoryDto csvDto) {
    for(CsvDatasourceTableBundleDto tableBundleDto : csvDto.getTablesList()) {
      File variables = tableBundleDto.hasVariables() ? new File(tableBundleDto.getVariables()) : null;
      File data = tableBundleDto.hasData() ? new File(tableBundleDto.getData()) : null;
      factory.addTable(tableBundleDto.getName(), variables, data);
    }
  }

}
