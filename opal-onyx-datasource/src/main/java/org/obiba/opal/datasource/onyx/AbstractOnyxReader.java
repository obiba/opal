/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.onyx;

import java.io.IOException;
import java.io.InputStream;

import org.obiba.core.util.StreamUtil;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableDataSet;
import org.obiba.onyx.engine.variable.util.VariableStreamer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.core.io.Resource;

/**
 * 
 */
public abstract class AbstractOnyxReader<T> implements ItemStreamReader<T> {

  private static final String VARIABLES_FILE = "variables.xml";

  private static final String PARTICIPANT_DATA_EXTENSION = ".xml";

  private Resource resource;

  private IOnyxDataInputStrategy dataInputStrategy;

  private OnyxDataInputContext dataInputContext;

  protected Variable variableRoot;

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public void setDataInputStrategy(IOnyxDataInputStrategy dataInputStrategy) {
    this.dataInputStrategy = dataInputStrategy;
  }

  public IOnyxDataInputStrategy getDataInputStrategy() {
    return dataInputStrategy;
  }

  public void close() throws ItemStreamException {
    dataInputStrategy.terminate(dataInputContext);
  }

  public void open(ExecutionContext executionContext) throws ItemStreamException {
    dataInputContext = new OnyxDataInputContext();
    try {
      dataInputContext.setSource(resource.getFile().getPath());
    } catch(IOException e) {
      throw new ItemStreamException(e);
    }
    dataInputStrategy.prepare(dataInputContext);

    doOpen(executionContext);
  }

  public void update(ExecutionContext executionContext) throws ItemStreamException {
  }

  protected void doOpen(ExecutionContext executionContext) throws ItemStreamException {
    variableRoot = readVariables();
  }

  protected VariableDataSet readVariableDataset(String entryName) {
    return getVariableFromXmlFile(entryName);
  }

  protected Variable readVariables() {
    return getVariableFromXmlFile(VARIABLES_FILE);
  }

  /**
   * Converts an XML file into a Variable or VariableDataSet.
   * @param <T> The type to be returned, such as Variable or VariableDataSet.
   * @param filename The XML file to be converted.
   * @return The root Variable or VariableDataSet
   */
  synchronized protected <E> E getVariableFromXmlFile(String filename) {
    InputStream inputStream = null;
    E object = null;
    try {
      inputStream = dataInputStrategy.getEntry(filename);
      object = VariableStreamer.<E> fromXML(inputStream);
      if(object == null) {
        throw new IllegalStateException("Unable to load variables from the file [" + filename + "].");
      }
    } finally {
      StreamUtil.silentSafeClose(inputStream);
    }
    return object;
  }

  /**
   * Returns true if the entryName is a Participant .xml datafile.
   * @param entryName The name of the entry.
   * @return True if the entryName is a Participant .xml datafile.
   */
  protected boolean isParticipantEntry(String entryName) {
    return (entryName != null && entryName.endsWith(PARTICIPANT_DATA_EXTENSION) && !entryName.equalsIgnoreCase(VARIABLES_FILE));
  }
}
