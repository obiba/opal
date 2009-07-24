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
import java.util.Iterator;
import java.util.List;

import org.obiba.core.util.StreamUtil;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDataSet;
import org.obiba.onyx.engine.variable.util.VariableStreamer;
import org.obiba.onyx.util.data.Data;
import org.obiba.opal.core.crypt.OpalKeyStore;
import org.obiba.opal.core.domain.data.DataItem;
import org.obiba.opal.core.domain.data.Dataset;
import org.obiba.opal.core.domain.data.Entity;
import org.obiba.opal.datasource.EntityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.core.io.Resource;

/**
 *
 */
public class OnyxDatasetReader implements ItemStreamReader<Dataset> {

  private static final Logger log = LoggerFactory.getLogger(OnyxDatasetReader.class);

  private static final String VARIABLES_FILE = "variables.xml";

  public static final String PARTICIPANT_DATA_EXTENSION = ".xml";

  private Resource resource;

  private IOnyxDataInputStrategy dataInputStrategy;

  private EntityProvider entityProvider;

  private OnyxDataInputContext dataInputContext;

  private Iterator<String> entryIterator;

  public void setDataInputStrategy(IOnyxDataInputStrategy dataInputStrategy) {
    this.dataInputStrategy = dataInputStrategy;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public void setEntityProvider(EntityProvider entityProvider) {
    this.entityProvider = entityProvider;
  }

  public Dataset read() throws Exception, UnexpectedInputException, ParseException {
    if(entryIterator.hasNext()) {
      String entryName = entryIterator.next();
      while(isParticipantEntry(entryName) == false && entryIterator.hasNext()) {
        entryName = entryIterator.next();
      }

      if(isParticipantEntry(entryName)) {
        VariableDataSet variableDataSetRoot = getVariableFromXmlFile(entryName);

        Entity entity = entityProvider.fetchEntity("onyx", entryName.replace(".xml", ""));
        Dataset dataset = new Dataset(entity, "onyx", variableDataSetRoot.getExportDate());

        for(VariableData vd : variableDataSetRoot.getVariableDatas()) {
          List<Data> datum = vd.getDatas();
          if(datum.size() == 1) {
            DataItem dataItem = new DataItem(dataset, vd.getVariablePath(), datum.get(0).getValueAsString());
            dataset.getDataItems().add(dataItem);
          }
        }
        return dataset;
      }
    }
    return null;
  }

  public void close() throws ItemStreamException {
    dataInputStrategy.terminate(dataInputContext);
  }

  public void open(ExecutionContext executionContext) throws ItemStreamException {
    dataInputContext = new OnyxDataInputContext();
    dataInputContext.setKeyProviderArg(OpalKeyStore.KEYSTORE_PASSWORD_ARGKEY, "bethyurk");
    dataInputContext.setKeyProviderArg(OpalKeyStore.KEY_PASSWORD_ARGKEY, "bethyurk");
    try {
      dataInputContext.setSource(resource.getFile().getPath());
    } catch(IOException e) {
      throw new ItemStreamException(e);
    }
    dataInputStrategy.prepare(dataInputContext);
    this.entryIterator = dataInputStrategy.listEntries().iterator();
  }

  public void update(ExecutionContext executionContext) throws ItemStreamException {
  }

  /**
   * Converts an XML file into a Variable or VariableDataSet.
   * @param <T> The type to be returned, such as Variable or VariableDataSet.
   * @param filename The XML file to be converted.
   * @return The root Variable or VariableDataSet
   */
  private <T> T getVariableFromXmlFile(String filename) {
    log.debug("getVariableFromXmlFile({})", filename);
    InputStream inputStream = null;
    T object = null;
    try {
      inputStream = dataInputStrategy.getEntry(filename);
      object = VariableStreamer.<T> fromXML(inputStream);
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
  private boolean isParticipantEntry(String entryName) {
    return (entryName != null && entryName.endsWith(PARTICIPANT_DATA_EXTENSION) && !entryName.equalsIgnoreCase(VARIABLES_FILE));
  }

}
