/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.cart.service;

import org.obiba.opal.web.gwt.app.client.support.MagmaPath;

public class CartVariableItem {

  private final String identifier;

  private final String datasource;

  private final String table;

  private final String variable;

  private final String entityType;

  public CartVariableItem(String variableFullName, String entityType) {
    this.identifier = variableFullName;
    MagmaPath.Parser parser = MagmaPath.Parser.parse(variableFullName);
    this.datasource = parser.getDatasource();
    this.table = parser.getTable();
    this.variable = parser.getVariable();
    this.entityType = entityType;
  }

  public CartVariableItem(String datasource, String table, String variable, String entityType) {
    this.identifier = MagmaPath.Builder.datasource(datasource).table(table).variable(variable).build();
    this.datasource = datasource;
    this.table = table;
    this.variable = variable;
    this.entityType = entityType;
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

  public String getVariable() {
    return variable;
  }

  public String getEntityType() {
    return entityType;
  }

  public String getTableReference() {
    return MagmaPath.Builder.datasource(datasource).table(table).build();
  }
}
