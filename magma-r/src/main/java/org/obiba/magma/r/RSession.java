package org.obiba.magma.r;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineEvalException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;

import com.google.common.collect.Maps;

/**
 * Interface between magma and R
 */
public class RSession {

  final REXP environment;

  public RSession() {
    this.environment = MagmaEngine.get().getExtension(MagmaRExtension.class).newEnvironment();
  }

  public void close() throws REngineException, REXPMismatchException {
    getEngine().finalizeReference(this.environment);
  }

  /**
   * Attaches the {@code ValueTable} into the R environment. The resulting list of vectors is attached to the value
   * table's name.
   * @param vt the table to attach
   * @throws REngineException
   * @throws REXPMismatchException
   */
  public void attach(ValueTable vt) throws REngineException, REXPMismatchException {
    REngine R = getEngine();

    ParrallelVectorBuilder vectorBuilder = new ParrallelVectorBuilder();

    for(Variable variable : vt.getVariables()) {
      if(variable.isRepeatable() || variable.getValueType() == BinaryType.get()) {
        continue;
      }
      vectorBuilder.prepare(variable);
    }

    for(ValueSet vs : vt.getValueSets()) {
      // Create
      for(Variable variable : vectorBuilder.variables()) {
        Value v = vt.getValue(variable, vs);
        if(v == null) throw new IllegalStateException("value is null for variable " + variable.getName() + " entity " + vs.getVariableEntity().getIdentifier());
        if(v.isNull() == false && v.getValue() == null) throw new IllegalStateException("value's value is null for variable " + variable.getName() + " entity " + vs.getVariableEntity().getIdentifier());
        vectorBuilder.push(variable, v);
      }
    }

    R.assign(vt.getName(), vectorBuilder.toR(), environment);
  }

  /**
   * Sends a command to R
   * @param cmd
   * @throws REngineException
   * @throws REXPMismatchException
   */
  public REXP eval(String cmd) throws REngineException, REXPMismatchException {
    REngine R = MagmaEngine.get().getExtension(MagmaRExtension.class).getEngine();
    try {
      REXP result = R.parseAndEval(cmd, environment, false);
      return result;
    } catch(REngineEvalException e) {
      throw e;
    }
  }

  public static String toString(REXP result) throws REXPMismatchException {
    if(result.isList()) {
      RList list = result.asList();
      StringBuilder sb = new StringBuilder();
      for(int i = 0; i < list.size(); i++) {
        sb.append(list.keyAt(i)).append(" == ").append(toString(list.at(i))).append('\n');
      }
      return sb.toString();
    } else if(result.isString()) {
      return Arrays.toString(result.asStrings());
    } else if(result.isInteger()) {
      return Arrays.toString(result.asIntegers());
    } else if(result.isNumeric()) {
      return Arrays.toString(result.asDoubles());
    } else {
      return result.toString();
    }
  }

  private REngine getEngine() {
    return MagmaEngine.get().getExtension(MagmaRExtension.class).getEngine();
  }

  private enum VectorType {

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

  private class ParrallelVectorBuilder {

    private final Map<Variable, Collection<Value>> vectors = Maps.newHashMap();

    public Set<Variable> variables() {
      return vectors.keySet();
    }

    public REXP toR() {
      final RList list = new RList();
      for(Variable variable : vectors.keySet()) {
        VectorType vt = VectorType.forValueType(variable.getValueType());
        REXP rexp = vt.asVector(vectors.get(variable));
        list.add(rexp);
        list.setKeyAt(list.size() - 1, variable.getName().replaceAll("-", "."));
      }
      return new REXPGenericVector(list);
    }

    public void prepare(Variable variable) {
      vectors.put(variable, new ArrayList<Value>());
    }

    public void push(Variable variable, Value value) {
      vectors.get(variable).add(value);
    }
  }
}
