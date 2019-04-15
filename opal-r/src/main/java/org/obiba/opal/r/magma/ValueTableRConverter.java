/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.obiba.opal.r.magma;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.*;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.spi.r.datasource.magma.MagmaRRuntimeException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.RList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Build a R vector from a table: list of vectors of variables.
 */
abstract class ValueTableRConverter extends AbstractMagmaRConverter {

  private static final Logger log = LoggerFactory.getLogger(ValueTableRConverter.class);

  private ValueTable table;

  private final Map<String, Integer> lineCounts = Maps.newConcurrentMap();

  ValueTableRConverter(MagmaAssignROperation magmaAssignROperation) {
    super(magmaAssignROperation);
  }

  protected boolean hasMultilines() {
    return !lineCounts.isEmpty();
  }

  protected void doAssignTmpVectors(REXP ids, String[] names, RList list) {
    // one temporary vector per variable
    for (String name : names) {
      magmaAssignROperation.doAssign(getTmpVectorName(getSymbol(), name), list.at(name));
    }
    // one temporary vector for the timestamp
    if (withUpdatedColumn()) {
      magmaAssignROperation.doAssign(getTmpVectorName(getSymbol(), getUpdatedColumnName()),
          getUpdatedVector(withMissings()));
    }
    // one temporary vector for the ids
    magmaAssignROperation.doAssign(getTmpVectorName(getSymbol(),
        withIdColumn() ? getIdColumnName() : "row.names"), ids);
  }


  protected void doRemoveTmpVectors(String... names) {
    // remove temporary vectors
    List<String> vectorNames = Lists.newArrayList();
    for (String name : names) {
      vectorNames.add(getTmpVectorName(getSymbol(), name));
    }
    magmaAssignROperation.doEval("base::rm(list=c('" + Joiner.on("','").join(vectorNames) + "'))");
    magmaAssignROperation.doEval("base::rm(" + getTmpVectorName(getSymbol(),
        withIdColumn() ? getIdColumnName() : "row.names") + ")");
    if (withUpdatedColumn()) {
      magmaAssignROperation.doEval("base::rm(" + getTmpVectorName(getSymbol(),
          getUpdatedColumnName()) + ")");
    }
  }

  protected String getTmpVectorName(String symbol, String name) {
    return (".opal__" + symbol + "__" + name).replace("@", ".").replace("-", ".").replace("+", ".").replace(" ", ".").replace("\"", ".")
        .replace("'", ".").replace("[", ".").replace("]", ".").replace("{", ".").replace("}", ".").replace("(", ".").replace(")", ".");
  }

  protected String getSymbol() {
    return magmaAssignROperation.getSymbol();
  }

  protected boolean withIdColumn() {
    return magmaAssignROperation.withIdColumn();
  }
  
  protected String getIdColumnName() {
    return magmaAssignROperation.getIdColumnName();
  }

  protected boolean withUpdatedColumn() {
    return magmaAssignROperation.withUpdatedColumn();
  }

  protected String getUpdatedColumnName() {
    return magmaAssignROperation.getUpdatedColumnName();
  }
  
  protected SortedSet<VariableEntity> getEntities() {
    return magmaAssignROperation.getEntities();
  }

  protected List<VariableEntity> getEntitiesAsList() {
    return ImmutableList.copyOf(magmaAssignROperation.getEntities());
  }

  /**
   * Assign values matching categories with "missing" flag.
   *
   * @return
   */
  protected boolean withMissings() {
    return magmaAssignROperation.withMissings();
  }

  /**
   * R class "factor".
   *
   * @return
   */
  protected boolean  withFactors() {
    return true;
  }

  /**
   * R class "labelled".
   *
   * @return
   */
  protected boolean withLabelled() {
    return false;
  }

  protected REXP getIdsVector(boolean withMissings) {
    return getUnaryVector(new VariableEntityValueSource(getIdColumnName()), withMissings);
  }

  protected REXP getUpdatedVector(boolean withMissings) {
    return getUnaryVector(new ValueSetUpdatedValueSource(getUpdatedColumnName()), withMissings);
  }

  private REXP getUnaryVector(VariableValueSource vvs, boolean withMissings) {
    if (lineCounts.isEmpty())
      return getVector(vvs, getEntities(), withMissings);
    else {
      List<Value> values = Lists.newArrayList();
      List<Value> vValues = ImmutableList.copyOf(vvs.asVectorSource().getValues(getEntities()));
      List<String> ids = getEntitiesAsList().stream().map(VariableEntity::getIdentifier).collect(Collectors.toList());

      for (int i=0; i<ids.size(); i++) {
        Value val = vValues.get(i);
        String id = ids.get(i);
        if (lineCounts.containsKey(id)) {
          for (int j=0; j<lineCounts.get(id); j++) {
            values.add(val);
          }
        } else {
          values.add(val);
        }
      }

      return getVector(vvs.getVariable(), values, getEntities(), withMissings, false, false);
    }
  }

  protected RList getVariableVectors() {
    return getVariableVectorsByRows();
  }

  protected void setValueTable(ValueTable table) {
    this.table = table;
  }

  protected ValueTable getValueTable() {
    return table;
  }

