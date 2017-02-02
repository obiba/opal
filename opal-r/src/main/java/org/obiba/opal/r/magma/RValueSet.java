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

import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.ValueSetBean;
import org.rosuda.REngine.REXP;

import javax.validation.constraints.NotNull;

/**
 * The value set is the tibble subset for the entity identifier.
 */
class RValueSet extends ValueSetBean {

  private REXP rexp;

  RValueSet(@NotNull RValueTable table, @NotNull VariableEntity entity) {
    super(table, entity);
  }

  public REXP getREXP() {
    if (rexp == null) {
      // subset tibble: get the row(s) matching the entity id (result is a tibble)
      String cmd = String.format("`%s`[`%s`$`%s`=='%s',]", getRValueTable().getSymbol(), getRValueTable().getSymbol(),
          getRValueTable().getIdColumn(), getVariableEntity().getIdentifier());
      setREXP(getRValueTable().execute(cmd));
    }
    return rexp;
  }

  public void setREXP(REXP rexp) {
    this.rexp = rexp;
  }

  private RValueTable getRValueTable() {
    return (RValueTable) getValueTable();
  }

}
