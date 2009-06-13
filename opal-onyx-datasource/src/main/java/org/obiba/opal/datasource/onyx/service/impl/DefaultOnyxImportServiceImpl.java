/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.onyx.service.impl;

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
import org.obiba.opal.core.crypt.OpalKeyStore;
import org.obiba.opal.core.service.IParticipantKeyReadRegistry;
import org.obiba.opal.core.service.IParticipantKeyWriteRegistry;
import org.obiba.opal.datasource.onyx.IOnyxDataInputStrategy;
import org.obiba.opal.datasource.onyx.OnyxDataInputContext;
import org.obiba.opal.datasource.onyx.configuration.KeyVariable;
import org.obiba.opal.datasource.onyx.configuration.OnyxImportConfiguration;
import org.obiba.opal.datasource.onyx.service.OnyxImportService;
import org.obiba.opal.datasource.onyx.variable.VariableDataVisitor;
import org.obiba.opal.datasource.onyx.variable.VariableVisitor;
import org.obiba.opal.elmo.concepts.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import com.thoughtworks.xstream.XStream;

/**
 * Default <code>OnyxImportService</code> implementation.
 */
@Transactional
public class DefaultOnyxImportServiceImpl implements OnyxImportService {

  private static final Logger log = LoggerFactory.getLogger(DefaultOnyxImportServiceImpl.class);

  private static final String VARIABLES_FILE = "variables.xml";

  private static final String ENCRYPTION_FILE = "encryption.xml";

  public static final String PARTICIPANT_DATA_EXTENSION = ".xml";

  private IParticipantKeyWriteRegistry participantKeyWriteRegistry;

  private IParticipantKeyReadRegistry participantKeyReadRegistry;

  private IOnyxDataInputStrategy dataInputStrategy;

  private OnyxImportConfiguration onyxImportConfiguration;

  private VariableDataVisitor variableDataVisitor;

  private VariableVisitor variableVisitor;

  public void setParticipantKeyWriteRegistry(IParticipantKeyWriteRegistry participantKeyWriteRegistry) {
    this.participantKeyWriteRegistry = participantKeyWriteRegistry;
  }

  public void setParticipantKeyReadRegistry(IParticipantKeyReadRegistry participantKeyReadRegistry) {
    this.participantKeyReadRegistry = participantKeyReadRegistry;
  }

  public void setDataInputStrategy(IOnyxDataInputStrategy dataInputStrategy) {
    this.dataInputStrategy = dataInputStrategy;
  }

  public void setVariableDataVisitor(VariableDataVisitor variableDataVisitor) {
    this.variableDataVisitor = variableDataVisitor;
  }

  public void setVariableVisitor(VariableVisitor variableVisitor) {
    this.variableVisitor = variableVisitor;
  }

  public void setImportConfiguration(Resource importConfiguration) throws IOException {
    XStream xstream = new XStream();
    xstream.processAnnotations(OnyxImportConfiguration.class);
    xstream.processAnnotations(KeyVariable.class);
    xstream.autodetectAnnotations(true);

    this.onyxImportConfiguration = (OnyxImportConfiguration) xstream.fromXML(importConfiguration.getInputStream());
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
    Variable root = loadVariables();

    if(!onyxImportConfiguration.isCatalogOnly()) {
      loadParticipants(root);
    }
  }

  protected Variable loadVariables() {
    Variable variableRoot = getVariableRoot();
    try {
      for(Variable def : variableRoot.getVariables()) {
        variableVisitor.forDataEntryForm(def);
        for(Variable v : def.getVariables()) {
          variableVisitor.visit(v);
        }
      }
    } finally {
      variableVisitor.end();
    }
    return variableRoot;
  }

  protected void loadParticipants(Variable variableRoot) {
    int participantsProcessed = 0;
    int skipped = 0;
    int loaded = 0;
    int participantKeysRegistered = 0;

    VariableFinder variableFinder = VariableFinder.getInstance(variableRoot, new DefaultVariablePathNamingStrategy());
    try {
      for(String entryName : dataInputStrategy.listEntries()) {
        if(isParticipantEntry(entryName)) {
          log.debug("Processing participant entry {}", entryName);

          VariableDataSet variableDataSetRoot = getVariableFromXmlFile(entryName);

          if(isNewParticipant(variableFinder, variableDataSetRoot)) {
            log.debug("Processing new participant data.");
            String opalKey = participantKeyWriteRegistry.generateUniqueKey(IParticipantKeyReadRegistry.PARTICIPANT_KEY_DB_OPAL_NAME);
            participantKeysRegistered += registerKeys(opalKey, variableFinder, variableDataSetRoot);
            loadData(opalKey, variableDataSetRoot);
            loaded++;
          } else {
            skipped++;
          }
          participantsProcessed++;
        }
      }
    } finally {
      variableDataVisitor.end();
    }

    log.info("Participants processed [{}] (skipped {}, loaded {}) Participant Keys Registered [{}]", new Object[] { participantsProcessed, skipped, loaded, participantKeysRegistered });
  }

