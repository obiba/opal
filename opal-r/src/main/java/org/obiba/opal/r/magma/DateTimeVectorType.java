package org.obiba.opal.r.magma;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.type.DateTimeType;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPString;

import java.util.Date;
import java.util.List;

public class DateTimeVectorType extends VectorType {

  public DateTimeVectorType() {
    super(DateTimeType.get());
  }

  @Override
  protected REXP asContinuousVector(Variable variable, List<Value> values, boolean withMissings, boolean withLabelled) {
    double doubles[] = new double[values.size()];
    int i = 0;
    for (Value value : values) {
      // do not support categories in this type
      if (value.isNull()) {
        doubles[i++] = REXPInteger.NA;
      } else {
        Date d = (Date) value.getValue();
        double t = ((double)d.getTime()) / 1000;
        doubles[i++] = t;
      }
    }
    return variable == null ? new REXPDouble(doubles) : new REXPDouble(doubles, getVariableRAttributes(variable, null, withLabelled));
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
}
