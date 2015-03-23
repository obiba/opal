/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.r;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.domain.VariableNature;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPFactor;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPList;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPRaw;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A utility class for mapping {@code ValueType} to R {@code REXP}.
 */
public enum VectorType {

  booleans(BooleanType.get()) {
    @Override
    protected REXP asContinuousVector(Variable variable, int size, Iterable<Value> values, boolean withMissings) {
      byte bools[] = new byte[size];
      int i = 0;
      for(Value value : values) {
        // OPAL-1536 do not push missings
        if(!withMissings && variable.isMissingValue(value) || value.isNull()) {
          bools[i++] = REXPLogical.NA;
        } else if((Boolean) value.getValue()) {
          bools[i++] = REXPLogical.TRUE;
        } else {
          bools[i++] = REXPLogical.FALSE;
        }
      }
      return new REXPLogical(bools);
    }

  },

  ints(IntegerType.get()) {
    @Override
    protected REXP asContinuousVector(Variable variable, int size, Iterable<Value> values, boolean withMissings) {
      int ints[] = new int[size];
      int i = 0;
      for(Value value : values) {
        // OPAL-1536 do not push missings
        if(!withMissings && variable.isMissingValue(value) || value.isNull()) {
          ints[i++] = REXPInteger.NA;
        } else {
          ints[i++] = ((Number) value.getValue()).intValue();
        }
      }
      return new REXPInteger(ints);
    }

  },

  doubles(DecimalType.get()) {
    @Override
    protected REXP asContinuousVector(Variable variable, int size, Iterable<Value> values, boolean withMissings) {
      double doubles[] = new double[size];
      int i = 0;
      for(Value value : values) {
        // OPAL-1536 do not push missings
        if(!withMissings && variable.isMissingValue(value) || value.isNull()) {
          doubles[i++] = REXPDouble.NA;
        } else {
          doubles[i++] = ((Number) value.getValue()).doubleValue();
        }
      }
      return new REXPDouble(doubles);
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
      for(Value value : values) {
        raws[i++] = new REXPRaw(value.isNull() ? null : (byte[]) value.getValue());
      }
      return new REXPList(new RList(raws));
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
    for(VectorType v : VectorType.values()) {
      if(v.type == type) {
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
    Variable variable = vvs.getVariable();
    int size = entities.size();
    Iterable<Value> values = vvs.asVectorSource().getValues(entities);
    return variable.isRepeatable()
        ? asValueSequencesVector(variable, size, values, withMissings)
        : asValuesVector(variable, size, values, withMissings);
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
  protected REXP asValuesVector(Variable variable, int size, Iterable<Value> values, boolean withMissings) {
    if(BooleanType.get().equals(variable.getValueType()))
      return asContinuousVector(variable, size, values, withMissings);
    switch(VariableNature.getNature(variable)) {
      case CATEGORICAL:
        return asFactors(variable, size, values, withMissings);
    }
    return asContinuousVector(variable, size, values, withMissings);
  }

  protected REXP asContinuousVector(Variable variable, int size, Iterable<Value> values, boolean withMissings) {
    return asStringValuesVector(size, values);
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
  private REXP asValueSequencesVector(Variable variable, int size, Iterable<Value> values, boolean withMissings) {
    REXP sequences[] = new REXP[size];
    int i = 0;
    for(Value value : values) {
      ValueSequence seq = value.asSequence();
      sequences[i++] = seq.isNull() ? null : asValuesVector(variable, seq.getSize(), seq.getValue(), withMissings);
    }
    return new REXPList(new RList(sequences));
  }

  protected REXP asFactors(Variable variable, int size, Iterable<Value> values, boolean withMissings) {
    List<String> levels = Lists.newArrayList();
    Map<String, Integer> codes = Maps.newHashMap();
    populateCodesAndLevels(variable, withMissings, codes, levels);

    int ints[] = new int[size];
    int i = 0;
    for(Value value : values) {
      if(i >= size) {
        throw new IllegalStateException("unexpected value");
      }

      Integer code = null;
      if(withMissings || !variable.isMissingValue(value)) {
        String str = value.toString();
        code = codes.get(str);
      }
      ints[i] = code != null ? code : REXPInteger.NA;
      i++;
    }
    return new REXPFactor(ints, levels.toArray(new String[levels.size()]));
  }

  private void populateCodesAndLevels(Variable variable, boolean withMissings, Map<String, Integer> codes,
      List<String> levels) {
    // REXPFactor is one-based. That is, the ID the first level, is 1.
    int i = 1;
    for(Category c : variable.getCategories()) {
      if(withMissings || !c.isMissing()) {
        levels.add(c.getName());
        codes.put(c.getName(), i++);
      }
    }
  }

  /**
   * Build a R vector of strings.
   *
   * @param variable
   * @param size
   * @param values
   * @return
   */
  protected REXP asStringValuesVector(int size, Iterable<Value> values) {
    String strs[] = new String[size];
    int i = 0;
    for(Value value : values) {
      if(i >= size) {
        throw new IllegalStateException("unexpected value");
      }

      String str = value.toString();
      strs[i] = str != null && str.length() > 0 ? str : null;
      i++;
    }

    return new REXPString(strs);
  }
}
