package org.obiba.opal.r.magma;

import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.type.IntegerType;
import org.obiba.opal.r.magma.util.IntegerRange;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPList;

import java.util.List;
import java.util.stream.Collectors;

public class IntegerVectorType extends VectorType {

  public IntegerVectorType() {
    super(IntegerType.get());
  }

  @Override
  protected REXP asContinuousVector(Variable variable, List<Value> values, boolean withMissings, boolean withLabelled) {
    int ints[] = new int[values.size()];
    int i = 0;
    for (Value value : values) {
      // OPAL-1536 do not push missings
      if (!withMissings && variable.isMissingValue(value) || value.isNull()) {
        ints[i++] = REXPInteger.NA;
      } else {
        ints[i++] = ((Number) value.getValue()).intValue();
      }
    }
    return variable == null ? new REXPInteger(ints) : new REXPInteger(ints, getVariableRAttributes(variable, null, withLabelled));
  }

  @Override
  protected REXP getCategoriesMissingRange(Variable variable, List<String> missingCats) {
    if (missingCats.size()<=3) return null; // spss allows a max of 3 discrete missings
    IntegerRange range = new IntegerRange(variable.getCategories().stream().map(Category::getName).collect(Collectors.toList()), missingCats);
    if (range.hasRange()) {
      return new REXPInteger(new int[] { range.getMin(), range.getMax() });
    }
    return null;
  }

  @Override
  protected REXP getCategoriesRAttributes(Variable variable, List<String> labels, REXPList attr) {
    int ints[] = new int[labels.size()];
    for (int i=0; i<labels.size(); i++) {
      try {
        ints[i] = Integer.parseInt(labels.get(i));
      } catch (NumberFormatException e) {
        ints[i] = REXPInteger.NA;
      }
    }
    return new REXPInteger(ints, attr);
  }
}
