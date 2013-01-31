/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.support;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.AbstractDatasourceWrapper;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.MultiplexingDatasource;
import org.obiba.magma.support.MultiplexingDatasource.VariableAttributeMultiplexer;
import org.obiba.magma.support.MultiplexingDatasource.VariableNameTransformer;

/**
 * Fs Datasource wrapper that tries to detect old Onyx data dictionary, and in that case applies multiplexes the
 * Participants table and rename some variables.
 */
public class OnyxDatasource extends AbstractDatasourceWrapper {

//  private static final Logger log = LoggerFactory.getLogger(OnyxDatasource.class);

  private final FsDatasource fsDatasource;

  private Datasource wrapped;

  public OnyxDatasource(FsDatasource fsDatasource) {
    super(fsDatasource);
    this.fsDatasource = fsDatasource;
  }

  @Override
  public Datasource getWrappedDatasource() {
    return wrapped;
  }

  @Override
  public void initialise() {
    Datasource mxDatasource = new MultiplexingDatasource(fsDatasource, new VariableAttributeMultiplexer("stage"),
        new VariableNameTransformer() {

          @Override
          protected String transformName(Variable variable) {
            if(variable.hasAttribute("stage")) {
              return variable.getName().replaceFirst("^.*\\.?" + variable.getAttributeStringValue("stage") + "\\.", "");
            }
            return variable.getName();
          }

        });
    try {
      // this will initialise the fs datasource
      Initialisables.initialise(mxDatasource);
      wrapped = isToBeMultiplexed() ? mxDatasource : fsDatasource;
    } catch(UnsupportedOperationException ex) {
      wrapped = fsDatasource;
    }
  }

  /**
   * Fs datasource has to have one participants table with name Participants and have a variable that provides onyx
   * version: this identifies old onyx data dictionary.
   *
   * @return
   */
  private boolean isToBeMultiplexed() {
    boolean isOnyx = false;
    int participantTablesCount = 0;
    for(ValueTable table : fsDatasource.getValueTables()) {
      if("Participant".equals(table.getEntityType())) {
        participantTablesCount++;
        if("Participants".equals(table.getName())) {
          isOnyx = table.hasVariable("Admin.onyxVersion");
        }
      }
    }
    return isOnyx && participantTablesCount == 1;
  }

}