  protected boolean isNewParticipant(VariableFinder variableFinder, VariableDataSet variableDataSetRoot) {
    for(VariableData variableData : variableDataSetRoot.getVariableDatas()) {
      Variable variable = variableFinder.findVariable(variableData.getVariablePath());
      if(variable != null && variable.getKey() != null && !variable.getKey().equals("")) {
        String owner = variable.getKey();
        for(Data data : variableData.getDatas()) {
          String key = data.getValueAsString();
          if(participantKeyReadRegistry.hasParticipant(owner, key)) {
            return false;
          }
        }
      }
    }
    return true;

  }

  protected int registerKeys(String opalKey, VariableFinder variableFinder, VariableDataSet variableDataSetRoot) {
    int participantKeysRegistered = 0;
    for(VariableData variableData : variableDataSetRoot.getVariableDatas()) {
      Variable variable = variableFinder.findVariable(variableData.getVariablePath());
      log.debug("variable={}", variableData.getVariablePath());
      if(variable != null && variable.getKey() != null && !variable.getKey().equals("")) {
        log.debug("***** key={}", variable.getKey());
        if(variable.getParent().isRepeatable()) {
          for(VariableData repeatVariableData : variableData.getVariableDatas()) {
            registerOwnerAndKeyInParticipantKeyDatabase(opalKey, variable, repeatVariableData);
            participantKeysRegistered++;
          }
        } else {
          registerOwnerAndKeyInParticipantKeyDatabase(opalKey, variable, variableData);
          participantKeysRegistered++;
        }
      }
    }
    return participantKeysRegistered;
  }

  /**
   * Returns the {@link Variable} root read from the {@code variables.xml} file found inside the
   * {@link DataInputStrategy}.
   * @return The root of all the {@code Variable} metadata used to describe all the {@link VariableData} found in each
   * participant file.
   */
  private Variable getVariableRoot() {
    Variable variableRoot = null;
    if(onyxImportConfiguration.getCatalog() != null) {
      log.debug("catalog={}", onyxImportConfiguration.getCatalog());
      InputStream stream = null;
      try {
        stream = onyxImportConfiguration.getCatalogResource().getInputStream();
        variableRoot = VariableStreamer.fromXML(stream);
      } catch(IOException e) {
        throw new IllegalStateException("Unable to load variables from resource: " + onyxImportConfiguration.getCatalog());
      } finally {
        if(stream != null) {
          try {
            stream.close();
          } catch(IOException e) {
            throw new IllegalStateException("Could not close InputStream for variables resource: " + onyxImportConfiguration.getCatalog());
          }
        }
      }
    } else {
      for(String entryName : dataInputStrategy.listEntries()) {
        if(entryName.equalsIgnoreCase(VARIABLES_FILE)) {
          variableRoot = getVariableFromXmlFile(entryName);
        }
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
    log.debug("getVariableFromXmlFile({})", filename);
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

  private void registerOwnerAndKeyInParticipantKeyDatabase(String opalKey, Variable variable, VariableData variableData) {
    String owner = variable.getKey();
    for(Data data : variableData.getDatas()) {
      String key = data.getValueAsString();
      participantKeyWriteRegistry.registerEntry(IParticipantKeyReadRegistry.PARTICIPANT_KEY_DB_OPAL_NAME, opalKey, owner, key);
    }
  }

  /**
   * Returns true if the entryName is a Participant .xml datafile.
   * @param entryName The name of the entry.
   * @return True if the entryName is a Participant .xml datafile.
   */
  private boolean isParticipantEntry(String entryName) {
    return (entryName != null && entryName.endsWith(PARTICIPANT_DATA_EXTENSION) && !entryName.equalsIgnoreCase(VARIABLES_FILE) && !entryName.equalsIgnoreCase(ENCRYPTION_FILE));
  }

  private void loadData(String opalId, VariableDataSet vds) {
    variableDataVisitor.forEntity(Participant.class, opalId);
    for(VariableData vd : vds.getVariableDatas()) {
      variableDataVisitor.visit(vd);
    }
  }
}