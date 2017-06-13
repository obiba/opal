/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.search.support;

import com.google.common.base.Joiner;
import net.jazdw.rql.parser.ASTNode;
import net.jazdw.rql.parser.RQLParser;
import org.joda.time.DateTime;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.domain.VariableNature;
import org.obiba.opal.spi.search.ValuesIndexManager;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Criterion for filtering values based on a variable field, expressed in RQL.
 */
public class RQLValueSetVariableCriterionParser implements ValueSetVariableCriterionParser {

  private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private final ValuesIndexManager valuesIndexManager;

  private final String rqlQuery;

  private final String query;

  private ValueTable table;

  private Variable variable;

  public RQLValueSetVariableCriterionParser(ValuesIndexManager valuesIndexManager, String query) {
    this.valuesIndexManager = valuesIndexManager;
    this.rqlQuery = query;
    this.query = parseNode(new RQLParser().parse(query));
  }

  @Override
  public QuerySearchJsonBuilder.ChildQuery asChildQuery() {
    return new QuerySearchJsonBuilder.ChildQuery(valuesIndexManager.getIndex(table).getIndexType(), query);
  }

  @Override
  public String getOriginalQuery() {
    return rqlQuery;
  }

  private String parseNode(ASTNode node) {
    switch (node.getName()) {
      case "not":
        return "NOT " + parseNode((ASTNode) node.getArgument(0));
      case "exists":
        return "_exists_:" + parseField(node.getArgument(0).toString());
      case "in":
        return parseField(node.getArgument(0).toString()) + ":(" + parseValue(node.getArgument(1)) + ")";
      case "range":
        return parseField(node.getArgument(0).toString()) + ":[" + parseRange(node.getArgument(1)) + "]";
      case "and":
        return parseAnd(node.getArguments());
      case "or":
        return parseOr(node.getArguments());
    }
    return node.toString();
  }

  private String parseField(String variablePath) {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf(variablePath);
    table = MagmaEngine.get().getDatasource(resolver.getDatasourceName()).getValueTable(resolver.getTableName());
    variable = table.getVariable(resolver.getVariableName());
    return valuesIndexManager.getIndex(table).getFieldName(resolver.getVariableName());
  }

  private String parseValue(Object value) {
    if (value instanceof ASTNode) return parseNode((ASTNode) value);
    if (value instanceof Collection) {
      Collection<?> values = (Collection) value;
      if (values.size() == 1 && getValueType().isDateTime()) return parseSingleDate(values.iterator().next());
      return parseOr(values);
    }
    return value.toString();
  }

  private String parseRange(Object value) {
    if (value instanceof Collection) {
      return join(" TO ", (Collection) value);
    }
    return value + " TO *";
  }

  private String parseSingleDate(Object value) {
    String dateString = DATE_FORMAT.format(((DateTime) value).toDate());
    return ">=" + dateString + " AND " + "<=" + dateString;
  }

  private String parseAnd(Collection<?> args) {
    return join(" AND ", args);
  }

  private String parseOr(Collection<?> args) {
    return join(" OR ", args);
  }

  private String join(String on, Collection<?> args) {
    String nOn = on;
    boolean toQuote = getNature().equals(VariableNature.CATEGORICAL);
    List<String> nArgs = args.stream().map(arg -> {
      String nArg = getValueType().isDateTime() ? DATE_FORMAT.format(((DateTime) arg).toDate()) : arg.toString();
      if (toQuote) return "\"" + nArg + "\"";
      else return nArg;
    }).collect(Collectors.toList());

    return Joiner.on(nOn).join(nArgs);
  }

  private ValueType getValueType() {
    return variable == null ? TextType.get() : variable.getValueType();
  }

  private VariableNature getNature() {
    return variable == null ? VariableNature.UNDETERMINED : VariableNature.getNature(variable);
  }
}
