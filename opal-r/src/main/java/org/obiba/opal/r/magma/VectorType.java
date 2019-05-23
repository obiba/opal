package org.obiba.opal.r.magma;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.*;
import org.obiba.magma.support.VariableNature;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.PolygonType;
import org.rosuda.REngine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VectorType {

  private static final Logger log = LoggerFactory.getLogger(VectorType.class);

  private final ValueType valueType;

  public VectorType(ValueType valueType) {
    this.valueType = valueType;
  }

  public ValueType getValueType() {
    return valueType;
  }

  /**
   * Build R vector.
   *
   * @param vvs
   * @param entities
   * @param withMissings if true, values corresponding to missing categories will be pushed to the vector
   * @return
   */
  public REXP asVector(VariableValueSource vvs, List<VariableEntity> entities, boolean withMissings) {
    return asVector(vvs.getVariable(), ImmutableList.copyOf(vvs.asVectorSource().getValues(entities)), entities, withMissings, true, false);
  }

  /**
   * Build R vector from directly provided values (in entities order).
   *
   * @param variable
   * @param values
   * @param entities
   * @param withMissings
   * @param withFactors
   * @param withLabelled
   * @return
   */
  public REXP asVector(Variable variable, List<Value> values, List<VariableEntity> entities, boolean withMissings, boolean withFactors, boolean withLabelled) {
    return asValuesVector(variable, values, withMissings, withFactors, withLabelled);
  }

  /**
   * Build a type specific R vector. Default behaviour is to check the variable if it defines categories
   *
   * @param variable
   * @param values
   * @param withMissings
   * @param withFactors
   * @param withLabelled
   * @return
   */
  protected REXP asValuesVector(Variable variable, List<Value> values, boolean withMissings, boolean withFactors, boolean withLabelled) {
    if (variable.getValueType().equals(BooleanType.get())) {
      return asContinuousVector(variable, values, withMissings, withLabelled);
    }
    return VariableNature.CATEGORICAL.equals(VariableNature.getNature(variable)) && withFactors ?
        asFactors(variable, values, withMissings) : asContinuousVector(variable, values, withMissings, withLabelled);
  }

  protected REXP asContinuousVector(Variable variable, List<Value> values, boolean withMissings, boolean withLabelled) {
    return variable != null && variable.getValueType().isGeo() ? asGeoJSONValuesVector(variable, values, withLabelled) : asStringValuesVector(variable, values, withLabelled);
  }

  protected REXP asFactors(Variable variable, List<Value> values, boolean withMissings) {
    List<String> levels = Lists.newArrayList();
    Map<String, Integer> codes = Maps.newHashMap();
    populateCodesAndLevels(variable, withMissings, codes, levels);

    int ints[] = new int[values.size()];
    int i = 0;
    for (Value value : values) {
      Integer code = null;
      if (withMissings || !variable.isMissingValue(value)) {
        String str = value.toString();
        code = codes.get(str);
        if (code == null && value.getValueType().equals(DecimalType.get()) && str != null && str.endsWith(".0")) {
          code = codes.get(str.substring(0, str.length() - 2));
        }
      }
      ints[i] = code != null ? code : REXPInteger.NA;
      i++;
    }
    String[] levelsArray = levels.toArray(new String[levels.size()]);
    return new REXPFactor(ints, levelsArray, getVariableRAttributes(variable, levelsArray, false));
  }

  private void populateCodesAndLevels(Variable variable, boolean withMissings, Map<String, Integer> codes,
                                      List<String> levels) {
    // REXPFactor is one-based. That is, the ID the first level, is 1.
    int i = 1;
    for (Category c : variable.getCategories()) {
      if (withMissings || !c.isMissing()) {
        levels.add(c.getName());
        codes.put(c.getName(), i++);
      }
    }
  }

  /**
   * Build a R vector of strings.
   *
   * @param variable
   * @param values
   * @param withLabelled
   * @return
   */
  protected REXP asStringValuesVector(Variable variable, List<Value> values, boolean withLabelled) {
    String strs[] = new String[values.size()];
    int i = 0;
    for (Value value : values) {
      String str = value.toString();
      strs[i] = str != null && !str.isEmpty() ? str : null;
      i++;
    }

    return variable == null ? new REXPString(strs) : new REXPString(strs, getVariableRAttributes(variable, null, withLabelled));
  }

  /**
   * Build a R vector of strings representing GeoJSON values (see https://tools.ietf.org/html/rfc7946).
   *
   * @param variable
   * @param values
   * @param withLabelled
   * @return
   */
  protected REXP asGeoJSONValuesVector(Variable variable, List<Value> values, boolean withLabelled) {
    String strs[] = new String[values.size()];
    String type = "Point";
    if (variable.getValueType().equals(LineStringType.get())) type = "LineString";
    else if (variable.getValueType().equals(PolygonType.get())) type = "Polygon";
    int i = 0;
    for (Value value : values) {
      String str = value.toString();
      if (str == null || str.isEmpty())
        strs[i] = null;
      else
        strs[i] = "{\"type\":\"" + type + "\",\"coordinates\":" + str + "}";
      i++;
    }
    return new REXPString(strs, getVariableRAttributes(variable, null, withLabelled));
  }

  /**
   * Build the R attributes from a variable. If levels are provided, factor attributes will added in place of
   * labelled ones for describing the categories.
   *
   * @param variable
   * @param levels
   * @param withLabelled
   * @return
   */
  protected REXPList getVariableRAttributes(Variable variable, String[] levels, boolean withLabelled) {
    List<REXP> contents = Lists.newArrayList();
    List<String> names = Lists.newArrayList();
    asAttributesMap(variable).forEach((name, content) -> {
      if (!name.equals("class")) { // exclude class attribute as it is an interpreted by R
        names.add(name);
        contents.add(new REXPString(content));
      }
      // to help haven R package to write spss or stata formats
      if (name.equals("spss::format")) {
        names.add("format.spss");
        contents.add(new REXPString(content));
      } else if (name.equals("stata::format")) {
        names.add("format.stata");
        contents.add(new REXPString(content));
      }
    });

    if (levels != null) {
      names.add("class");
      contents.add(new REXPString("factor"));
      names.add("levels");
      contents.add(new REXPString(levels));
    } else if (variable.hasCategories() && withLabelled) {
      List<String> missingCats = Lists.newArrayList();
      for (Category cat : variable.getCategories()) {
        if (cat.isMissing()) {
          missingCats.add(cat.getName());
        }
      }
      if (!missingCats.isEmpty()) {
        names.add("class");
        contents.add(new REXPString(new String[]{"haven_labelled_spss", "haven_labelled"}));
        REXP naRange = getCategoriesMissingRange(variable, missingCats);
        if (naRange != null) {
          names.add("na_range");
          contents.add(naRange);
        }
        // add discrete missing values after na_range as the missingCats may have been modified
        if (!missingCats.isEmpty()) {
          if (naRange != null && missingCats.size() > 1) {
            log.warn("Variable {}: SPSS format does not support more than one discrete missing value in addition to a missing values range.", variable.getName());
          }
          names.add("na_values");
          contents.add(getCategoriesRAttributes(variable, missingCats, null));
        }
      } else {
        names.add("class");
        contents.add(new REXPString(new String[]{"haven_labelled"}));
      }
      names.add("labels");
      contents.add(getCategoriesRAttributes(variable));
    } else {
      addTypeRAttributes(variable, names, contents);
    }

    RList attrs = new RList(contents, names);

    return new REXPList(attrs);
  }

  /**
   * Allow adding type specific R attributes.
   *
   * @param variable
   * @param names
   * @param contents
   */
  protected void addTypeRAttributes(Variable variable, List<String> names, List<REXP> contents) {
    // no-op
  }

  /**
   * Try to identify a range of missings, and optionally set one discrete value.
   *
   * @param variable
   * @param missingCats Known discrete missing values, can be modified after a range has been identified
   * @return
   */
  protected REXP getCategoriesMissingRange(Variable variable, List<String> missingCats) {
    return null;
  }

  /**
   * Build the R attributes that describe the categories of the variable.
   *
   * @param variable
   * @return
   */
  protected REXP getCategoriesRAttributes(Variable variable) {
    List<String> contents = Lists.newArrayList();
    List<String> names = Lists.newArrayList();
    variable.getCategories().forEach(cat -> {
      contents.add(cat.getName());
      names.add(getLabel(cat));
    });
    return getCategoriesRAttributes(variable, contents, new REXPList(
        new RList(
            new REXP[]{
                new REXPString(names.toArray(new String[names.size()]))
            }, new String[]{"names"})));
  }

  /**
   * Get the R attributes from category labels. To be overridden to match the R data type.
   *
   * @param variable
   * @param labels
   * @param attr
   * @return
   */
  protected REXP getCategoriesRAttributes(Variable variable, List<String> labels, REXPList attr) {
    return new REXPString(labels.toArray(new String[labels.size()]), attr);
  }

  /**
   * Extract the category label.
   *
   * @param category
   * @return The label or the name if label is not found.
   */
  protected String getLabel(Category category) {
    Map<String, String> attributesMap = asAttributesMap(category);
    return attributesMap.containsKey("label") ? attributesMap.get("label") : category.getName();
  }

  /**
   * Merge the {@link Attribute} namespace and name as the map key, and the locale and value as the map value.
   *
   * @param attributeAware
   * @return
   */
  protected Map<String, String> asAttributesMap(AttributeAware attributeAware) {
    // per namespace::name, per locale
    Map<String, Map<String, String>> attributesMap = Maps.newHashMap();
    if (attributeAware.hasAttributes()) {
      for (Attribute attr : attributeAware.getAttributes()) {
        if (Strings.isNullOrEmpty(attr.getValue().toString())) continue;
        String name = attr.getName();
        if (attr.hasNamespace()) name = attr.getNamespace() + "::" + name;
        if (!attributesMap.containsKey(name)) {
          attributesMap.put(name, Maps.newHashMap());
        }
        attributesMap.get(name).put(attr.isLocalised() ? attr.getLocale().toLanguageTag() : "", attr.getValue().toString());
      }
    }
    // per namespace::name
    Map<String, String> rval = Maps.newHashMap();
    attributesMap.forEach((name, localeMap) -> {
      String content = localeMap.entrySet()
          .stream()
          .map(entry -> (Strings.isNullOrEmpty(entry.getKey()) ? "" : "(" + entry.getKey() + ") ") + entry.getValue())
          .collect(Collectors.joining(" | "));
      rval.put(name, content);
    });
    return rval;
  }

  @Override
  public String toString() {
    return this.getClass().getName() + ":" + valueType.toString();
  }
}
