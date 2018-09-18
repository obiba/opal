package org.obiba.opal.r.magma;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BinaryType;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPList;
import org.rosuda.REngine.REXPRaw;
import org.rosuda.REngine.RList;

import java.util.List;

public class BinaryVectorType extends VectorType {

  public BinaryVectorType() {
    super(BinaryType.get());
  }

  @Override
  protected REXP asContinuousVector(Variable variable, List<Value> values, boolean withMissings, boolean withLabelled) {
    REXPRaw raws[] = new REXPRaw[values.size()];
    int i = 0;
    for (Value value : values) {
      raws[i++] = new REXPRaw(value.isNull() ? null : (byte[]) value.getValue());
    }
    return variable == null ? new REXPList(new RList(raws)) : new REXPList(new RList(raws), getVariableRAttributes(variable, null, withLabelled));
  }
}
