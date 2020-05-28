/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArrayString;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.support.VariableDtoNature;
import org.obiba.opal.web.gwt.rql.client.RQLParser;
import org.obiba.opal.web.gwt.rql.client.RQLQuery;
import org.obiba.opal.web.model.client.magma.VariableDto;

import java.util.List;

/**
 * Single RQL query simple parser: expected statement is one that can be produced by criterion dropdowns.
 */
public class RQLCriterionParser {

  protected String fieldName;
  private String value;
  private JsArrayString values;
  private boolean not;
  private boolean all;
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

  public boolean isAll() {
    return all;
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
    return !Strings.isNullOrEmpty(value) || values != null;
  }

  public String getValue() {
    return value;
  }

  public String getValueString() {
    if (!hasValue()) return "";
    if (!Strings.isNullOrEmpty(value)) return value;
    return Joiner.on(",").join(JsArrays.toIterable(values));
  }

  public List<String> getValues() {
    if (!hasValue()) return Lists.newArrayList();
    if (!Strings.isNullOrEmpty(value)) return Lists.newArrayList(value);
    return JsArrays.toList(values);
  }

  public boolean isValid() {
    return true;
  }

  private void parseRQLQuery(String query) {
    if (Strings.isNullOrEmpty(query)) return;
    RQLQuery node = RQLParser.parse(query).getRQLQuery(0);
    not = "not".equals(node.getName());
    if (not) {
      node = node.getRQLQuery(0);
    }
    exists = "exists".equals(node.getName());
    if (exists) {
      parseField(node.getString(0));
      return;
    }
    all = "all".equals(node.getName());
    if (all) {
      parseField(node.getString(0));
      return;
    }
    like = "like".equals(node.getName());
    range = "range".equals(node.getName());
    in = range || "in".equals(node.getName());

    parseField(node.getString(0));
    if (node.getArgumentsSize()<2) return;
    if (node.isArray(1)) {
      values = node.getArray(1);
    }
    else value = node.getString(1);
  }

  protected void parseField(String field) {
    this.fieldName = field.trim();
  }

}
