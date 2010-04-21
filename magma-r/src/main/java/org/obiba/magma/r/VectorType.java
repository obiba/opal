package org.obiba.magma.r;

import java.util.Collection;

import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPString;

/**
 * A utility class for mapping {@code ValueType} to R {@code REXP}.
 */
public enum VectorType {

  ints(IntegerType.get()) {
    @Override
    public REXP asVector(Collection<Value> values) {
      int ints[] = new int[values.size()];
      int i = 0;
      for(Value value : values) {
        ints[i++] = value.isNull() ? REXPInteger.NA : ((Number) value.getValue()).intValue();
      }
      return new REXPInteger(ints);
    }
  },

  doubles(DecimalType.get()) {
    @Override
    public REXP asVector(Collection<Value> values) {
      double doubles[] = new double[values.size()];
      int i = 0;
      for(Value value : values) {
        doubles[i++] = value.isNull() ? REXPDouble.NA : ((Number) value.getValue()).doubleValue();
      }
      return new REXPDouble(doubles);
    }
  },

  dates(DateTimeType.get()) {
    @Override
    public REXP asVector(Collection<Value> values) {
      String strings[] = new String[values.size()];
      int i = 0;
      for(Value value : values) {
        strings[i++] = value.toString();
      }
      return new REXPString(strings);
    }
  },

  strings(TextType.get()) {
    @Override
    public REXP asVector(Collection<Value> values) {
      String strings[] = new String[values.size()];
      int i = 0;
      for(Value value : values) {
        strings[i++] = value.toString();
      }
      return new REXPString(strings);
    }
  };

  private ValueType type;

  private VectorType(ValueType type) {
    this.type = type;
  }

  public static VectorType forValueType(ValueType type) {
    for(VectorType v : VectorType.values()) {
      if(v.type == type) {
        return v;
      }
    }
    throw new IllegalArgumentException("No VectorType for ValueType " + type);
  }

  public abstract REXP asVector(Collection<Value> value);

}