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

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceTransformer;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 */
public class FunctionalUnitDatasourceManager implements DatasourceTransformer {

  private final TransactionTemplate txTemplate;

  private final OpalRuntime opalRuntime;

  private final IParticipantIdentifier participantIdentifier;

  /** Configured through org.obiba.opal.keys.tableReference */
  private final String keysTableReference;

  @Autowired
  public FunctionalUnitDatasourceManager(TransactionTemplate txTemplate, OpalRuntime opalRuntime, IParticipantIdentifier participantIdentifier, @Value("${org.obiba.opal.keys.tableReference}") String keysTableReference) {
    super();
    if(txTemplate == null) throw new IllegalArgumentException("txManager cannot be null");
    if(opalRuntime == null) throw new IllegalArgumentException("opalRuntime cannot be null");
    if(participantIdentifier == null) throw new IllegalArgumentException("participantIdentifier cannot be null");
    if(keysTableReference == null) throw new IllegalArgumentException("keysTableReference cannot be null");

    this.txTemplate = txTemplate;
    this.opalRuntime = opalRuntime;
    this.participantIdentifier = participantIdentifier;
    this.keysTableReference = keysTableReference;
  }

  @Override
  public Datasource transform(Datasource datasource) {
    FunctionalUnit unit = null;
    // TODO search for unit given a datasource name
    if(unit != null) {
      return new FunctionalUnitDatasource(txTemplate, unit, datasource, lookupKeysTable(), participantIdentifier);
    } else
      return datasource;
  }

  private ValueTable lookupKeysTable() {
    return MagmaEngineTableResolver.valueOf(keysTableReference).resolveTable();
  }

}
