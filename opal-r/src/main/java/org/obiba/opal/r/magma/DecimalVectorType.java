package org.obiba.opal.r.magma;

import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.type.DecimalType;
import org.obiba.opal.r.magma.util.DoubleRange;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPList;

import java.util.List;
import java.util.stream.Collectors;

public class DecimalVectorType extends VectorType {

  public DecimalVectorType() {
    super(DecimalType.get());
  }

  @Override
  protected REXP asContinuousVector(Variable variable, List<Value> values, boolean withMissings, boolean withLabelled) {
    double doubles[] = new double[values.size()];
    int i = 0;
    for (Value value : values) {
      // OPAL-1536 do not push missings
      if (!withMissings && variable.isMissingValue(value) || value.isNull()) {
        doubles[i++] = REXPDouble.NA;
      } else {
        doubles[i++] = ((Number) value.getValue()).doubleValue();
      }
    }
    return variable == null ? new REXPDouble(doubles) : new REXPDouble(doubles, getVariableRAttributes(variable, null, withLabelled));
  }

  @Override
  protected REXP getCategoriesMissingRange(Variable variable, List<String> missingCats) {
    if (missingCats.size()<=3) return null; // spss allows a max of 3 discrete missings
    DoubleRange range = new DoubleRange(variable.getCategories().stream().map(Category::getName).collect(Collectors.toList()), missingCats);
    if (range.hasRange()) {
      return new REXPDouble(new double[] { range.getMin(), range.getMax() });
    }
    return null;
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
}
