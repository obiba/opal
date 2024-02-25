/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.magma;

import org.obiba.magma.*;
import org.obiba.magma.support.AbstractValueTableWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * When performing a copy, instead of simply override the meta-data, make sure the destination variable attributes
 * that are not in the source variable, are not removed.
 */
public class MergingValueTable extends AbstractValueTableWrapper {

  private static final Logger log = LoggerFactory.getLogger(MergingValueTable.class);

  private final ValueTable wrapped;

  private final ValueTable destinationTable;

  public MergingValueTable(ValueTable wrapped, ValueTable destinationTable) {
    this.wrapped = wrapped;
    this.destinationTable = destinationTable;
  }

  @Override
  public ValueTable getWrappedValueTable() {
    return wrapped;
  }

  @Override
  public VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException {
    VariableValueSource wrappedVvs = super.getVariableValueSource(variableName);
    if (destinationTable == null || !destinationTable.hasVariable(variableName))
      return wrappedVvs;

    Variable destinationVariable = destinationTable.getVariable(variableName);
    return new MergeVariableValueSource(wrappedVvs, destinationVariable);
  }

  @Override
  public Variable getVariable(String name) throws NoSuchVariableException {
    return getVariableValueSource(name).getVariable();
  }

  @Override
  public Iterable<Variable> getVariables() {
    return StreamSupport.stream(super.getVariables().spliterator(), false)
        .map(var -> getVariableValueSource(var.getName()).getVariable())
        .collect(Collectors.toList());
  }

  private class MergeVariableValueSource extends AbstractVariableValueSource {

    private final VariableValueSource wrappedVvs;
    private final Variable destinationVariable;

    private final Variable mergedVariable;

    public MergeVariableValueSource(VariableValueSource wrappedVvs, Variable destinationVariable) {
      this.wrappedVvs = wrappedVvs;
      this.destinationVariable = destinationVariable;
      Variable wrappedVariable = wrappedVvs.getVariable();
      Variable.Builder builder = Variable.Builder.sameAs(destinationVariable);
      builder.overrideWith(wrappedVariable);
      this.mergedVariable = builder.build();
    }

    @NotNull
    @Override
    public Variable getVariable() {
      return mergedVariable;
    }

    @NotNull
    @Override
    public ValueType getValueType() {
      return wrappedVvs.getValueType();
    }

    @NotNull
    @Override
    public Value getValue(ValueSet valueSet) {
      return wrappedVvs.getValue(valueSet);
    }

    @Override
    public boolean supportVectorSource() {
      return wrappedVvs.supportVectorSource();
    }

    @NotNull
    @Override
    public VectorSource asVectorSource() throws VectorSourceNotSupportedException {
      return wrappedVvs.asVectorSource();
    }
  }
}
