package org.obiba.opal.r.magma;

import org.obiba.magma.MagmaDate;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.type.DateType;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPString;

import java.util.Date;
import java.util.List;

public class DateVectorType extends VectorType {

  public DateVectorType() {
    super(DateType.get());
  }

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
}
