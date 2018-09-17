/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.r.magma;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.*;
import org.obiba.magma.support.VariableNature;
import org.obiba.magma.type.*;
import org.obiba.opal.r.MagmaRRuntimeException;
import org.obiba.opal.r.magma.util.DoubleRange;
import org.obiba.opal.r.magma.util.IntegerRange;
import org.rosuda.REngine.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * A utility class for mapping {@code ValueType} to R {@code REXP}.
 */
public enum VectorType {

  booleans(BooleanType.get()) {
    @Override
    protected REXP asContinuousVector(Variable variable, List<Value> values, boolean withMissings, boolean withLabelled) {
      byte bools[] = new byte[values.size()];
      int i = 0;
      for (Value value : values) {
        // OPAL-1536 do not push missings
        if (!withMissings && variable.isMissingValue(value) || value.isNull()) {
          bools[i++] = REXPLogical.NA;
        } else if ((Boolean) value.getValue()) {
          bools[i++] = REXPLogical.TRUE;
        } else {
          bools[i++] = REXPLogical.FALSE;
        }
      }
      return variable == null ? new REXPLogical(bools) : new REXPLogical(bools, getVariableRAttributes(variable, null, withLabelled));
    }

    @Override
    protected REXP getCategoriesRAttributes(Variable variable, List<String> labels, REXPList attr) {
      byte bools[] = new byte[labels.size()];
      for (int i=0; i<labels.size(); i++) {
        try {
          bools[i] = Boolean.parseBoolean(labels.get(i)) ? REXPLogical.TRUE : REXPLogical.FALSE;
        } catch (NumberFormatException e) {
          bools[i] = REXPLogical.NA;
        }
      }
      return new REXPLogical(bools, attr);
    }
  },

  ints(IntegerType.get()) {
    @Override
    protected REXP asContinuousVector(Variable variable, List<Value> values, boolean withMissings, boolean withLabelled) {
      int ints[] = new int[values.size()];
      int i = 0;
      for (Value value : values) {
        // OPAL-1536 do not push missings
        if (!withMissings && variable.isMissingValue(value) || value.isNull()) {
          ints[i++] = REXPInteger.NA;
        } else {
          ints[i++] = ((Number) value.getValue()).intValue();
        }
      }
      return variable == null ? new REXPInteger(ints) : new REXPInteger(ints, getVariableRAttributes(variable, null, withLabelled));
    }

    @Override
    protected REXP getCategoriesMissingRange(Variable variable, List<String> missingCats) {
      if (missingCats.size()<=3) return null; // spss allows a max of 3 discrete missings
      IntegerRange range = new IntegerRange(variable.getCategories().stream().map(Category::getName).collect(Collectors.toList()), missingCats);
      if (range.hasRange()) {
        return new REXPInteger(new int[] { range.getMin(), range.getMax() });
      }
      return null;
    }

    @Override
    protected REXP getCategoriesRAttributes(Variable variable, List<String> labels, REXPList attr) {
      int ints[] = new int[labels.size()];
      for (int i=0; i<labels.size(); i++) {
        try {
          ints[i] = Integer.parseInt(labels.get(i));
        } catch (NumberFormatException e) {
          ints[i] = REXPInteger.NA;
        }
      }
      return new REXPInteger(ints, attr);
    }

  },

  doubles(DecimalType.get()) {
    @Override
    protected REXP asContinuousVector(Variable variable, List<Value> values, boolean withMissings, boolean withLabelled) {
      double doubles[] = new double[values.size()];
      int i = 0;
      for (Value value : values) {
        // OPAL-1536 do not push missings
        if (!withMissings && variable.isMissingValue(value) || value.isNull()) {
          doubles[i++] = REXPDouble.NA;
        } else {
          doubles[i++] = ((Number) value.getValue()).doubleValue();
        }
      }
      return variable == null ? new REXPDouble(doubles) : new REXPDouble(doubles, getVariableRAttributes(variable, null, withLabelled));
    }

    @Override
    protected REXP getCategoriesMissingRange(Variable variable, List<String> missingCats) {
      if (missingCats.size()<=3) return null; // spss allows a max of 3 discrete missings
      DoubleRange range = new DoubleRange(variable.getCategories().stream().map(Category::getName).collect(Collectors.toList()), missingCats);
      if (range.hasRange()) {
        return new REXPDouble(new double[] { range.getMin(), range.getMax() });
      }
      return null;
    }

    @Override
    protected REXP getCategoriesRAttributes(Variable variable, List<String> labels, REXPList attr) {
      double doubles[] = new double[labels.size()];
      for (int i=0; i<labels.size(); i++) {
        try {
          doubles[i] = Double.parseDouble(labels.get(i));
        } catch (NumberFormatException e) {
          doubles[i] = REXPDouble.NA;
        }
      }
      return new REXPDouble(doubles, attr);
    }
  },

