package org.obiba.magma.r;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableValueSource;
import org.rosuda.REngine.REXP;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ParrallelVectorBuilder {

  private final Map<VariableValueSource, List<Value>> vectors = Maps.newLinkedHashMap();

  private final Map<VariableValueSource, String> symbols = Maps.newLinkedHashMap();

  private final Set<ValueSet> valueSets = Sets.newLinkedHashSet();

  public ParrallelVectorBuilder add(VariableValueSource variable, String symbol) {
    vectors.put(variable, new ArrayList<Value>());
    symbols.put(variable, symbol);
    return this;
  }

  public ParrallelVectorBuilder addValues(final Iterable<ValueSet> valueSets) {
    for(ValueSet valueSet : valueSets) {
      this.valueSets.add(valueSet);
    }
    return this;
  }

  public Map<String, REXP> build() {
    for(ValueSet valueSet : valueSets) {
      for(VariableValueSource variable : vectors.keySet()) {
        vectors.get(variable).add(variable.getValue(valueSet));
      }
    }

    Map<String, REXP> result = Maps.newLinkedHashMap();
    for(VariableValueSource variable : vectors.keySet()) {
      VectorType vt = VectorType.forValueType(variable.getValueType());
      REXP rexp = vt.asVector(vectors.get(variable));
      result.put(symbols.get(variable), rexp);
    }
    return result;
  }

}