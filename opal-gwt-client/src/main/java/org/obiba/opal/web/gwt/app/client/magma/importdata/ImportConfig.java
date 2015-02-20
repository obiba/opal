/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.importdata;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

@SuppressWarnings("UnusedDeclaration")
public class ImportConfig {

  public enum ImportFormat {

    CSV, EXCEL, XML, LIMESURVEY, REST, JDBC, SPSS

  }

  private ImportFormat importFormat;

  private boolean incremental;

  private Integer limit;

  private String xmlFile;

  private String destinationDatasourceName;

  private String destinationTableName;

  private String characterSet;

  private boolean identifierAsIs;

  private boolean identifierSharedWithUnit;

  private boolean archiveLeave;

  private boolean archiveMove;

  private String archiveDirectory;

  private int row;

  private String quote;

  private String field;

  private String csvFile;

  private String spssFile;

  private String transientDatasourceName;

  private String entityType;

  private String database;

  private String tablePrefix;

  private Map<String, Object> properties;

  private String idMapping;

  private boolean allowIdentifierGeneration;

  private boolean ignoreUnknownIdentifier;

  private String locale;

  @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
  public void clear() {
    importFormat = null;
    incremental = false;
    xmlFile = null;
    destinationDatasourceName = null;
    destinationTableName = null;
    characterSet = null;
    identifierAsIs = false;
    identifierSharedWithUnit = false;
    idMapping = null;
    archiveLeave = false;
    archiveMove = false;
    archiveDirectory = null;
    row = 0;
    quote = null;
    field = null;
    csvFile = null;
    spssFile = null;
    locale = null;

    transientDatasourceName = null;
    database = null;
    tablePrefix = null;
    allowIdentifierGeneration = false;
    ignoreUnknownIdentifier = false;
  }

  public void setFormat(ImportFormat importFormat) {
    this.importFormat = importFormat;
  }

  public ImportFormat getImportFormat() {
    return importFormat;
  }

  public void setXmlFile(String xmlFile) {
    this.xmlFile = xmlFile;
  }

  public String getXmlFile() {
    return xmlFile;
  }

  public void setDestinationDatasourceName(@Nullable String destinationDatasourceName) {
    this.destinationDatasourceName = destinationDatasourceName;
  }

  public String getDestinationDatasourceName() {
    return destinationDatasourceName;
  }

  public void setDestinationTableName(String destinationTableName) {
    this.destinationTableName = destinationTableName;
  }

  public String getDestinationTableName() {
    return destinationTableName;
  }

  public void setCharacterSet(String characterSet) {
    this.characterSet = characterSet;
  }

  public String getCharacterSet() {
    return characterSet;
  }

  public boolean isIdentifierAsIs() {
    return identifierAsIs;
  }

  public void setIdentifierAsIs(boolean identifierAsIs) {
    this.identifierAsIs = identifierAsIs;
  }

  public boolean isIdentifierSharedWithUnit() {
    return identifierSharedWithUnit;
  }

  public void setIdentifierSharedWithUnit(boolean identifierSharedWithUnit) {
    this.identifierSharedWithUnit = identifierSharedWithUnit;
  }

  public String getIdentifiersMapping() {
    return idMapping;
  }

  public void setIdentifiersMapping(String idMapping) {
    this.idMapping = idMapping;
  }

  public boolean isArchiveLeave() {
    return archiveLeave;
  }

  public void setArchiveLeave(boolean archiveLeave) {
    this.archiveLeave = archiveLeave;
  }

  public boolean isArchiveMove() {
    return archiveMove;
  }

  public void setArchiveMove(boolean archiveMove) {
    this.archiveMove = archiveMove;
  }

  public String getArchiveDirectory() {
    return archiveDirectory;
  }

  public void setArchiveDirectory(String archiveDirectory) {
    this.archiveDirectory = archiveDirectory;
  }

  public void setImportFormat(ImportFormat importFormat) {
    this.importFormat = importFormat;
  }

  public int getRow() {
    return row;
  }

  public void setRow(int row) {
    this.row = row;
  }

  public String getCsvFile() {
    return csvFile;
  }

  public void setCsvFile(String csvFile) {
    this.csvFile = csvFile;
  }

  public String getQuote() {
    return quote;
  }

  public void setQuote(String quote) {
    this.quote = quote;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getTransientDatasourceName() {
    return transientDatasourceName;
  }

  public void setTransientDatasourceName(String transientDatasourceName) {
    this.transientDatasourceName = transientDatasourceName;
  }

  public void setDestinationEntityType(String entityType) {
    this.entityType = entityType;
  }

  public String getEntityType() {
    return entityType;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getTablePrefix() {
    return tablePrefix;
  }

  public void setTablePrefix(String tablePrefix) {
    this.tablePrefix = tablePrefix;
  }

  public boolean isIncremental() {
    return incremental;
  }

  public void setIncremental(boolean incremental) {
    this.incremental = incremental;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public Integer getLimit() {
    return limit;
  }

  private Map<String, Object> getProperties() {
    if(properties == null) {
      properties = Maps.newHashMap();
    }
    return properties;
  }

  public ImportConfig put(String key, Object value) {
    getProperties().put(key, value);
    return this;
  }

  public Object get(String key) {
    return getProperties().get(key);
  }

  public String getString(String key) {
    Object value = getProperties().get(key);
    return value == null ? null : value.toString();
  }

  public boolean isAllowIdentifierGeneration() {
    return allowIdentifierGeneration;
  }

  public void setAllowIdentifierGeneration(boolean allowIdentifierGeneration) {
    this.allowIdentifierGeneration = allowIdentifierGeneration;
  }

  public boolean isIgnoreUnknownIdentifier() {
    return ignoreUnknownIdentifier;
  }

  public void setIgnoreUnknownIdentifier(boolean ignoreUnknownIdentifier) {
    this.ignoreUnknownIdentifier = ignoreUnknownIdentifier;
  }

  public String getSpssFile() {
    return spssFile;
  }

  public void setSpssFile(String spssFile) {
    this.spssFile = spssFile;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }
}
