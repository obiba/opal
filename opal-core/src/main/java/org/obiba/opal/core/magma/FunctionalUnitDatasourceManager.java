/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.magma;

import java.util.HashMap;
import java.util.Map;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.Decorator;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 */
public class FunctionalUnitDatasourceManager implements Decorator<Datasource> {

  private final OpalRuntime opalRuntime;

  private final IParticipantIdentifier participantIdentifier;

  /** Configured through org.obiba.opal.keys.tableReference */
  private final String keysTableReference;

  private Map<String, FunctionalUnitDatasource> functionalUnitDatasourcesMap = new HashMap<String, FunctionalUnitDatasource>();

  @Autowired
  public FunctionalUnitDatasourceManager(TransactionTemplate txTemplate, OpalRuntime opalRuntime, IParticipantIdentifier participantIdentifier, @Value("${org.obiba.opal.keys.tableReference}") String keysTableReference) {
    super();
    if(opalRuntime == null) throw new IllegalArgumentException("opalRuntime cannot be null");
    if(participantIdentifier == null) throw new IllegalArgumentException("participantIdentifier cannot be null");
    if(keysTableReference == null) throw new IllegalArgumentException("keysTableReference cannot be null");

    this.opalRuntime = opalRuntime;
    this.participantIdentifier = participantIdentifier;
    this.keysTableReference = keysTableReference;
  }

  @Override
  public Datasource decorate(Datasource datasource) {
    // FIXME too soon for DatasourceResource:250, the factory is not registered yet!
    for(DatasourceFactory factory : opalRuntime.getOpalConfiguration().getMagmaEngineFactory().factories()) {
      if(factory.getName().equals(datasource.getName())) {
        // TODO get associated unit if defined
        // FunctionalUnit unit = opalRuntime.getFunctionalUnit(factory.getUnit());
        // if(unit != null) {
        // FunctionalUnitDatasource fuDs = new FunctionalUnitDatasource(datasource, unit.getKeyVariableName(),
        // lookupKeysTable(),
        // participantIdentifier);
        // functionalUnitDatasourcesMap.put(datasource.getName(), fuDs);
        // return fuDs;
        // }
        // else return datasource; // not a unit dependent datasource
      }
    }
    return datasource;
  }

  /**
   * Synchronizes the private identifiers with Opal identifiers.
   * @param datasource
   */
  public void mapIdentifiers(String datasource) {
    FunctionalUnitDatasource ds = functionalUnitDatasourcesMap.get(datasource);
    if(ds != null) {
      ds.mapIdentifiers();
    }
  }

  private ValueTable lookupKeysTable() {
    return MagmaEngineTableResolver.valueOf(keysTableReference).resolveTable();
  }

}
