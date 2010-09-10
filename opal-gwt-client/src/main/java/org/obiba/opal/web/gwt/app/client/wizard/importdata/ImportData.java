/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata;

public class ImportData {

  private ImportFormat importFormat;

  private String xmlFile;

  private String destinationDatasourceName;

  private String destinationTableName;

  private String characterSet;

  private boolean identifierAsIs;

  private boolean identifierSharedWithUnit;

  private String unit;

  private boolean archiveLeave;

  private boolean archiveMove;

  private String archiveDirectory;

  private String jobId;

  private int row;

  private String quote;

  private String field;

  private String csvFile;

  private String transientDatasourceName;

  public void clear() {
    importFormat = null;
    xmlFile = null;
    destinationDatasourceName = null;
    destinationTableName = null;
    characterSet = null;
    identifierAsIs = false;
    identifierSharedWithUnit = false;
    unit = null;
    archiveLeave = false;
    archiveMove = false;
    archiveDirectory = null;
    jobId = null;
    row = 0;
    quote = null;
    field = null;
    csvFile = null;
    transientDatasourceName = null;
  }

  public void setFormat(ImportFormat importFormat) {
    this.importFormat = importFormat;
  }

  public ImportFormat getImportFormat() {
    return importFormat;
  }

  public void setXmlFile(String selectedFile) {
    this.xmlFile = selectedFile;
  }

  public String getXmlFile() {
    return xmlFile;
  }

  public void setDestinationDatasourceName(String selectedDatasource) {
    this.destinationDatasourceName = selectedDatasource;
  }

  public String getDestinationDatasourceName() {
    return destinationDatasourceName;
  }

  public void setDestinationTableName(String selectedTable) {
    this.destinationTableName = selectedTable;
  }

  public String getDestinationTableName() {
    return destinationTableName;
  }

  public void setCharacterSet(String selectedCharacterSet) {
    this.characterSet = selectedCharacterSet;
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

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
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

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
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

}
