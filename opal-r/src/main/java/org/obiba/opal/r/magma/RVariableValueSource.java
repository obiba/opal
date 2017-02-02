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

import com.google.common.collect.Lists;
import org.obiba.magma.*;
import org.obiba.magma.type.*;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * The R variable represents the column of a tibble.
 */
class RVariableValueSource extends AbstractVariableValueSource implements VariableValueSource, VectorSource {

  private static final Logger log = LoggerFactory.getLogger(RVariableValueSource.class);

  public static final String EPOCH = "1970-01-01";

  private RValueTable valueTable;

  private final String colName;

  private final String colClass;

  private final List<String> colTypes;

  private final int position;

  private Variable variable;

  RVariableValueSource(RValueTable valueTable, RList column, int position) throws REXPMismatchException {
    this.valueTable = valueTable;
    this.colName = column.at("name").asString();
    this.colClass = column.at("class").asString();
    this.colTypes = Lists.newArrayList(column.at("type").asString().split("\\+"));
    REXP colAttr = column.at("attributes");
    this.position = position;
    initialiseVariable(colAttr);
  }

  @Override
  public Variable getVariable() {
    return variable;
  }

  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @Override
  public Iterable<Value> getValues(SortedSet<VariableEntity> entities) {
    return null;
  }

  @Override
  public Value getValue(ValueSet valueSet) {
    Map<Integer, List<String>> columnValues = ((RValueSet) valueSet).getValuesByPosition();
    if (!columnValues.containsKey(position))
      return variable.isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
    return getValue(columnValues.get(position));
  }

  @Override
  public boolean supportVectorSource() {
    return true;
  }

  @Override
  public VectorSource asVectorSource() throws VectorSourceNotSupportedException {
    return this;
  }

  //
  // Private methods
  //

  private void initialiseVariable(REXP attr) {
    this.variable = VariableBean.Builder.newVariable(colName, extractValueType(attr), valueTable.getEntityType())
        .addAttributes(extractAttributes(attr)).addCategories(extractCategories(attr))
        .repeatable(valueTable.isMultilines()).occurrenceGroup(valueTable.isMultilines() ? valueTable.getSymbol() : null).build();
  }

  private ValueType extractValueType(REXP attr) {
    ValueType type = TextType.get();
    if (isNumeric()) type = isInteger() ? IntegerType.get() : DecimalType.get();
    else if (isInteger()) type = IntegerType.get();
    else if (isDate()) type = DateType.get();
    else if (isDateTime()) type = DateTimeType.get();
    else if (isBoolean()) type = BooleanType.get();
    else if (isBinary()) type = BinaryType.get();
    log.info("Tibble '{}' has column '{}' of class {} mapped to {}", valueTable.getSymbol(), colName, colClass, type.getName());
    return type;
  }

  private boolean isNumeric() {
    return "numeric".equals(colClass);
  }

  private boolean isInteger() {
    return "integer".equals(colClass) || isNumeric() && colTypes.contains("int");
  }

  private boolean isBoolean() {
    return "logical".equals(colClass);
  }

  private boolean isBinary() {
    return "raw".equals(colClass);
  }

  private boolean isDate() {
    return "Date".equals(colClass);
  }

  private boolean isDateTime() {
    return "POSIXct".equals(colClass) || "POSIXt".equals(colClass);
  }

  private List<Attribute> extractAttributes(REXP attr) {
    List<Attribute> attributes = Lists.newArrayList();
    if (attr == null || !attr.isList()) return attributes;
    try {
      RList rList = attr.asList();
      if (!rList.isNamed()) return attributes;
      for (String key : rList.keys()) {
        if (key.equals("labels")) continue;
        REXP value = rList.at(key);
        String name = key;
        String namespace = null;
        if (key.contains("::")) {
          String[] nsn = key.split("::");
          name = nsn[1];
          namespace = nsn[0];
        }
        if (value.isString() && value.length() > 0) {
          attributes.add(Attribute.Builder.newAttribute(name).withNamespace(namespace).withValue(value.asStrings()[0]).build());
        } else {
          log.info("Attribute value is a {}", value.getClass().getName());
        }
      }

    } catch (REXPMismatchException e) {
      // ignore
      log.warn("Error while parsing variable attributes: {}", colName, e);
    }
    return attributes;
  }

  private List<Category> extractCategories(REXP attr) {
    if ("labelled".equals(colClass))
      return extractCategoriesFromLabels(extractAttribute(attr, "labels"));
    else if ("factor".equals(colClass))
      return extractCategoriesFromLevels(extractAttribute(attr, "levels"));
    return Lists.newArrayList();
  }

  private List<Category> extractCategoriesFromLabels(REXP labels) {
    List<Category> categories = Lists.newArrayList();
    if (labels == null) return categories;
    try {
      String[] catLabels = null;
      if (labels.hasAttribute("names")) {
        catLabels = labels.getAttribute("names").asStrings();
      }
      int i = 0;
      for (String name : labels.asStrings()) {
        Category.Builder builder = Category.Builder.newCategory(name);
        if (catLabels != null) {
          builder.addAttribute("label", catLabels[i]);
        }
        categories.add(builder.build());
        i++;
      }
    } catch (REXPMismatchException e) {
      // ignore
      log.warn("Error while parsing variable categories: {}", colName, e);
    }
    return categories;
  }

  private List<Category> extractCategoriesFromLevels(REXP levels) {
    log.warn("Extracting '{}' categories factor levels not implemented yet", colName);
    return Lists.newArrayList();
  }

  private REXP extractAttribute(REXP attr, String attrName) {
    if (attr == null || !attr.isList()) return null;
    try {
      RList rList = attr.asList();
      if (!rList.isNamed() || !rList.containsKey(attrName)) return null;
      return rList.at(attrName);
    } catch (REXPMismatchException e) {
      return null;
    }
  }

  private Value getValue(List<String> strValues) {
    if (strValues == null || strValues.size() == 0)
      return variable.isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
    return variable.isRepeatable() ?
        getValueType().sequenceOf(Lists.newArrayList(strValues).stream().map(this::getSingleValue).collect(Collectors.toList())) :
        getSingleValue(strValues.get(0));
  }

  private Value getSingleValue(String strValue) {
    if (isDate())
      return getDateFromEpoch(strValue);
    else if (isDateTime())
      return getDateTimeFromEpoch(strValue);
    return "NaN".equals(strValue) ? getValueType().nullValue() : getValueType().valueOf(strValue);
  }

  /**
   * R dates are serialized as a number of days since epoch (1970-01-01).
   *
   * @param strValue
   * @return
   */
  private Value getDateFromEpoch(String strValue) {
    if ("NaN".equals(strValue)) return getValueType().nullValue();
    try {
      Date value = new Date(Long.parseLong(strValue.replaceAll("\\.0$", "")) * 24 * 3600 * 1000);
      return getValueType().valueOf(value);
    } catch (Exception e) {
      return getValueType().nullValue();
    }
  }

  private Value getDateTimeFromEpoch(String strValue) {
    if ("NaN".equals(strValue)) return getValueType().nullValue();
    try {
      Date value = new Date(Long.parseLong(strValue.replaceAll("\\.0$", "")));
      return getValueType().valueOf(value);
    } catch (Exception e) {
      return getValueType().nullValue();
    }
  }
}
