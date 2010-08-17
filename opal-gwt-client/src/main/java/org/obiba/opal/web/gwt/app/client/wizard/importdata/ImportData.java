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

}
