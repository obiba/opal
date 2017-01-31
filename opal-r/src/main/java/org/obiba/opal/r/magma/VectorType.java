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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.*;
import org.obiba.magma.type.*;
import org.obiba.opal.core.domain.VariableNature;
import org.obiba.opal.r.MagmaRRuntimeException;
import org.rosuda.REngine.*;

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
    protected REXP asContinuousVector(Variable variable, int size, Iterable<Value> values, boolean withMissings) {
      byte bools[] = new byte[size];
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
      return variable == null ? new REXPLogical(bools) : new REXPLogical(bools, getVariableRAttributes(variable, null));
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
    protected REXP asContinuousVector(Variable variable, int size, Iterable<Value> values, boolean withMissings) {
      int ints[] = new int[size];
      int i = 0;
      for (Value value : values) {
        // OPAL-1536 do not push missings
        if (!withMissings && variable.isMissingValue(value) || value.isNull()) {
          ints[i++] = REXPInteger.NA;
        } else {
          ints[i++] = ((Number) value.getValue()).intValue();
        }
      }
      return variable == null ? new REXPInteger(ints) : new REXPInteger(ints, getVariableRAttributes(variable, null));
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
    protected REXP asContinuousVector(Variable variable, int size, Iterable<Value> values, boolean withMissings) {
      double doubles[] = new double[size];
      int i = 0;
      for (Value value : values) {
        // OPAL-1536 do not push missings
        if (!withMissings && variable.isMissingValue(value) || value.isNull()) {
          doubles[i++] = REXPDouble.NA;
        } else {
          doubles[i++] = ((Number) value.getValue()).doubleValue();
        }
      }
      return variable == null ? new REXPDouble(doubles) : new REXPDouble(doubles, getVariableRAttributes(variable, null));
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

  datetimes(DateTimeType.get()),

  dates(DateType.get()),

  locales(LocaleType.get()),

  strings(TextType.get()),

  binaries(BinaryType.get()) {
    @Override
    protected REXP asContinuousVector(Variable variable, int size, Iterable<Value> values, boolean withMissings) {
      REXPRaw raws[] = new REXPRaw[size];
      int i = 0;
      for (Value value : values) {
        raws[i++] = new REXPRaw(value.isNull() ? null : (byte[]) value.getValue());
      }
      return variable == null ? new REXPList(new RList(raws)) : new REXPList(new RList(raws), getVariableRAttributes(variable, null));
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
    return asVector(vvs.getVariable(), vvs.asVectorSource().getValues(entities), entities, withMissings, true);
  }

  /**
   * Build R vector from directly provided values (in entities order).
   *
   * @param variable
   * @param values
   * @param entities
   * @param withMissings
   * @return
   */
  public REXP asVector(Variable variable, Iterable<Value> values, SortedSet<VariableEntity> entities, boolean withMissings, boolean withFactors) {
    int size = entities.size();
    return variable.isRepeatable()
        ? asValueSequencesVector(variable, size, values, withMissings, withFactors)
        : asValuesVector(variable, size, values, withMissings, withFactors);
  }

  /**
   * Build a type specific R vector. Default behaviour is to check the variable if it defines categories
   *
   * @param variable
   * @param size
   * @param values
   * @param withMissings
   * @return
   */
  protected REXP asValuesVector(Variable variable, int size, Iterable<Value> values, boolean withMissings, boolean withFactors) {
    return VariableNature.CATEGORICAL.equals(VariableNature.getNature(variable)) && withFactors ?
        asFactors(variable, size, values, withMissings) : asContinuousVector(variable, size, values, withMissings);
  }

  protected REXP asContinuousVector(Variable variable, int size, Iterable<Value> values, boolean withMissings) {
    return asStringValuesVector(variable, size, values);
  }

  /**
   * Build a list of R vectors.
   *
   * @param variable
   * @param size
   * @param values
   * @param withMissings
   * @return
   */
  private REXP asValueSequencesVector(Variable variable, int size, Iterable<Value> values, boolean withMissings, boolean withFactors) {
    REXP sequences[] = new REXP[size];
    int i = 0;
    for (Value value : values) {
      ValueSequence seq = value.asSequence();
      sequences[i++] = seq.isNull() ? null : asContinuousVector(null, seq.getSize(), seq.getValue(), withMissings);
    }
    return new REXPList(new RList(sequences), getVariableRAttributes(variable, null));
  }

  protected REXP asFactors(Variable variable, int size, Iterable<Value> values, boolean withMissings) {
    List<String> levels = Lists.newArrayList();
    Map<String, Integer> codes = Maps.newHashMap();
    populateCodesAndLevels(variable, withMissings, codes, levels);

    int ints[] = new int[size];
    int i = 0;
    for (Value value : values) {
      if (i >= size) {
        throw new IllegalStateException("unexpected value");
      }

      Integer code = null;
      if (withMissings || !variable.isMissingValue(value)) {
        String str = value.toString();
        code = codes.get(str);
      }
      ints[i] = code != null ? code : REXPInteger.NA;
      i++;
    }
    String[] levelsArray = levels.toArray(new String[levels.size()]);
    return new REXPFactor(ints, levelsArray, getVariableRAttributes(variable, levelsArray));
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
   * @param size
   * @param values
   * @return
   */
  protected REXP asStringValuesVector(Variable variable, int size, Iterable<Value> values) {
    String strs[] = new String[size];
    int i = 0;
    for (Value value : values) {
      if (i >= size) {
        throw new IllegalStateException("unexpected value");
      }

      String str = value.toString();
      strs[i] = str != null && !str.isEmpty() ? str : null;
      i++;
    }

    return variable == null ? new REXPString(strs) : new REXPString(strs, getVariableRAttributes(variable, null));
  }

  /**
   * Build the R attributes from a variable. If levels are provided, factor attributes will added in place of
   * labelled ones for describing the categories.
   *
   * @param variable
   * @param levels
   * @return
   */
  protected REXPList getVariableRAttributes(Variable variable, String[] levels) {
    List<REXP> contents = Lists.newArrayList();
    List<String> names = Lists.newArrayList();
    asAttributesMap(variable).forEach((name, content) -> {
      names.add(name);
      contents.add(new REXPString(content));
    });

    if (levels != null) {
      names.add("class");
      contents.add(new REXPString("factor"));
      names.add("levels");
      contents.add(new REXPString(levels));
    } else if (variable.hasCategories()) {
      names.add("class");
      contents.add(new REXPString("labelled"));
      names.add("labels");
      contents.add(getCategoriesRAttributes(variable));
    }

    RList attrs = new RList(contents, names);

    return new REXPList(attrs);
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
          .collect(Collectors.joining("; "));
      rval.put(name, content);
    });
    return rval;
  }
}