  /**
   * Parallelize vector extraction per value set as it is safe and optimal to do so for a view (some derive variables
   * can refer to each other in the same value set).
   *
   * @return
   */
  protected RList getVariableVectorsByRows() {
    List<REXP> contents = Lists.newArrayList();
    List<String> names = Lists.newArrayList();
    SortedSet<VariableEntity> entities = getEntities();
    Iterable<Variable> variables = filterVariables();
    Map<String, Map<String, Value>> variableValues = Maps.newConcurrentMap();
    variables.forEach(variable -> variableValues.put(variable.getName(), Maps.newConcurrentMap()));

    // OPAL-3013 do not parallelize value set extraction
    for (ValueSet valueSet : table.getValueSets(entities)) {
      magmaAssignROperation.getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          variables.forEach(variable -> {
            String identifier = valueSet.getVariableEntity().getIdentifier();
            Value value = table.getValue(variable, valueSet);
            if (variable.isRepeatable()) {
              int seqSize = value.asSequence().getSize();
              if (!lineCounts.containsKey(identifier)) {
                lineCounts.put(identifier, seqSize);
              } else {
                lineCounts.put(identifier, Math.max(lineCounts.get(identifier), seqSize));
              }
            }
            variableValues.get(variable.getName()).put(identifier, value);
          });
        }
      });
    }

    // vector for each variable, values in the same order as entities
    variables.forEach(v -> {
      Map<String, Value> entityValueMap = variableValues.get(v.getName());
      List<Value> values = Lists.newArrayListWithExpectedSize(entities.size());

      entities.forEach(e -> {
        Value value = entityValueMap.get(e.getIdentifier());
        if (lineCounts.containsKey(e.getIdentifier())) {
          if (value.isSequence()) {
            values.addAll(value.asSequence().getValues());
            for (int i = value.asSequence().getSize(); i<lineCounts.get(e.getIdentifier()); i++) {
              values.add(value.getValueType().nullValue());
            }
          } else {
            // repeat the non sequence value over the multiple lines
            for (int i=0; i<lineCounts.get(e.getIdentifier()); i++) {
              values.add(value);
            }
          }
        } else {
          values.add(value);
        }
      });

      contents.add(getVector(v, values, entities, withMissings(), withFactors(), withLabelled()));
      names.add(v.getName());
    });

    return new RList(contents, names);
  }

  /**
   * Filter the variables by a select clause (if any).
   *
   * @return
   */
  private Iterable<Variable> filterVariables() {
    List<Variable> filteredVariables;
    List<Variable> allVariables = StreamSupport.stream(table.getVariables().spliterator(), false)
      .filter(v -> !BinaryType.get().equals(v.getValueType())).collect(Collectors.toList());

    if (Strings.isNullOrEmpty(magmaAssignROperation.getVariableFilter())) {
      filteredVariables = allVariables;
    } else {
      JavascriptClause jsClause = new JavascriptClause(magmaAssignROperation.getVariableFilter());
      jsClause.initialise();
      filteredVariables = allVariables.stream().filter(v -> jsClause.select(v)).collect(Collectors.toList());
    }
    Collections.sort(filteredVariables, (o1, o2) -> o1.getIndex() - o2.getIndex());
    return filteredVariables;
  }

  protected void resolvePath(String path) {
    MagmaEngineReferenceResolver resolver = MagmaEngineTableResolver.valueOf(path);

    if (resolver.getDatasourceName() == null) {
      throw new MagmaRRuntimeException("Datasource is not defined in path: " + path);
    }
    Datasource ds = MagmaEngine.get().getDatasource(resolver.getDatasourceName());

    table = applyIdentifiersMapping(ds.getValueTable(resolver.getTableName()));
  }

  /**
   * Represents the entity identifiers as values of a variable.
   */
  private class VariableEntityValueSource extends AbstractVariableValueSource implements VariableValueSource {

    private final Variable variable;

    VariableEntityValueSource(String name) {
      this.variable = Variable.Builder.newVariable(Strings.isNullOrEmpty(name) ? "opal_id" : name, TextType.get(), table.getEntityType()).build();
    }

    @Override
    public Variable getVariable() {
      return variable;
    }

    @NotNull
    @Override
    public ValueType getValueType() {
      return TextType.get();
    }

    @NotNull
    @Override
    public Value getValue(ValueSet valueSet) {
      return TextType.get().valueOf(valueSet.getVariableEntity().getIdentifier());
    }

    @Override
    public boolean supportVectorSource() {
      return true;
    }

    @NotNull
    @Override
    public VectorSource asVectorSource() {
      return new VectorSource() {
        @Override
        public ValueType getValueType() {
          return TextType.get();
        }

        @Override
        public Iterable<Value> getValues(SortedSet<VariableEntity> entities) {
          return entities.stream().map(e -> TextType.get().valueOf(e.getIdentifier())).collect(Collectors.toList());
        }
      };
    }
  }

  private class ValueSetUpdatedValueSource extends AbstractVariableValueSource implements VariableValueSource {

    private final Variable variable;

    ValueSetUpdatedValueSource(String name) {
      this.variable = Variable.Builder.newVariable(name, DateTimeType.get(), table.getEntityType()).build();
    }

    @Override
    public Variable getVariable() {
      return variable;
    }

    @NotNull
    @Override
    public ValueType getValueType() {
      return DateTimeType.get();
    }

    @NotNull
    @Override
    public Value getValue(ValueSet valueSet) {
      return valueSet.getTimestamps().getLastUpdate();
    }

    @Override
    public boolean supportVectorSource() {
      return true;
    }

    @NotNull
    @Override
    public VectorSource asVectorSource() {
      return new VectorSource() {
        @Override
        public ValueType getValueType() {
          return DateTimeType.get();
        }

        @Override
        public Iterable<Value> getValues(SortedSet<VariableEntity> entities) {
          return entities.stream().map(e -> table.getValueSet(e).getTimestamps().getLastUpdate()).collect(Collectors.toList());
        }
      };
    }
  }
}
