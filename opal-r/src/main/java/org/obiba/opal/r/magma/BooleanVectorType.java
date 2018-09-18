package org.obiba.opal.r.magma;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BooleanType;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPList;
import org.rosuda.REngine.REXPLogical;

import java.util.List;

public class BooleanVectorType extends VectorType {

  public BooleanVectorType() {
    super(BooleanType.get());
  }

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
}
