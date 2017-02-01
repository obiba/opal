/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma;

import com.google.common.base.Strings;
import org.obiba.magma.*;
import org.obiba.magma.support.*;
import org.obiba.opal.r.ROperationWithResult;
import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.service.OpalRSession;
import org.rosuda.REngine.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * A value table based on a tibble.
 */
public class RValueTable extends AbstractValueTable {

  private int idPosition;

  public RValueTable(@NotNull RDatasource datasource, @NotNull String name, String entityType, String idColumn) {
    super(datasource, name);
    this.idPosition = 0;
    setVariableEntityProvider(new RVariableEntityProvider(this, entityType, idColumn));
  }

  @Override
  public void initialise() {
    checkIsTibble();
    initialiseVariables();
    super.initialise();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new RValueSet(this, entity);
  }

  @Override
  protected ValueSetBatch getValueSetsBatch(List<VariableEntity> entities) {
    return super.getValueSetsBatch(entities);
  }

  boolean isMultilines() {
    return ((RVariableEntityProvider)getVariableEntityProvider()).isMultilines();
  }

  int getIdPosition() {
    return idPosition;
  }

  String getIdColumn() {
    return ((RVariableEntityProvider)getVariableEntityProvider()).getIdColumn();
  }

  //
  // Private methods
  //

  private void checkIsTibble() {
    REXP isTibble = execute(String.format("is.tibble(`%s`)", getName()));
    if (isTibble.isLogical()) {
      REXPLogical isTibbleLogical = (REXPLogical) isTibble;
      if (isTibbleLogical.length() == 0 || !isTibbleLogical.isTRUE()[0]) throw new IllegalArgumentException(getName() + " is not a tibble.");
    } else {
      throw new IllegalArgumentException("Cannot determine if " + getName() + " is a tibble.");
    }
  }

  private void initialiseVariables() {
    REXPGenericVector tibble = (REXPGenericVector) execute(String.format("`%s`[0,]", getName()));
    RList columns = tibble.asList();
    REXPVector colnames = (REXPVector) tibble.getAttribute("names");
    try {
      int pos = 1;
      for (String colname : colnames.asStrings()) {
        if (!getIdColumn().equals(colname)) {
          REXP attr = execute(String.format("attributes(`%s`$`%s`)", getName(), colname));
          addVariableValueSource(new RVariableValueSource(this, colname, pos, (REXP)columns.get(pos - 1), attr));
        }
        else
          idPosition = pos;
        pos++;
      }
    } catch (REXPMismatchException e) {
      // ignore
    }
  }

  REXP execute(String script) {
    return execute(new RScriptROperation(script, false));
  }

  REXP execute(ROperationWithResult rop) {
    getRSession().execute(rop);
    return rop.getResult();
  }

  @Override
  public Timestamps getTimestamps() {
    return NullTimestamps.get();
  }

  private OpalRSession getRSession() {
    return ((RDatasource) getDatasource()).getRSession();
  }

}
