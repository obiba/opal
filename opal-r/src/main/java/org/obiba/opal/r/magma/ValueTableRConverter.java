/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
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
import org.obiba.magma.support.SplitValueTablesFactory;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.spi.r.datasource.magma.MagmaRRuntimeException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPList;
import org.rosuda.REngine.RList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Build a R vector from a table: list of vectors of variables.
 */
abstract class ValueTableRConverter extends AbstractMagmaRConverter {

  private static final Logger log = LoggerFactory.getLogger(ValueTableRConverter.class);

  protected static final String VALUETABLE_LIST_SYMBOL = ".valuetablelist";

  private final Map<String, Integer> lineCounts = Maps.newConcurrentMap();

  ValueTableRConverter(MagmaAssignROperation magmaAssignROperation) {
    super(magmaAssignROperation);
  }

  protected boolean hasMultilines() {
    return !lineCounts.isEmpty();
  }

  @Override
  public void doAssign(String symbol, String path) {
    ValueTable table;
    if (magmaAssignROperation.hasValueTable()) table = magmaAssignROperation.getValueTable();
    else table = resolvePath(path);
    if (table == null) throw new IllegalStateException("Table must not be null");

    // if only table path was provided, it might be necessary to assign table by chunks
    doAssignTable(table, !magmaAssignROperation.hasValueTable());
  }

  /**
   * Make the table assignment, split it to assign it by chunks if necessary.
   *
   * @param table
   * @param split
   */
  protected void doAssignTable(ValueTable table, boolean split) {
    if (split) {
      List<ValueTable> splitTables = SplitValueTablesFactory.split(table);
      if (splitTables.size() > 1) {
        List<String> splitSymbols = Lists.newArrayList();
        int i = 1;
        for (ValueTable splitTable : splitTables) {
          String splitSymbol = "." + getSymbol() + "__" + i;
          doAssignTable(splitTable, splitSymbol);
          splitSymbols.add(splitSymbol);
          i++;
        }
        String args = Joiner.on("`,`").join(splitSymbols);
        magmaAssignROperation.doEnsurePackage("dplyr");
        magmaAssignROperation.doEval(String.format("is.null(base::assign('%s', dplyr::bind_rows(`%s`)))", getSymbol(), args));
        magmaAssignROperation.doEval(String.format("for (n in names(`%s`)) attributes(`%s`[[n]]) <- attributes(`%s`[[n]])", getSymbol(), getSymbol(), splitSymbols.get(0)));
        args = Joiner.on("','").join(splitSymbols);
        magmaAssignROperation.doEval(String.format("base::rm(list='%s')", args));
      } else {
        doAssignTable(table, getSymbol());
      }
    } else {
      doAssignTable(table, getSymbol());
    }
  }

  /**
   * Converter specific table assignment.
   *
   * @param table
   * @param symbol
   */
  protected abstract void doAssignTable(ValueTable table, String symbol);

  protected void doAssignTmpVectorsList(REXPList list) {
    magmaAssignROperation.doAssign(VALUETABLE_LIST_SYMBOL, list);
  }

  protected void doRemoveTmpVectorsList() {
    magmaAssignROperation.doEval("base::rm(" + VALUETABLE_LIST_SYMBOL + ")");
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

  protected List<VariableEntity> getEntities(ValueTable table) {
    return table.getVariableEntities();
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
  protected boolean withFactors() {
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

  protected REXP getIdsVector(ValueTable table, boolean withMissings) {
    return getUnaryVector(table, new VariableEntityValueSource(table, getIdColumnName()), withMissings);
  }

  protected REXP getUpdatedVector(ValueTable table, boolean withMissings) {
    return getUnaryVector(table, new ValueSetUpdatedValueSource(table, getUpdatedColumnName()), withMissings);
  }

  private REXP getUnaryVector(ValueTable table, VariableValueSource vvs, boolean withMissings) {
    List<VariableEntity> entities = getEntities(table);
    if (lineCounts.isEmpty())
      return getVector(vvs, entities, withMissings);
    else {
      List<Value> values = Lists.newArrayList();
      List<Value> vValues = ImmutableList.copyOf(vvs.asVectorSource().getValues(entities));
      List<String> ids = entities.stream().map(VariableEntity::getIdentifier).collect(Collectors.toList());

      for (int i = 0; i < ids.size(); i++) {
        Value val = vValues.get(i);
        String id = ids.get(i);
        if (lineCounts.containsKey(id)) {
          for (int j = 0; j < lineCounts.get(id); j++) {
            values.add(val);
          }
        } else {
          values.add(val);
        }
      }

      return getVector(vvs.getVariable(), values, entities, withMissings, false, false);
    }
  }

  /**
   * Parallelize vector extraction per value set as it is safe and optimal to do so for a view (some derive variables
   * can refer to each other in the same value set).
   *
   * @return
   */
  protected REXPList getVectorsList(ValueTable table) {
    List<REXP> contents = Lists.newArrayList();
    List<String> names = Lists.newArrayList();
    List<VariableEntity> entities = getEntities(table);
    Iterable<Variable> variables = filterVariables(table);
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
            for (int i = value.asSequence().getSize(); i < lineCounts.get(e.getIdentifier()); i++) {
              values.add(value.getValueType().nullValue());
            }
          } else {
            // repeat the non sequence value over the multiple lines
            for (int i = 0; i < lineCounts.get(e.getIdentifier()); i++) {
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

    // one temporary vector for the timestamp
    if (withUpdatedColumn()) {
      names.add(0, getUpdatedColumnName());
      contents.add(0, getUpdatedVector(table, withMissings()));
    }

    // one temporary vector for the ids
    if (withIdColumn()) {
      REXP ids = getIdsVector(table, withMissings());
      names.add(0, getIdColumnName());
      contents.add(0, ids);
    }

    return new REXPList(new RList(contents, names));
  }

  /**
   * Filter the variables by a select clause (if any).
   *
   * @return
   */
  private Iterable<Variable> filterVariables(ValueTable table) {
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

  protected ValueTable resolvePath(String path) {
    MagmaEngineReferenceResolver resolver = MagmaEngineTableResolver.valueOf(path);

    if (resolver.getDatasourceName() == null) {
      throw new MagmaRRuntimeException("Datasource is not defined in path: " + path);
    }
    Datasource ds = MagmaEngine.get().getDatasource(resolver.getDatasourceName());

    return applyIdentifiersMapping(ds.getValueTable(resolver.getTableName()));
  }

  /**
   * Represents the entity identifiers as values of a variable.
   */
  private class VariableEntityValueSource extends AbstractVariableValueSource implements VariableValueSource {

    private final Variable variable;

    VariableEntityValueSource(ValueTable table, String name) {
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
        public Iterable<Value> getValues(List<VariableEntity> entities) {
          return entities.stream().map(e -> TextType.get().valueOf(e.getIdentifier())).collect(Collectors.toList());
        }
      };
    }
  }

  private class ValueSetUpdatedValueSource extends AbstractVariableValueSource implements VariableValueSource {

    private final ValueTable table;

    private final Variable variable;

    ValueSetUpdatedValueSource(ValueTable table, String name) {
      this.table = table;
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
        public Iterable<Value> getValues(List<VariableEntity> entities) {
          return entities.stream().map(e -> table.getValueSet(e).getTimestamps().getLastUpdate()).collect(Collectors.toList());
        }
      };
    }
  }
}
