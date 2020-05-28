/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.cart.service;

import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.model.client.magma.VariableDto;

public class CartVariableItem {

  private final String identifier;

  private final String datasource;

  private final String table;

  private final VariableDto variable;

  public CartVariableItem(String variableFullName, String variableStr) {
    this.identifier = variableFullName;
    MagmaPath.Parser parser = MagmaPath.Parser.parse(variableFullName);
    this.datasource = parser.getDatasource();
    this.table = parser.getTable();
    this.variable = VariableDto.parse(variableStr);
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getDatasource() {
    return datasource;
  }

  public String getTable() {
    return table;
  }

  public VariableDto getVariable() {
    return variable;
  }

  public String getEntityType() {
    return variable.getEntityType();
  }

  public String getTableReference() {
    return MagmaPath.Builder.datasource(datasource).table(table).build();
  }
}
