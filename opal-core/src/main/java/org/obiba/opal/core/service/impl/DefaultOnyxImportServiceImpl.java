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
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;

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

import com.sun.security.auth.callback.TextCallbackHandler;

/**
 * Default <code>OnyxImportService</code> implementation.
 */
@Transactional
public class DefaultOnyxImportServiceImpl implements OnyxImportService {

  private static final String VARIABLES_FILE = "variables.xml";

  private IParticipantKeyReadRegistry participantKeyReadRegistry;

  private IParticipantKeyWriteRegistry participantKeyWriteRegistry;

  private IOnyxDataInputStrategy dataInputStrategy;

  public void setParticipantKeyReadRegistry(IParticipantKeyReadRegistry participantKeyReadRegistry) {
    this.participantKeyReadRegistry = participantKeyReadRegistry;
  }

  public void setParticipantKeyWriteRegistry(IParticipantKeyWriteRegistry participantKeyWriteRegistry) {
    this.participantKeyWriteRegistry = participantKeyWriteRegistry;
  }

  public void setDataInputStrategy(IOnyxDataInputStrategy dataInputStrategy) {
    this.dataInputStrategy = dataInputStrategy;
  }

  public void importData(String username, String password) {
    // TODO Auto-generated method stub
    System.out.println("<importData(user: " + username + ", password: " + password + ")>");
  }

  public void importData(String username, String password, Date date, String site, List<String> tags) {
    // TODO Auto-generated method stub
    System.out.println("<importData(user: " + username + ", password: " + password + ", date: " + date.toString() + ", site: " + site + ", tags: " + tags + ")>");
  }

  public void importData(String username, String password, List<String> tags, File source) {
    System.out.println("<importData(user: " + username + ", password: " + password + ", tags: " + tags + ", file: " + source.getPath() + ")>\n");

    String keystorePassword = promptForPassword("Enter keystore password: ");

    String keyPassword = promptForPassword("Enter key password (RETURN if same as keystore password): ");
    if(keyPassword == null) {
      keyPassword = keystorePassword;
    }

    // Create the dataInputContext, based on the specified command-line options.
    OnyxDataInputContext dataInputContext = new OnyxDataInputContext();
    dataInputContext.setKeyProviderArg(OpalKeyStore.KEYSTORE_PASSWORD_ARGKEY, keystorePassword);
    dataInputContext.setKeyProviderArg(OpalKeyStore.KEY_PASSWORD_ARGKEY, keyPassword);
    dataInputContext.setSource(source.getPath());

    dataInputStrategy.prepare(dataInputContext);

    Variable variableRoot = null;
    for(String entryName : dataInputStrategy.listEntries()) {
      if(entryName.equalsIgnoreCase(VARIABLES_FILE)) {
        InputStream entryStream = dataInputStrategy.getEntry(entryName);
        variableRoot = VariableStreamer.fromXML(entryStream);
      }
    }
    if(variableRoot == null) throw new IllegalStateException("Unable to load variables from [variables.xml].");

    int participantsProcessed = 0;
    int participantKeysRegistered = 0;
    for(String entryName : dataInputStrategy.listEntries()) {
      if(((DecryptingOnyxDataInputStrategy) dataInputStrategy).isParticipantEntry(entryName)) {
        InputStream entryStream = dataInputStrategy.getEntry(entryName);

        String opalKey = null; // Unique key used by Opal to identify this participant

        VariableDataSet variableDataSetRoot = VariableStreamer.fromXML(entryStream);
        VariableFinder variableFinder = VariableFinder.getInstance(variableRoot, new DefaultVariablePathNamingStrategy());
        for(VariableData variableData : variableDataSetRoot.getVariableDatas()) {
          Variable var = variableFinder.findVariable(variableData.getVariablePath());

          if(var != null && var.getKey() != null && !var.getKey().equals("")) {
            // the data of this variable is a participant ID that should go to the participant key database
            for(Data data : variableData.getDatas()) {
              String participantID = data.getValueAsString();
              if(participantKeyReadRegistry.hasParticipant(var.getKey(), participantID)) {
                opalKey = getOneOpalKey(var.getKey(), participantID);
              } else {
                if(opalKey == null) {
                  participantKeyWriteRegistry.registerEntry(var.getKey(), participantID);
                  opalKey = getOneOpalKey(var.getKey(), participantID);
                  participantKeysRegistered++;
                } else {
                  participantKeyWriteRegistry.registerEntry(IParticipantKeyReadRegistry.PARTICIPANT_KEY_DB_OPAL_NAME, opalKey, var.getKey(), participantID);
                  participantKeysRegistered++;
                }
              }
            }
          }
        }
        participantsProcessed++;
      }
    }
    System.out.println("Participants processed [" + participantsProcessed + "]    Participant Keys Registered [" + participantKeysRegistered + "]");
  }

  private String getOneOpalKey(String owner, String key) {
    Collection<String> opalKeys = participantKeyReadRegistry.getEntry(owner, key, IParticipantKeyReadRegistry.PARTICIPANT_KEY_DB_OPAL_NAME);
    if(opalKeys.size() > 0) {
      String returnValue = null;
      for(String opalKey : opalKeys) {
        returnValue = opalKey;
        break;
      }
      return returnValue;
    } else {
      throw new IllegalStateException("The participant with the owner/key pair [" + owner + "]=[" + key + "] does not have an associated Opal key.");
    }
  }

  private String promptForPassword(String prompt) {
    String password = null;

    PasswordCallback passwordCallback = new PasswordCallback(prompt, false);
    TextCallbackHandler handler = new TextCallbackHandler();

    try {
      handler.handle(new Callback[] { passwordCallback });
      if(passwordCallback.getPassword() != null) {
        password = new String(passwordCallback.getPassword());

        if(password.length() == 0) {
          password = null;
        }
      }
    } catch(Exception ex) {
      // nothing to do
    }

    return password;
  }
}
