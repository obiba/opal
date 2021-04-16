/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.google.common.base.Strings;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.support.VariableDtoNature;
import org.obiba.opal.web.model.client.magma.VariableDto;

/**
 * Single variable filter query simple parser: expected statement is one that can be produced by criterion dropdowns.
 */
public class RQLValueSetVariableCriterionParser extends RQLCriterionParser {

  private String datasourceName;
  private String tableName;
  private String variableName;
  private VariableDto variable;

  public RQLValueSetVariableCriterionParser(String query) {
    super(query);
  }

  public RQLValueSetVariableCriterionParser(String datasource, String table, VariableDto variable) {
    this(datasource, table, variable, datasource + "." + table + ":" + variable.getName());
  }

  public RQLValueSetVariableCriterionParser(String datasource, String table, VariableDto variable, String field) {
    this.datasourceName = datasource;
    this.tableName = table;
    this.variableName = variable.getName();
    this.variable = variable;
    this.fieldName = field;
  }

  public boolean isValid() {
    return isValid(datasourceName) && isValid(tableName) && isValid(variableName);
  }

  public String getDatasourceName() {
    return datasourceName;
  }

  public String getTableName() {
    return tableName;
  }

  public String getTableReference() {
    return datasourceName + "." + tableName;
  }

  public String getVariableName() {
    return variableName;
  }

  public VariableDto getVariable() {
    return variable;
  }

  public void setVariable(VariableDto variable) {
    this.variable = variable;
  }

  public VariableDtoNature getNature() {
    return VariableDtoNature.getNature(variable);
  }

  protected void parseField(String field) {
    super.parseField(field);
    MagmaPath.Parser parser = MagmaPath.Parser.parse(fieldName);
    this.datasourceName = parser.getDatasource();
    this.tableName = parser.getTable();
    this.variableName = parser.getVariable();
  }

  private boolean isValid(String token) {
    return !Strings.isNullOrEmpty(token);
  }

}
