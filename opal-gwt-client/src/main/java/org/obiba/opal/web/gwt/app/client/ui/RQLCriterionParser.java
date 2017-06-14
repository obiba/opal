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
import com.google.common.collect.Lists;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.support.VariableDtoNature;
import org.obiba.opal.web.model.client.magma.VariableDto;

import java.util.List;

/**
 * Single RQL query simple parser: expected statement is one that can be produced by criterion dropdowns.
 */
public class RQLCriterionParser {

  protected String fieldName;
  private String value;
  private boolean not;
  private boolean exists;
  private boolean like;
  private boolean in;
  private boolean range;

  public RQLCriterionParser() {}

  public RQLCriterionParser(String query) {
    parseRQLQuery(query);
  }

  public String getField() {
    return fieldName;
  }

  public boolean isNot() {
    return not;
  }

  public boolean isExists() {
    return exists;
  }

  public boolean isIn() {
    return in;
  }

  public boolean isLike() {
    return like;
  }

  public boolean isRange() {
    return range;
  }

  public boolean hasWildcardValue() {
    return hasValue() && "*".equals(value);
  }

  public boolean hasValue() {
    return !Strings.isNullOrEmpty(value);
  }

  public String getValue() {
    return value;
  }

  public String getValueString() {
    if (!hasValue()) return "";
    if (value.startsWith("(") && value.endsWith(")")) return value.substring(1, value.length() - 1);
    return value;
  }

  public List<String> getValues() {
    List<String> values = Lists.newArrayList();
    if (!hasValue()) return values;
    String nValue = value;
    if (value.startsWith("(") && value.endsWith(")"))
      nValue = value.substring(1, value.length() - 1);
    for (String val : Splitter.on(",").splitToList(nValue)) {
      values.add(val.trim());
    }
    return values;
  }

  public boolean isValid() {
    return true;
  }

  private void parseRQLQuery(String query) {
    if (Strings.isNullOrEmpty(query)) return;
    String nQuery = query.trim();
    if (nQuery.startsWith("not(") && nQuery.endsWith(")")) {
      not = true;
      nQuery = nQuery.substring(4, nQuery.length() - 1);
    }
    if (nQuery.startsWith("exists(") && nQuery.endsWith(")")) {
      exists = true;
      nQuery = nQuery.substring(7, nQuery.length() - 1);
      parseField(nQuery);
      return;
    }

    if (nQuery.startsWith("like(") && nQuery.endsWith(")")) {
      like = true;
      nQuery = nQuery.substring(5, nQuery.length() - 1);
    }
    if (nQuery.startsWith("in(") && nQuery.endsWith(")")) {
      in = true;
      nQuery = nQuery.substring(3, nQuery.length() - 1);
    }
    if (nQuery.startsWith("range(") && nQuery.endsWith(")")) {
      in = true;
      range = true;
      nQuery = nQuery.substring(6, nQuery.length() - 1);
    }
    // at this point we should have "field,values"
    int idx = nQuery.indexOf(',');
    parseField(nQuery.substring(0, idx));
    parseValue(nQuery.substring(idx + 1));
  }

  protected void parseField(String field) {
    this.fieldName = field.trim();
  }

  private void parseValue(String valueString) {
    this.value = valueString;
  }

}
