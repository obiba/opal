/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource.magma;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.magma.*;
import org.obiba.magma.type.*;
import org.obiba.opal.spi.r.RNamedList;
import org.obiba.opal.spi.r.RServerResult;
import org.obiba.opal.spi.r.RUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RVariableHelper {

  private static final Logger log = LoggerFactory.getLogger(RVariableHelper.class);

  private static final String[] EXCLUDED_ATTRIBUTES = {
      "labels", "labels_names", "class",
      "valueType", "entityType", "mimeType", "referencedEntityType",
      "repeatable", "occurrenceGroup", "unit"
  };

  private final String entityType;

  private final boolean multilines;

  private final String colName;

  private final List<String> colClasses;

  private final List<String> colTypes;

  private final RServerResult colAttr;

  private final int position;

  private final String defaultLocale;

  RVariableHelper(RNamedList<RServerResult> columnDesc, TibbleTable valueTable, int position) {
    this(columnDesc, valueTable.getEntityType(), valueTable.isMultilines(), valueTable.getDefaultLocale(), position);
  }

  RVariableHelper(RNamedList<RServerResult> columnDesc, String entityType, boolean multilines, String defaultLocale, int position) {
    this.entityType = entityType;
    this.multilines = multilines;
    this.colName = columnDesc.get("name").asStrings()[0];
    this.colClasses = Lists.newArrayList(columnDesc.get("class").asStrings());
    this.colTypes = Lists.newArrayList(columnDesc.get("type").asStrings()[0].split("\\+"));
    this.colAttr = columnDesc.get("attributes");
    this.position = position;
    this.defaultLocale = defaultLocale;
  }

  public static Variable newVariable(RNamedList<RServerResult> columnDesc, String entityType, boolean multilines, String defaultLocale, int position) {
    final RVariableHelper helper = new RVariableHelper(columnDesc, entityType, multilines, defaultLocale, position);
    return helper.newVariable();
  }

  Variable newVariable() {
    String repeatableProp = extractProperty("opal.repeatable");
    boolean repeatable = Strings.isNullOrEmpty(repeatableProp) ?
        multilines : ("1.0".equals(repeatableProp) || "1".equals(repeatableProp));

    int index = position;
    String indexStr = extractProperty("opal.index");
    if (!Strings.isNullOrEmpty(indexStr)) {
      try {
        index = Double.valueOf(indexStr).intValue();
      } catch (NumberFormatException e) {
        // ignore
      }
    }

    String occurrenceGroup = extractProperty( "opal.occurrence_group");

    return VariableBean.Builder.newVariable(colName, extractValueType(), entityType)
        .unit(extractProperty("opal.unit"))
        .referencedEntityType(extractProperty( "opal.referenced_entity_type"))
        .mimeType(extractProperty( "opal.mime_type"))
        .repeatable(repeatable)
        .occurrenceGroup(Strings.isNullOrEmpty(occurrenceGroup) ? null : occurrenceGroup)
        .addAttributes(extractAttributes())
        .addCategories(extractCategories())
        .index(index)
        .build();
  }

  boolean isNumeric() {
    return colClasses.contains("numeric");
  }

  boolean isInteger() {
    return colClasses.contains("integer") || colTypes.contains("int");
  }

  boolean isDecimal() {
    return colClasses.contains("double") || colTypes.contains("dbl");
  }

  boolean isBoolean() {
    return colClasses.contains("logical");
  }

  boolean isBinary() {
    return colClasses.contains("raw");
  }

  boolean isDate() {
    return colClasses.contains("Date");
  }

  boolean isDateTime() {
    return colClasses.contains("POSIXct") || colClasses.contains("POSIXt");
  }

  //
  // Private methods
  //

  private ValueType extractValueType() {
    ValueType type = null;
    String typePropertyStr = extractProperty("opal.value_type");
    if (!Strings.isNullOrEmpty(typePropertyStr)) {
      try {
        type = ValueType.Factory.forName(typePropertyStr);
      } catch (Exception e) {
        // ignore
        log.warn("Not a valid 'opal.value_type' attribute value: {}", typePropertyStr);
      }
    }
    if (type == null) {
      type = TextType.get();
      // column's data type has the priority
      if (isNumeric()) type = isInteger() ? IntegerType.get() : DecimalType.get();
      else if (isInteger()) type = IntegerType.get();
      else if (isDecimal()) type = DecimalType.get();
      else if (isDate()) type = DateType.get();
      else if (isDateTime()) type = DateTimeType.get();
      else if (isBoolean()) type = BooleanType.get();
      else if (isBinary()) type = BinaryType.get();
    }
    //log.debug("Tibble '{}' has column '{}' of class '{}' mapped to {}", valueTable.getSymbol(), colName, Joiner.on(", ").join(colClasses), type.getName());
    return type;
  }

  private String extractProperty(String property) {
    if (colAttr == null) return null;
    try {
      RNamedList<RServerResult> rList = colAttr.asNamedList();
      if (rList.isEmpty()) return null;
      for (String key : rList.keySet()) {
        if (property.equals(key)) {
          RServerResult value = rList.get(key);
          return value.isNull() ? null : value.asStrings()[0];
        }
      }
    } catch (Exception e) {
      // ignore
    }
    return null;
  }

  private List<Attribute> extractAttributes() {
    List<Attribute> attributes = Lists.newArrayList();
    if (colAttr == null || !colAttr.isNamedList()) return attributes;
    try {
      RNamedList<RServerResult> rList = colAttr.asNamedList();
      if (rList.isEmpty()) return attributes;
      for (String key : rList.keySet()) {
        if (key.startsWith("opal.") || Arrays.asList(EXCLUDED_ATTRIBUTES).contains(key)) continue;
        RServerResult value = rList.get(key);
        String name = key;
        String namespace = null;
        if (key.contains("::")) {
          String[] nsn = key.split("::");
          name = nsn[1];
          namespace = nsn[0];
        }
        if (value.isString() && value.length() > 0 && !Strings.isNullOrEmpty(value.asStrings()[0])) {
          String strValue = value.asStrings()[0];
          if (Strings.isNullOrEmpty(namespace) && ("label".equals(name) || "description".equals(name)))
            attributes.addAll(extractLocalizedAttributes(namespace, name, strValue));
          else
            attributes.add(Attribute.Builder.newAttribute(name).withNamespace(namespace).withValue(strValue).build());
        } else {
          log.debug("Attribute value is a {}", value.getClass().getName());
        }
      }

    } catch (Exception e) {
      // ignore
      log.warn("Error while parsing variable attributes: {}", colName, e);
    }
    return attributes;
  }

  private boolean hasCategories() {
    return colClasses.contains("labelled") || colClasses.contains("haven_labelled")
        || colClasses.contains("labelled_spss") || colClasses.contains("haven_labelled_spss")
        || colClasses.contains("factor");
  }

  private List<Category> extractCategories() {
    RServerResult labels = extractAttribute("labels");
    if (labels != null) {
      return extractCategoriesFromLabels(labels,
          extractAttribute("labels_names"),
          extractAttribute("na_values"),
          extractAttribute("na_range"));
    } else if (colClasses.contains("factor"))
      return extractCategoriesFromLabels(extractAttribute("levels"),
          extractAttribute("levels_names"),
          extractAttribute("na_values"),
          extractAttribute("na_range"));
    return Lists.newArrayList();
  }

  /**
   * Extract categories and flag missings appropriatly.
   *
   * @param labels        Category values
   * @param missings      Discrete missings
   * @param missingsRange Range of missings
   * @return
   */
  private List<Category> extractCategoriesFromLabels(RServerResult labels, RServerResult labelsNames, RServerResult missings, RServerResult missingsRange) {
    List<Category> categories = Lists.newArrayList();
    if (labels == null) return categories;
    try {
      String[] catLabels = null;
      if (labelsNames != null) {
        catLabels = labelsNames.asStrings();
      } else if (labels.hasNames()) {
        catLabels = labels.getNames();
      }
      List<String> missingNames = Lists.newArrayList();
      if (missings != null) {
        for (String name : missings.asStrings()) {
          missingNames.add(normalizeCategoryName(name));
        }
      }

      int i = 0;
      for (String name : labels.asStrings()) {
        if ("NaN".equals(name) && (labels.isNumeric() || labels.isInteger())) {
          // skip
          i++;
        } else {
          String catName = normalizeCategoryName(name);
          Category.Builder builder = Category.Builder.newCategory(catName);
          if (catLabels != null) {
            extractLocalizedAttributes(null, "label", catLabels[i]).forEach(builder::addAttribute);
          }
          builder.missing(missingNames.contains(catName)
              || (labels.isInteger() && integerRangeContains(missingsRange, labels.asIntegers()[i]))
              || (labels.isNumeric() && doubleRangeContains(missingsRange, labels.asDoubles()[i])));
          categories.add(builder.build());
          i++;
        }
      }
    } catch (Exception e) {
      // ignore
      log.warn("Error while parsing variable categories: {}", colName, e);
    }
    return categories;
  }

  private boolean integerRangeContains(RServerResult range, int value) {
    if (range == null) return false;
    int min = range.asIntegers()[0];
    int max = range.asIntegers()[1];
    return value >= min && value <= max;
  }

  private boolean doubleRangeContains(RServerResult range, double value) {
    if (range == null) return false;
    double min = range.asDoubles()[0];
    double max = range.asDoubles()[1];
    return value >= min && value <= max;
  }

  private String normalizeCategoryName(String name) {
    return (isNumeric() || isInteger() || isDecimal()) && name.endsWith(".0") ? name.substring(0, name.length() - 2) : name;
  }

  private RServerResult extractAttribute(String attrName) {
    if (colAttr == null) return null;
    try {
      RNamedList<RServerResult> rList = colAttr.asNamedList();
      if (!rList.containsKey(attrName)) return null;
      return rList.get(attrName);
    } catch (Exception e) {
      return null;
    }
  }

  private List<Attribute> extractLocalizedAttributes(String namespace, String name, String value) {
    List<Attribute> attributes = Lists.newArrayList();
    if (Strings.isNullOrEmpty(value)) return attributes;

    String[] strValues = value.split("\\|");
    Pattern pattern = Pattern.compile("^\\(([a-z]{2})\\) (.+)", Pattern.DOTALL);
    for (String strValue : strValues) {
      Matcher matcher = pattern.matcher(strValue.trim());
      if (matcher.find()) {
        String localeStr = matcher.group(1);
        if (!RUtils.isLocaleValid(localeStr))
          localeStr = defaultLocale;
        int count = matcher.groupCount();
        String attrValue = matcher.group(2);
        attributes.add(Attribute.Builder.newAttribute(name).withNamespace(namespace)
            .withLocale(localeStr).withValue(attrValue).build());
      } else if (Strings.isNullOrEmpty(namespace) && ("label".equals(name) || "description".equals(name)))
        attributes.add(Attribute.Builder.newAttribute(name).withLocale(defaultLocale).withValue(strValue).build());
      else
        attributes.add(Attribute.Builder.newAttribute(name).withNamespace(namespace).withValue(strValue).build());
    }
    return attributes;
  }
}
