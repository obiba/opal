/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.search.support;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import net.jazdw.rql.parser.ASTNode;
import org.joda.time.DateTime;
import org.obiba.magma.ValueType;
import org.obiba.magma.support.VariableNature;
import org.obiba.magma.type.TextType;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Criterion for filtering values based on a variable field, expressed in RQL.
 */
public class RQLCriterionParser {

  private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * RQL query object.
   */
  private ASTNode queryNode;

  /**
   * RQL query string.
   */
  private final String rqlQuery;

  /**
   * ES query string.
   */
  private String query;

  public RQLCriterionParser(String rqlQuery) {
    this.rqlQuery = rqlQuery;
  }

  public RQLCriterionParser(ASTNode queryNode) {
    this.queryNode = queryNode;
    this.rqlQuery = toString(queryNode);
  }

  public String getOriginalQuery() {
    return rqlQuery;
  }

  public String getQuery() {
    if (Strings.isNullOrEmpty(query)) {
      this.query = queryNode == null ? parseNode(RQLParserFactory.newParser().parse(rqlQuery))
            : parseNode(queryNode);
    }
    return query;
  }

  private String toString(ASTNode node) {
    StringBuilder builder = new StringBuilder(node.getName()).append("(");
    for (int i=0; i<node.getArgumentsSize(); i++) {
      if (i>0) builder.append(",");
      append(builder, node.getArgument(i));
    }
    builder.append(")");
    return builder.toString();
  }

  private void append(StringBuilder builder, Object arg) {
    if (arg instanceof ASTNode) builder.append(toString((ASTNode)arg));
    else if (arg instanceof Collection) {
      builder.append("(");
      Collection<?> values = (Collection) arg;
      int i=0;
      for (Object value : values) {
        if (i>0) builder.append(",");
        append(builder, value);
        i++;
      }
      builder.append(")");
    }
    else if (arg instanceof DateTime) builder.append(normalizeDate((DateTime)arg));
    else builder.append(arg.toString().replace("%","%25"));
  }

  private String parseNode(ASTNode node) {
    switch (node.getName()) {
      case "not":
        return "NOT " + parseNode((ASTNode) node.getArgument(0));
      case "exists":
        return "_exists_:" + parseField(node.getArgument(0).toString());
      case "all":
        parseField(node.getArgument(0).toString());
        return "";
      case "in":
        return parseField(node.getArgument(0).toString()) + ":(" + parseValue(node.getArgument(1)) + ")";
      case "like":
        String valueStr = parseValue(node.getArgument(1));
        if (Strings.isNullOrEmpty(valueStr))
          valueStr = "''";
        return "(" + parseField(node.getArgument(0).toString()) + ":(" + valueStr + ") OR "
            + parseFieldLike(node.getArgument(0).toString()) + ":(" + valueStr + "))";
      case "range":
        return parseField(node.getArgument(0).toString()) + ":[" + parseRange(node.getArgument(1)) + "]";
      case "and":
        return parseAnd(node.getArguments());
      case "or":
        return parseOr(node.getArguments());
    }
    return node.toString();
  }

  protected String parseField(String path) {
    return path;
  }

  protected String parseFieldLike(String path) {
    return path + ".analyzed";
  }

  protected ValueType getValueType() {
    return TextType.get();
  }

  protected VariableNature getNature() {
    return VariableNature.UNDETERMINED;
  }

  protected String join(String on, Collection<?> args) {
    String nOn = on;
    boolean toQuote = getNature().equals(VariableNature.CATEGORICAL);
    List<String> nArgs = args.stream().map(arg -> {
      String nArg = arg instanceof DateTime ? normalizeDate((DateTime) arg) : arg.toString();
      if (toQuote) return quote(nArg);
      else return normalizeString(nArg);
    }).collect(Collectors.toList());

    return Joiner.on(nOn).join(nArgs);
  }

  //
  // Private methods
  //

  private String parseValue(Object value) {
    if (value instanceof ASTNode) return parseNode((ASTNode) value);
    if (value instanceof Collection) {
      Collection<?> values = (Collection) value;
      if (values.size() == 1 && getValueType().isDateTime()) return parseSingleDate(values.iterator().next());
      return parseOr(values);
    }
    if (value instanceof DateTime) return parseSingleDate(value);
    return getNature().equals(VariableNature.CATEGORICAL) ? quote(value) : normalizeString(value.toString());
  }

  private String normalizeString(String str) {
    return str.replace(" ","+");
  }

  private String normalizeDate(DateTime date) {
    return DATE_FORMAT.format(date.toDate());
  }

  private String quote(Object value) {
    String valueStr = value.toString();
    if (valueStr.contains("*")) return normalizeString(valueStr);
    if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) return valueStr;
    valueStr = valueStr.replace("+", " ");
    if (valueStr.contains(" ") || valueStr.startsWith("-")) return "\"" + valueStr + "\"";
    return valueStr;
  }

  private String parseRange(Object value) {
    if (value instanceof Collection) {
      return join(" TO ", (Collection) value);
    }
    return value + " TO *";
  }

  private String parseSingleDate(Object value) {
    if (value instanceof DateTime) {
      String dateString = DATE_FORMAT.format(((DateTime) value).toDate());
      return ">=" + dateString + " AND " + "<=" + dateString;
    }
    return normalizeString(value.toString());
  }

  private String parseAnd(Collection<?> args) {
    return join(" AND ", args.stream().map(arg -> parseValue(arg)).collect(Collectors.toList()));
  }

  private String parseOr(Collection<?> args) {
    return join(" OR ", args.stream().map(arg -> parseValue(arg)).collect(Collectors.toList()));
  }

}
