/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.obiba.opal.web.gwt.app.client.support.VariableDtoNature;
import org.obiba.opal.web.model.client.magma.VariableDto;

import java.util.List;

/**
 * Single variable filter query simple parser: expected statement is one that can be produced by criterion dropdowns.
 */
public class ValueSetVariableCriterion {
  private String fieldName;
  private String datasourceName;
  private String tableName;
  private String variableName;
  private String value;
  private boolean not;
  private boolean exists;
  private VariableDto variable;

  public ValueSetVariableCriterion(String query) {
    String nQuery = query;
    if (query.startsWith("NOT ")) {
      not = true;
      nQuery = nQuery.substring(4);
    }
    if (nQuery.startsWith("_exists_")) {
      exists = true;
      nQuery = nQuery.substring(9);
    }
    fieldName = nQuery;
    if (!exists) {
      int idx = nQuery.indexOf(":");
      fieldName = nQuery.substring(0, idx);
      value = nQuery.substring(idx + 1);
    }
    List<String> tokens = Splitter.on("__").splitToList(fieldName);
    if (tokens.size() > 0) datasourceName = tokens.get(0);
    if (tokens.size() > 1) tableName = tokens.get(1);
    if (tokens.size() > 2) variableName = tokens.get(2);
  }

  public ValueSetVariableCriterion(String datasource, String table, VariableDto variable, String field) {
    this.datasourceName = datasource;
    this.tableName = table;
    this.variableName = variable.getName();
    this.variable = variable;
    this.fieldName = field;
  }

  public boolean isValid() {
    return isValid(datasourceName) && isValid(tableName) && isValid(variableName);
  }

  public String getField() {
    return fieldName;
  }

  public String getDatasourceName() {
    return datasourceName;
  }

  public String getTableName() {
    return tableName;
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

  public boolean isNot() {
    return not;
  }

  public boolean isExists() {
    return exists;
  }

  public boolean hasValue() {
    return !Strings.isNullOrEmpty(value);
  }

  public String getValue() {
    return value;
  }

  private boolean isValid(String token) {
    return !Strings.isNullOrEmpty(token);
  }

  public VariableDtoNature getNature() {
    return VariableDtoNature.getNature(variable);
  }
}
