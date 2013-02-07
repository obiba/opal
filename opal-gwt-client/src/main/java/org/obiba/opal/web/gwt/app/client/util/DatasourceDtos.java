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
import org.obiba.opal.web.model.client.magma.CsvDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.CsvDatasourceTableBundleDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.DatasourceIncrementalConfigDto;
import org.obiba.opal.web.model.client.magma.DatasourceUnitConfigDto;
import org.obiba.opal.web.model.client.magma.FsDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.LimesurveyDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.RestDatasourceFactoryDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 *
 */
public class DatasourceDtos {

  private DatasourceDtos() {
  }

  public static DatasourceFactoryDto createDatasourceFactoryDto(ImportData importData) {
    switch(importData.getImportFormat()) {
      case CSV:
        return createCSVDatasourceFactoryDto(importData);
      case XML:
        return createXMLDatasourceFactoryDto(importData);
      case LIMESURVEY:
        return createLimesurveyDatasourceFactoryDto(importData);
      case REST:
        return createRestDatasourceFactoryDto(importData);
      default:
        throw new IllegalArgumentException("Import data format not supported: " + importData.getImportFormat());
    }
  }

  private static DatasourceFactoryDto createLimesurveyDatasourceFactoryDto(ImportData importData) {
    LimesurveyDatasourceFactoryDto factoryDto = LimesurveyDatasourceFactoryDto.create();
    factoryDto.setDatabase(importData.getDatabase());
    factoryDto.setTablePrefix(importData.getTablePrefix());
    return createAndConfigureDatasourceFactoryDto(importData,
        LimesurveyDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
  }

  private static DatasourceFactoryDto createRestDatasourceFactoryDto(ImportData importData) {
    RestDatasourceFactoryDto factoryDto = RestDatasourceFactoryDto.create();
    factoryDto.setUrl(importData.getString("url"));
    factoryDto.setUsername(importData.getString("username"));
    factoryDto.setPassword(importData.getString("password"));
    factoryDto.setRemoteDatasource(importData.getString("remoteDatasource"));

    return createAndConfigureDatasourceFactoryDto(importData,
        RestDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
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

    return createAndConfigureDatasourceFactoryDto(importData,
        CsvDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
  }

  private static DatasourceFactoryDto createXMLDatasourceFactoryDto(ImportData importData) {

    FsDatasourceFactoryDto factoryDto = FsDatasourceFactoryDto.create();
    factoryDto.setFile(importData.getXmlFile());
    factoryDto.setOnyxWrapper(true);
    return createAndConfigureDatasourceFactoryDto(importData,
        FsDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
  }

  private static DatasourceFactoryDto createAndConfigureDatasourceFactoryDto(ImportData importData,
      String extensionName, JavaScriptObject extension) {
    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    configureUnit(importData, dto);
    configureIncremental(importData, dto);
    dto.setExtension(extensionName, extension);
    return dto;
  }

  private static void configureIncremental(ImportData importData, DatasourceFactoryDto dto) {
    if(importData.isIncremental()) {
      DatasourceIncrementalConfigDto configDto = DatasourceIncrementalConfigDto.create();
      configDto.setIncremental(true);
      configDto.setIncrementalDestinationName(importData.getDestinationDatasourceName());
      dto.setIncrementalConfig(configDto);
    }
  }

  private static void configureUnit(ImportData importData, DatasourceFactoryDto dto) {
    if(!Strings.isNullOrEmpty(importData.getUnit())) {
      DatasourceUnitConfigDto configDto = DatasourceUnitConfigDto.create();
      configDto.setUnit(importData.getUnit());
      configDto.setAllowIdentifierGeneration(importData.isAllowIdentifierGeneration());
      configDto.setIgnoreUnknownIdentifier(importData.isIgnoreUnknownIdentifier());
      dto.setUnitConfig(configDto);
    }
  }
}
