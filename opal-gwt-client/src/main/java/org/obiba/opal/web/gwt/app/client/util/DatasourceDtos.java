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

import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportConfig;
import org.obiba.opal.web.model.client.magma.CsvDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.CsvDatasourceTableBundleDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.DatasourceIncrementalConfigDto;
import org.obiba.opal.web.model.client.magma.DatasourceUnitConfigDto;
import org.obiba.opal.web.model.client.magma.FsDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.LimesurveyDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.RestDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.SpssDatasourceFactoryDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 *
 */
public class DatasourceDtos {

  private DatasourceDtos() {
  }

  public static DatasourceFactoryDto createDatasourceFactoryDto(ImportConfig importConfig) {
    switch(importConfig.getImportFormat()) {
      case CSV:
        return createCSVDatasourceFactoryDto(importConfig);
      case XML:
        return createXMLDatasourceFactoryDto(importConfig);
      case LIMESURVEY:
        return createLimesurveyDatasourceFactoryDto(importConfig);
      case REST:
        return createRestDatasourceFactoryDto(importConfig);
      case SPSS:
        return createSpssDatasourceFactoryDto(importConfig);
      default:
        throw new IllegalArgumentException("Import data format not supported: " + importConfig.getImportFormat());
    }
  }

  private static DatasourceFactoryDto createLimesurveyDatasourceFactoryDto(ImportConfig importConfig) {
    LimesurveyDatasourceFactoryDto factoryDto = LimesurveyDatasourceFactoryDto.create();
    factoryDto.setDatabase(importConfig.getDatabase());
    factoryDto.setTablePrefix(importConfig.getTablePrefix());
    return createAndConfigureDatasourceFactoryDto(importConfig,
        LimesurveyDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
  }

  private static DatasourceFactoryDto createRestDatasourceFactoryDto(ImportConfig importConfig) {
    RestDatasourceFactoryDto factoryDto = RestDatasourceFactoryDto.create();
    factoryDto.setUrl(importConfig.getString("url"));
    factoryDto.setUsername(importConfig.getString("username"));
    factoryDto.setPassword(importConfig.getString("password"));
    factoryDto.setRemoteDatasource(importConfig.getString("remoteDatasource"));

    return createAndConfigureDatasourceFactoryDto(importConfig,
        RestDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
  }

  private static DatasourceFactoryDto createCSVDatasourceFactoryDto(ImportConfig importConfig) {

    CsvDatasourceTableBundleDto bundleDto = CsvDatasourceTableBundleDto.create();
    bundleDto.setName(importConfig.getDestinationTableName());
    bundleDto.setData(importConfig.getCsvFile());
    bundleDto.setEntityType(importConfig.getEntityType());
    if(importConfig.getDestinationDatasourceName() != null) {
      bundleDto.setRefTable(importConfig.getDestinationDatasourceName() + "." + importConfig.getDestinationTableName());
    }

    @SuppressWarnings("unchecked")
    JsArray<CsvDatasourceTableBundleDto> tables = (JsArray<CsvDatasourceTableBundleDto>) JsArray.createArray();
    tables.push(bundleDto);

    CsvDatasourceFactoryDto factoryDto = CsvDatasourceFactoryDto.create();
    factoryDto.setCharacterSet(importConfig.getCharacterSet());
    factoryDto.setFirstRow(importConfig.getRow());
    factoryDto.setQuote(importConfig.getQuote());
    factoryDto.setSeparator(importConfig.getField());
    factoryDto.setTablesArray(tables);

    return createAndConfigureDatasourceFactoryDto(importConfig,
        CsvDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
  }

  private static DatasourceFactoryDto createXMLDatasourceFactoryDto(ImportConfig importConfig) {

    FsDatasourceFactoryDto factoryDto = FsDatasourceFactoryDto.create();
    factoryDto.setFile(importConfig.getXmlFile());
    factoryDto.setOnyxWrapper(true);
    return createAndConfigureDatasourceFactoryDto(importConfig,
        FsDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
  }

  private static DatasourceFactoryDto createAndConfigureDatasourceFactoryDto(ImportConfig importConfig,
      String extensionName, JavaScriptObject extension) {
    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    configureUnit(importConfig, dto);
    configureIncremental(importConfig, dto);
    dto.setExtension(extensionName, extension);
    return dto;
  }

  private static void configureIncremental(ImportConfig importConfig, DatasourceFactoryDto dto) {
    if(importConfig.isIncremental()) {
      DatasourceIncrementalConfigDto configDto = DatasourceIncrementalConfigDto.create();
      configDto.setIncremental(true);
      configDto.setIncrementalDestinationName(importConfig.getDestinationDatasourceName());
      dto.setIncrementalConfig(configDto);
    }
  }

  private static void configureUnit(ImportConfig importConfig, DatasourceFactoryDto dto) {
    if(!Strings.isNullOrEmpty(importConfig.getUnit())) {
      DatasourceUnitConfigDto configDto = DatasourceUnitConfigDto.create();
      configDto.setUnit(importConfig.getUnit());
      configDto.setAllowIdentifierGeneration(importConfig.isAllowIdentifierGeneration());
      configDto.setIgnoreUnknownIdentifier(importConfig.isIgnoreUnknownIdentifier());
      dto.setUnitConfig(configDto);
    }
  }

  private static DatasourceFactoryDto createSpssDatasourceFactoryDto(ImportConfig importConfig) {
    SpssDatasourceFactoryDto factoryDto = SpssDatasourceFactoryDto.create();
    factoryDto.setFile(importConfig.getSpssFile());
    factoryDto.setCharacterSet(importConfig.getCharacterSet());
    factoryDto.setEntityType(importConfig.getEntityType());

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    configureIncremental(importConfig, dto);
    dto.setExtension(SpssDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);

    return dto;
  }
}
