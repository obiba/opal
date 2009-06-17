/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.mart.sas.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.obiba.opal.core.mart.sas.ISasMartBuilder;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * A SAS mart builder writing in a CSV file. If the file already exists, it is overridden. The directory where to output
 * the csv files must be provided.
 */
public class CsvSasMartBuilder implements ISasMartBuilder {

  private static final String SAS_NULL_VALUE = ".";

  private static final String PARTICIPANT_HEADER = "Participant";

  private CSVWriter csvWriter;

  private File csvDirectory;

  private String csvFileName;

  private String csvFileNamePrefix = "opal-sas";

  private char separator = CSVWriter.DEFAULT_SEPARATOR;

  private int variablesCount;

  /**
   * Set the directory where to output the files, required.
   * @param csvDirectory
   */
  public void setCsvDirectory(File csvDirectory) {
    this.csvDirectory = csvDirectory;
  }

  /**
   * Set the CSV file name to be used at each mart build, optional.
   * @param csvFileName
   */
  public void setCsvFileName(String csvFileName) {
    this.csvFileName = csvFileName;
  }

  /**
   * Set the CSV file name prefix to which the current date will be appended, ignored if {@link #setCsvFileName(String)}
   * was passed a not null value.
   * @param csvFileNamePrefix
   */
  public void setCsvFileNamePrefix(String csvFileNamePrefix) {
    this.csvFileNamePrefix = csvFileNamePrefix;
  }

  /**
   * CSV fields separator to be used.
   * @param separator
   */
  public void setSeparator(char separator) {
    this.separator = separator;
  }

  public void setVariableNames(String... names) {
    variablesCount = names.length;
    String[] headers = new String[variablesCount + 1];
    headers[0] = PARTICIPANT_HEADER;
    for(int i = 0; i < names.length; i++) {
      headers[i + 1] = names[i];
    }
    csvWriter.writeNext(headers);
  }

  public void withData(String participantId, Object... values) {
    String[] data = new String[variablesCount + 1];
    data[0] = participantId;

    for(int i = 0; i < values.length; i++) {
      if(values[i] == null) {
        data[i + 1] = SAS_NULL_VALUE;
      } else if(values[i] instanceof Boolean) {
        data[i + 1] = ((Boolean) values[i]) ? "1" : "0";
      } else {
        data[i + 1] = values[i].toString();
      }
    }

    csvWriter.writeNext(data);
  }

  /**
   * Create the file to output and build the CSVWriter according to SAS mart builder options.
   */
  public void initialize() throws IOException {
    if(csvDirectory == null) {
      throw new IllegalArgumentException("Directory where CSV file for SAS will be exported is missing.");
    }
    if(csvDirectory.exists()) {
      if(!csvDirectory.isDirectory()) {
        throw new IllegalArgumentException(csvDirectory.getAbsolutePath() + " is not a directory.");
      }
    } else {
      csvDirectory.mkdirs();
    }

    File csvFile;
    if(csvFileName != null) {
      csvFile = new File(csvDirectory, csvFileName);
    } else {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmm");
      csvFile = new File(csvDirectory, csvFileNamePrefix + "-" + formatter.format(new Date()) + ".csv");
    }

    csvWriter = new CSVWriter(new FileWriter(csvFile), separator);
  }

  /**
   * Flush and close the CSV writer.
   */
  public void shutdown() throws IOException {
    csvWriter.close();
  }

}
