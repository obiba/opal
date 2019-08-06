/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.support;

import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.model.client.identifiers.IdentifiersMappingConfigDto;
import org.obiba.opal.web.model.client.magma.CsvDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.CsvDatasourceTableBundleDto;
import org.obiba.opal.web.model.client.magma.DatasourceBatchConfigDto;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.DatasourceIncrementalConfigDto;
import org.obiba.opal.web.model.client.magma.FsDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.GNPostalCodesDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.HCDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.JdbcDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.PluginDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.RHavenDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.RestDatasourceFactoryDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 *
 */
@SuppressWarnings({ "StaticMethodOnlyUsedInOneClass", "OverlyCoupledClass" })
public class DatasourceDtos {

  private DatasourceDtos() {
  }

  public static boolean hasPersistedTables(DatasourceDto datasource) {
    if(datasource.getTableArray() == null || datasource.getTableArray().length() == 0) return false;
    if(datasource.getViewArray() == null || datasource.getViewArray().length() == 0) return true;
    return datasource.getTableArray().length() > datasource.getViewArray().length();
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  public static DatasourceFactoryDto createDatasourceFactoryDto(ImportConfig importConfig) {
    switch(importConfig.getImportFormat()) {
      case CSV:
        return createCSVDatasourceFactoryDto(importConfig);
      case XML:
        return createXMLDatasourceFactoryDto(importConfig);
      case JDBC:
        return createJdbcDatasourceFactoryDto(importConfig);
      case REST:
        return createRestDatasourceFactoryDto(importConfig);
      case RSPSS:
      case RZSPSS:
      case RSAS:
      case RXPT:
      case RSTATA:
        return createRHavenDatasourceFactoryDto(importConfig);
      case FROM_PLUGIN:
        return createDatasourceFactoryDtoFromPlugin(importConfig);
      default:
        throw new IllegalArgumentException("Import data format not supported: " + importConfig.getImportFormat());
    }
  }

  private static DatasourceFactoryDto createDatasourceFactoryDtoFromPlugin(ImportConfig importConfig) {
    PluginDatasourceFactoryDto factoryDto = PluginDatasourceFactoryDto.create();
    factoryDto.setName(importConfig.getPluginName());
    factoryDto.setParameters(importConfig.getPluginImportConfig().toString());
    return createAndConfigureDatasourceFactoryDto(importConfig, PluginDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
  }

  private static DatasourceFactoryDto createJdbcDatasourceFactoryDto(ImportConfig importConfig) {
    JdbcDatasourceFactoryDto factoryDto = JdbcDatasourceFactoryDto.create();
    factoryDto.setDatabase(importConfig.getDatabase());

    return createAndConfigureDatasourceFactoryDto(importConfig,
        JdbcDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
  }

  private static DatasourceFactoryDto createRestDatasourceFactoryDto(ImportConfig importConfig) {
    RestDatasourceFactoryDto factoryDto = RestDatasourceFactoryDto.create();
    factoryDto.setUrl(importConfig.getString("url"));
    if (importConfig.getString("token") == null) {
      factoryDto.setUsername(importConfig.getString("username"));
      factoryDto.setPassword(importConfig.getString("password"));
    } else
      factoryDto.setToken(importConfig.getString("token"));

    factoryDto.setRemoteDatasource(importConfig.getString("remoteDatasource"));

    return createAndConfigureDatasourceFactoryDto(importConfig,
        RestDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
  }

  private static DatasourceFactoryDto createCSVDatasourceFactoryDto(ImportConfig importConfig) {

    CsvDatasourceTableBundleDto bundleDto = CsvDatasourceTableBundleDto.create();
    bundleDto.setName(importConfig.getDestinationTableName());
    bundleDto.setData(importConfig.getFile());
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
    factoryDto.setDefaultValueType(importConfig.getDefaultValueType());
    factoryDto.setTablesArray(tables);

    return createAndConfigureDatasourceFactoryDto(importConfig,
        CsvDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
  }

  private static DatasourceFactoryDto createXMLDatasourceFactoryDto(ImportConfig importConfig) {

    FsDatasourceFactoryDto factoryDto = FsDatasourceFactoryDto.create();
    factoryDto.setFile(importConfig.getFile());
    factoryDto.setOnyxWrapper(true);
    return createAndConfigureDatasourceFactoryDto(importConfig,
        FsDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
  }

  private static DatasourceFactoryDto createAndConfigureDatasourceFactoryDto(ImportConfig importConfig,
      String extensionName, JavaScriptObject extension) {
    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    configureUnit(importConfig, dto);
    configureIncremental(importConfig, dto);
    configureBatch(importConfig, dto);
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

  private static void configureBatch(ImportConfig importConfig, DatasourceFactoryDto dto) {
    if(importConfig.getLimit() != null) {
      DatasourceBatchConfigDto configDto = DatasourceBatchConfigDto.create();
      configDto.setLimit(importConfig.getLimit());
      dto.setBatchConfig(configDto);
    }
  }

  private static void configureUnit(ImportConfig importConfig, DatasourceFactoryDto dto) {
    if(!Strings.isNullOrEmpty(importConfig.getIdentifiersMapping())) {
      IdentifiersMappingConfigDto configDto = IdentifiersMappingConfigDto.create();
      configDto.setName(importConfig.getIdentifiersMapping());
      configDto.setAllowIdentifierGeneration(importConfig.isAllowIdentifierGeneration());
      configDto.setIgnoreUnknownIdentifier(importConfig.isIgnoreUnknownIdentifier());
      dto.setIdConfig(configDto);
    }
  }

  private static DatasourceFactoryDto createRHavenDatasourceFactoryDto(ImportConfig importConfig) {
    RHavenDatasourceFactoryDto factoryDto = RHavenDatasourceFactoryDto.create();
    String file = importConfig.getFile();
    factoryDto.setFile(file);
    if (importConfig.hasAdditionalFile()) factoryDto.setCatFile(importConfig.getAdditionalFile());
    String symbol = file.substring(file.lastIndexOf('/') + 1, file.lastIndexOf('.'));
    factoryDto.setSymbol(symbol);
    factoryDto.setEntityType(importConfig.getEntityType());
    factoryDto.setIdColumn(importConfig.getIdColumn());

    return createAndConfigureDatasourceFactoryDto(importConfig,
        RHavenDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
  }

  private static DatasourceFactoryDto createHCDatasourceFactoryDto(ImportConfig importConfig) {
    HCDatasourceFactoryDto factoryDto = HCDatasourceFactoryDto.create();

    return createAndConfigureDatasourceFactoryDto(importConfig,
        HCDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
  }

  private static DatasourceFactoryDto createGNPostalCodesDatasourceFactoryDto(ImportConfig importConfig) {
    GNPostalCodesDatasourceFactoryDto factoryDto = GNPostalCodesDatasourceFactoryDto.create();

    return createAndConfigureDatasourceFactoryDto(importConfig,
        GNPostalCodesDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, factoryDto);
  }
}
