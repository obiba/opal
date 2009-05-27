/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDataSet;
import org.obiba.onyx.engine.variable.impl.DefaultVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.util.VariableFinder;
import org.obiba.onyx.engine.variable.util.VariableStreamer;
import org.obiba.onyx.util.data.Data;
import org.obiba.opal.core.datasource.onyx.DecryptingOnyxDataInputStrategy;
import org.obiba.opal.core.datasource.onyx.IOnyxDataInputStrategy;
import org.obiba.opal.core.datasource.onyx.OnyxDataInputContext;
import org.obiba.opal.core.datasource.onyx.OpalKeyStore;
import org.obiba.opal.core.service.IParticipantKeyReadRegistry;
import org.obiba.opal.core.service.IParticipantKeyWriteRegistry;
import org.obiba.opal.core.service.OnyxImportService;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default <code>OnyxImportService</code> implementation.
 */
@Transactional
public class DefaultOnyxImportServiceImpl implements OnyxImportService {

  private static final String VARIABLES_FILE = "variables.xml";

  private IParticipantKeyWriteRegistry participantKeyWriteRegistry;

  private IOnyxDataInputStrategy dataInputStrategy;

  /** The unique opal identifying key for the current participant being imported. */
  private String opalKey;

  public void setParticipantKeyWriteRegistry(IParticipantKeyWriteRegistry participantKeyWriteRegistry) {
    this.participantKeyWriteRegistry = participantKeyWriteRegistry;
  }

  public void setDataInputStrategy(IOnyxDataInputStrategy dataInputStrategy) {
    this.dataInputStrategy = dataInputStrategy;
  }

  public void importData() {
    // TODO Auto-generated method stub
    System.out.println("<importData()>");
  }

  public void importData(Date date, String site, List<String> tags) {
    // TODO Auto-generated method stub
    System.out.println("<importData(date: " + date.toString() + ", site: " + site + ", tags: " + tags + ")>");
  }

  public void importData(List<String> tags, File source, final String keyStorePassword, final String keyPassword) {

    // Create the dataInputContext, based on the specified command-line options.
    OnyxDataInputContext dataInputContext = new OnyxDataInputContext();
    dataInputContext.setKeyProviderArg(OpalKeyStore.KEYSTORE_PASSWORD_ARGKEY, keyStorePassword);
    dataInputContext.setKeyProviderArg(OpalKeyStore.KEY_PASSWORD_ARGKEY, keyPassword);
    dataInputContext.setSource(source.getPath());

    dataInputStrategy.prepare(dataInputContext);

    Variable variableRoot = getVariableRoot();

    int participantsProcessed = 0;
    int participantKeysRegistered = 0;

    for(String entryName : dataInputStrategy.listEntries()) {
      if(((DecryptingOnyxDataInputStrategy) dataInputStrategy).isParticipantEntry(entryName)) {

        opalKey = participantKeyWriteRegistry.generateUniqueKey(IParticipantKeyReadRegistry.PARTICIPANT_KEY_DB_OPAL_NAME);

        VariableDataSet variableDataSetRoot = getVariableFromXmlFile(entryName);
        VariableFinder variableFinder = VariableFinder.getInstance(variableRoot, new DefaultVariablePathNamingStrategy());

        for(VariableData variableData : variableDataSetRoot.getVariableDatas()) {
          Variable variable = variableFinder.findVariable(variableData.getVariablePath());
          if(variable != null && variable.getKey() != null && !variable.getKey().equals("")) {
            if(variable.getParent().isRepeatable()) {
              for(VariableData repeatVariableData : variableData.getVariableDatas()) {
                registerOwnerAndKeyInParticipantKeyDatabase(variable, repeatVariableData);
                participantKeysRegistered++;
              }
            } else {
              registerOwnerAndKeyInParticipantKeyDatabase(variable, variableData);
              participantKeysRegistered++;
            }
          }
        }
        participantsProcessed++;
      }
    }
    System.out.println("Participants processed [" + participantsProcessed + "]    Participant Keys Registered [" + participantKeysRegistered + "]");
  }

  /**
   * Returns the {@link Variable} root read from the {@code variables.xml} file found inside the
   * {@link DataInputStrategy}.
   * @return The root of all the {@code Variable} metadata used to describe all the {@link VariableData} found in each
   * participant file.
   */
  private Variable getVariableRoot() {
    Variable variableRoot = null;
    for(String entryName : dataInputStrategy.listEntries()) {
      if(entryName.equalsIgnoreCase(VARIABLES_FILE)) {
        variableRoot = getVariableFromXmlFile(entryName);
      }
    }
    if(variableRoot == null) throw new IllegalStateException("Unable to load variables. The file [" + VARIABLES_FILE + "] was not found.");
    return variableRoot;
  }

  /**
   * Converts an XML file into a Variable or VariableDataSet.
   * @param <T> The type to be returned, such as Variable or VariableDataSet.
   * @param filename The XML file to be converted.
   * @return The root Variable or VariableDataSet
   */
  private <T> T getVariableFromXmlFile(String filename) {
    InputStream inputStream = null;
    T object = null;
    try {
      inputStream = dataInputStrategy.getEntry(filename);
      object = VariableStreamer.<T> fromXML(inputStream);
      if(object == null) throw new IllegalStateException("Unable to load variables from the file [" + filename + "].");
    } finally {
      try {
        inputStream.close();
      } catch(IOException e) {
        throw new IllegalStateException("Could not close InputStream for file [" + filename + "].");
      }
    }
    return object;
  }

  private void registerOwnerAndKeyInParticipantKeyDatabase(Variable variable, VariableData variableData) {
    String owner = variable.getKey();
    for(Data data : variableData.getDatas()) {
      String key = data.getValueAsString();
      participantKeyWriteRegistry.registerEntry(IParticipantKeyReadRegistry.PARTICIPANT_KEY_DB_OPAL_NAME, opalKey, owner, key);
    }
  }

}