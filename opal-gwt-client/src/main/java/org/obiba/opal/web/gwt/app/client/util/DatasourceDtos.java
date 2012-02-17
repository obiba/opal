/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.util;

import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.model.client.magma.CsvDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.CsvDatasourceTableBundleDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.FsDatasourceFactoryDto;

import com.google.gwt.core.client.JsArray;

/**
 *
 */
public class DatasourceDtos {

  public static DatasourceFactoryDto createDatasourceFactoryDto(ImportData importData) {
    if(importData.getImportFormat().equals(ImportFormat.CSV)) {
      return createCSVDatasourceFactoryDto(importData);
    } else if(importData.getImportFormat().equals(ImportFormat.XML)) {
      return createXMLDatasourceFactoryDto(importData);
    } else
      throw new IllegalArgumentException("Import data format not supported: " + importData.getImportFormat());
  }

  private static DatasourceFactoryDto createCSVDatasourceFactoryDto(ImportData importData) {

    CsvDatasourceTableBundleDto bundleDto = CsvDatasourceTableBundleDto.create();
    bundleDto.setName(importData.getDestinationTableName());
    bundleDto.setData(importData.getCsvFile());
    bundleDto.setEntityType(importData.getEntityType());
    if(importData.getDestinationDatasourceName() != null) {
      bundleDto.setRefTable(importData.getDestinationDatasourceName() + "." + importData.getDestinationTableName());
    }

    @SuppressWarnings("unchecked")
    JsArray<CsvDatasourceTableBundleDto> tables = (JsArray<CsvDatasourceTableBundleDto>) JsArray.createArray();
    tables.push(bundleDto);

    CsvDatasourceFactoryDto factoryDto = CsvDatasourceFactoryDto.create();
    factoryDto.setCharacterSet(importData.getCharacterSet());
    factoryDto.setFirstRow(importData.getRow());
    factoryDto.setQuote(importData.getQuote());
    factoryDto.setSeparator(importData.getField());
    factoryDto.setTablesArray(tables);

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(CsvDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);

    return dto;
  }

  private static DatasourceFactoryDto createXMLDatasourceFactoryDto(ImportData importData) {

    FsDatasourceFactoryDto factoryDto = FsDatasourceFactoryDto.create();
    factoryDto.setFile(importData.getXmlFile());
    factoryDto.setUnit(importData.getUnit());

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(FsDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);

    return dto;
  }
}