  datetimes(DateTimeType.get()) {
    @Override
    protected REXP asContinuousVector(Variable variable, List<Value> values, boolean withMissings, boolean withLabelled) {
      int ints[] = new int[values.size()];
      int i = 0;
      for (Value value : values) {
        // do not support categories in this type
        if (value.isNull()) {
          ints[i++] = REXPInteger.NA;
        } else {
          Date d = (Date) value.getValue();
          double t = ((double)d.getTime()) / 1000;
          ints[i++] = Long.valueOf(Math.round(t)).intValue();
        }
      }
      return variable == null ? new REXPInteger(ints) : new REXPInteger(ints, getVariableRAttributes(variable, null, withLabelled));
    }

    @Override
    protected void addTypeRAttributes(Variable variable, List<String> names, List<REXP> contents) {
      names.add("class");
      contents.add(new REXPString(new String[] {"POSIXct", "POSIXt"}));
      if (!names.contains("tzone")) {
        names.add("tzone");
        contents.add(new REXPString("UTC"));
      }
    }
  },

  dates(DateType.get()) {
    @Override
    protected REXP asContinuousVector(Variable variable, List<Value> values, boolean withMissings, boolean withLabelled) {
      int ints[] = new int[values.size()];
      int i = 0;
      for (Value value : values) {
        // do not support categories in this type
        if (value.isNull()) {
          ints[i++] = REXPInteger.NA;
        } else {
          Object val = value.getValue();
          Date date;
          if (val instanceof MagmaDate) {
            date = ((MagmaDate)val).asDate();
          } else {
            date = (Date) val;
          }
          double d = ((double)date.getTime()) / (24 * 3600 * 1000);
          ints[i++] = Long.valueOf(Math.round(d)).intValue();
        }
      }
      return variable == null ? new REXPInteger(ints) : new REXPInteger(ints, getVariableRAttributes(variable, null, withLabelled));
    }

    @Override
    protected void addTypeRAttributes(Variable variable, List<String> names, List<REXP> contents) {
      names.add("class");
      contents.add(new REXPString("Date"));
    }
  },

  locales(LocaleType.get()),

  strings(TextType.get()),

  binaries(BinaryType.get()) {
    @Override
    protected REXP asContinuousVector(Variable variable, List<Value> values, boolean withMissings, boolean withLabelled) {
      REXPRaw raws[] = new REXPRaw[values.size()];
      int i = 0;
      for (Value value : values) {
        raws[i++] = new REXPRaw(value.isNull() ? null : (byte[]) value.getValue());
      }
      return variable == null ? new REXPList(new RList(raws)) : new REXPList(new RList(raws), getVariableRAttributes(variable, null, withLabelled));
    }
  },

  points(PointType.get()),

  linestrings(LineStringType.get()),

  polygons(PolygonType.get());

  private final ValueType type;

  VectorType(ValueType type) {
    this.type = type;
  }

  public static VectorType forValueType(ValueType type) {
    for (VectorType v : VectorType.values()) {
      if (v.type == type) {
        return v;
      }
    }
    throw new MagmaRRuntimeException("No VectorType for ValueType " + type);
  }

  /**
   * Build R vector.
   *
   * @param vvs
   * @param entities
   * @param withMissings if true, values corresponding to missing categories will be pushed to the vector
   * @return
   */
  public REXP asVector(VariableValueSource vvs, SortedSet<VariableEntity> entities, boolean withMissings) {
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
  public REXP asVector(Variable variable, List<Value> values, SortedSet<VariableEntity> entities, boolean withMissings, boolean withFactors, boolean withLabelled) {
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
        contents.add(new REXPString(new String[] { "labelled_spss", "labelled" }));
        REXP naRange = getCategoriesMissingRange(variable, missingCats);
        if (naRange != null) {
          names.add("na_range");
          contents.add(naRange);
        }
        // add discrete missing values after na_range as the missingCats may have been modified
        if (!missingCats.isEmpty()) {
          names.add("na_values");
          contents.add(getCategoriesRAttributes(variable, missingCats, null));
        }
      } else {
        names.add("class");
        contents.add(new REXPString("labelled"));
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
}
