package org.obiba.opal.r;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.TextType;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPList;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPRaw;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;

/**
 * A utility class for mapping {@code ValueType} to R {@code REXP}.
 */
public enum VectorType {

  booleans(BooleanType.get()) {
    @Override
    protected REXP asValuesVector(int size, Iterable<Value> values) {
      byte booleans[] = new byte[size];
      int i = 0;
      for(Value value : values) {
        if(value.isNull()) {
          booleans[i++] = REXPLogical.NA;
        } else if((Boolean) value.getValue()) {
          booleans[i++] = REXPLogical.TRUE;
        } else {
          booleans[i++] = REXPLogical.FALSE;
        }
      }
      return new REXPLogical(booleans);
    }

  },

  ints(IntegerType.get()) {
    @Override
    protected REXP asValuesVector(int size, Iterable<Value> values) {
      int ints[] = new int[size];
      int i = 0;
      for(Value value : values) {
        ints[i++] = value.isNull() ? REXPInteger.NA : ((Number) value.getValue()).intValue();
      }
      return new REXPInteger(ints);
    }
  },

  doubles(DecimalType.get()) {
    @Override
    protected REXP asValuesVector(int size, Iterable<Value> values) {
      double doubles[] = new double[size];
      int i = 0;
      for(Value value : values) {
        doubles[i++] = value.isNull() ? REXPDouble.NA : ((Number) value.getValue()).doubleValue();
      }
      return new REXPDouble(doubles);
    }
  },

  datetimes(DateTimeType.get()) {
    @Override
    protected REXP asValuesVector(int size, Iterable<Value> values) {
      String strings[] = new String[size];
      int i = 0;
      for(Value value : values) {
        strings[i++] = value.toString();
      }
      return new REXPString(strings);
    }
  },

  dates(DateType.get()) {
    @Override
    protected REXP asValuesVector(int size, Iterable<Value> values) {
      String strings[] = new String[size];
      int i = 0;
      for(Value value : values) {
        strings[i++] = value.toString();
      }
      return new REXPString(strings);
    }
  },

  locales(LocaleType.get()) {
    @Override
    protected REXP asValuesVector(int size, Iterable<Value> values) {
      String strings[] = new String[size];
      int i = 0;
      for(Value value : values) {
        strings[i++] = value.toString();
      }
      return new REXPString(strings);
    }
  },

  strings(TextType.get()) {
    @Override
    protected REXP asValuesVector(int size, Iterable<Value> values) {
      String strings[] = new String[size];
      int i = 0;
      for(Value value : values) {
        strings[i++] = value.toString();
      }
      return new REXPString(strings);
    }
  },

  binaries(BinaryType.get()) {
    @Override
    protected REXP asValuesVector(int size, Iterable<Value> values) {
      REXPRaw raws[] = new REXPRaw[size];
      int i = 0;
      for(Value value : values) {
        raws[i++] = new REXPRaw((byte[]) value.getValue());
      }
      return new REXPList(new RList(raws));
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
    throw new MagmaRRuntimeException("No VectorType for ValueType " + type);
  }

  protected abstract REXP asValuesVector(int size, Iterable<Value> values);

  /**
   * Build R vector from values.
   * @param repeatable true if values are {@link ValueSequence}
   * @param size number of values (including null values)
   * @param values
   * @return the R vector
   */
  public REXP asVector(boolean repeatable, int size, Iterable<Value> values) {
    if(repeatable) {
      return asValueSequencesVector(size, values);
    } else
      return asValuesVector(size, values);
  }

  private REXP asValueSequencesVector(int size, Iterable<Value> values) {
    REXP sequences[] = new REXP[size];
    int i = 0;
    for(Value value : values) {
      ValueSequence seq = value.asSequence();
      sequences[i++] = seq.isNull() ? null : asValuesVector(seq.getSize(), seq.getValue());
    }
    return new REXPList(new RList(sequences));
  }

}