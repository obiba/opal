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

import java.io.File;

import javax.validation.constraints.NotNull;

import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.csv.support.CsvDatasourceFactory;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.web.model.Magma.CsvDatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.CsvDatasourceTableBundleDto;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class CsvDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  @NotNull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto, DatasourceEncryptionStrategy encryptionStrategy) {
    CsvDatasourceFactory factory = new CsvDatasourceFactory();
    CsvDatasourceFactoryDto csvDto = dto.getExtension(CsvDatasourceFactoryDto.params);
    if(csvDto.hasBundle()) factory.setBundle(resolveLocalFile(csvDto.getBundle()));
    if(csvDto.hasSeparator()) factory.setSeparator(csvDto.getSeparator());
    if(csvDto.hasQuote()) factory.setQuote(csvDto.getQuote());
    if(csvDto.hasFirstRow()) factory.setFirstRow(csvDto.getFirstRow());
    if(csvDto.hasCharacterSet()) factory.setCharacterSet(csvDto.getCharacterSet());
    if(csvDto.hasMultilines()) factory.setMultilines(csvDto.getMultilines());
    if(csvDto.hasDefaultValueType()) factory.setDefaultValueType(csvDto.getDefaultValueType());
    factory.setName(dto.getName());
    addTableBundles(factory, csvDto);
    return factory;
  }

  private void addTableBundles(CsvDatasourceFactory factory, CsvDatasourceFactoryDto csvDto) {
    for(CsvDatasourceTableBundleDto tableBundleDto : csvDto.getTablesList()) {
      File data = tableBundleDto.hasData() ? resolveLocalFile(tableBundleDto.getData()) : null;
      ValueTable refTable = null;
      if(tableBundleDto.hasRefTable()) {
        try {
          refTable = MagmaEngineTableResolver.valueOf(tableBundleDto.getRefTable()).resolveTable();
        } catch(NoSuchValueTableException e) {
          // ref table does not exists, let data dictionary creates it self either from the provided variables
          // or from the csv data file header
        }
      }
      if(refTable != null) {
        factory.addTable(refTable, data);
      } else {
        File variables = tableBundleDto.hasVariables() ? resolveLocalFile(tableBundleDto.getVariables()) :
            (data == null ? null : new File(data.getParentFile(), "variables.csv"));
        if (variables != null && variables.exists()) {
          factory.addTable(tableBundleDto.getName(), variables, data);
        } else {
          factory.addTable(tableBundleDto.getName(), data, tableBundleDto.getEntityType());
        }
      }
    }
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(CsvDatasourceFactoryDto.params);
  }

}
