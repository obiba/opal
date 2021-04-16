/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.search.support;

import com.google.common.base.Strings;
import net.jazdw.rql.parser.ASTNode;
import org.obiba.magma.*;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.support.VariableNature;
import org.obiba.magma.type.TextType;
import org.obiba.opal.spi.search.QuerySettings;
import org.obiba.opal.spi.search.ValuesIndexManager;

/**
 * Criterion for filtering values based on a variable field, expressed in RQL.
 */
public class RQLValueSetVariableCriterionParser extends RQLCriterionParser implements ValueSetVariableCriterionParser {

  private final ValuesIndexManager valuesIndexManager;

  private ValueTable table;

  private Variable variable;
  
  public RQLValueSetVariableCriterionParser(ValuesIndexManager valuesIndexManager, ASTNode rqlNode) {
    super(rqlNode);
    this.valuesIndexManager = valuesIndexManager;
  }

  @Override
  public QuerySettings.ChildQuery asChildQuery(String idQuery) {
    String query = getQuery(); // make sure ES query is built
    if (!Strings.isNullOrEmpty(query)) query = " AND " + query;
    if (!Strings.isNullOrEmpty(idQuery)) query = " AND " + idQuery + query;
    query = "reference:\"" + table.getTableReference() + "\"" + query;
    return new QuerySettings.ChildQuery(valuesIndexManager.getIndex(table).getIndexType(), query);
  }

  @Override
  protected String parseField(String variablePath) {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf(variablePath);
    if (resolver.hasTableName()) {
      table = MagmaEngine.get().getDatasource(resolver.getDatasourceName()).getValueTable(resolver.getTableName());
      variable = table.getVariable(resolver.getVariableName());
      String field = valuesIndexManager.getIndex(table).getFieldName(resolver.getVariableName());
      field = field.replace("{","\\{").replace("}","\\}").replace("[","\\[").replace("]","\\]");
      return field;
    } else return variablePath;
  }

  @Override
  protected String parseFieldLike(String path) {
    // variable fields are always analyzed
    return parseField(path);
  }

  @Override
  protected ValueType getValueType() {
    return variable == null ? TextType.get() : variable.getValueType();
  }

  @Override
  protected VariableNature getNature() {
    return variable == null ? VariableNature.UNDETERMINED : VariableNature.getNature(variable);
  }
}
