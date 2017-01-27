/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.obiba.opal.r.magma;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.*;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.r.MagmaRRuntimeException;
import org.rosuda.REngine.*;
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
class ValueTableRConverter extends AbstractMagmaRConverter {

  private static final Logger log = LoggerFactory.getLogger(ValueTableRConverter.class);

  private ValueTable table;

  ValueTableRConverter(MagmaAssignROperation magmaAssignROperation) {
    super(magmaAssignROperation);
  }

  @Override
  public boolean canResolve(String path) {
    return path != null && path.contains(".") && !path.contains(":");
  }

  @Override
  public REXP asVector(String path, boolean withMissings, String identifiersMapping) {
    resolvePath(path, identifiersMapping);
    return asVector(withMissings);
  }

  /**
   * Build a R vector from an already set ValueTable.
   *
   * @param withMissings
   * @return
   */
  private REXP asVector(boolean withMissings) {
    if (table == null) throw new IllegalStateException("Table must not be null");
    magmaAssignROperation.setEntities(table);
    REXP ids = getIdsVector(withMissings);
    return createDataFrame(ids);
  }

  @Override
  public void doAssign(String symbol, String path, boolean withMissings, String identifiersMapping) {
    // OPAL-2710 assigning a data.frame directly fails with a lot of rows
    resolvePath(path, identifiersMapping);
    if (table == null) throw new IllegalStateException("Table must not be null");
    magmaAssignROperation.setEntities(table);
    REXP ids = getIdsVector(withMissings);
    RList list = getVariableVectors();

    String[] names = list.keys();
    if (names == null || names.length == 0) return;

    doAssignTmpVectors(ids, names, list);
    doAssignDataFrame(names);
    doRemoveTmpVectors(names);
  }

  private void doAssignTmpVectors(REXP ids, String[] names, RList list) {
    // one temporary vector per variable
    for (String name : names) {
      magmaAssignROperation.doAssign(getTmpVectorName(magmaAssignROperation.getSymbol(), name), list.at(name));
    }
    if (magmaAssignROperation.withUpdatedColumn()) {
      magmaAssignROperation.doAssign(getTmpVectorName(magmaAssignROperation.getSymbol(), magmaAssignROperation.getUpdatedColumnName()),
          getUpdatedVector(magmaAssignROperation.withMissings()));
    }
    // one temporary vector for the ids
    magmaAssignROperation.doAssign(getTmpVectorName(magmaAssignROperation.getSymbol(),
        magmaAssignROperation.withIdColumn() ? magmaAssignROperation.getIdColumnName() : "row.names"), ids);
  }

  private void doAssignDataFrame(String... names) {
    // create the data.frame from the vectors
    StringBuilder args = new StringBuilder();
    if (magmaAssignROperation.withIdColumn()) {
      args.append(String.format("'%s'=%s", magmaAssignROperation.getIdColumnName(),
          getTmpVectorName(magmaAssignROperation.getSymbol(), magmaAssignROperation.getIdColumnName())));
    }

    if (magmaAssignROperation.withUpdatedColumn()) {
      if (args.length() > 0) args.append(", ");
      args.append(String.format("'%s'=%s", magmaAssignROperation.getUpdatedColumnName(),
          getTmpVectorName(magmaAssignROperation.getSymbol(), magmaAssignROperation.getUpdatedColumnName())));
    }
    for (String name : names) {
      if (args.length() > 0) args.append(", ");
      args.append(String.format("'%s'=%s", name, getTmpVectorName(magmaAssignROperation.getSymbol(), name)));
    }
    if (!magmaAssignROperation.withIdColumn())
      args.append(String.format(", row.names=%s", getTmpVectorName(magmaAssignROperation.getSymbol(), "row.names")));
    log.info("data.frame arguments: {}", args);
    magmaAssignROperation.doEval(String.format("base::assign('%s', data.frame(%s, stringsAsFactors=FALSE))", magmaAssignROperation.getSymbol(), args));
  }

  private void doRemoveTmpVectors(String... names) {
    // remove temporary vectors
    for (String name : names) {
      magmaAssignROperation.doEval("base::rm(" + getTmpVectorName(magmaAssignROperation.getSymbol(), name) + ")");
    }
    magmaAssignROperation.doEval("base::rm(" + getTmpVectorName(magmaAssignROperation.getSymbol(),
        magmaAssignROperation.withIdColumn() ? magmaAssignROperation.getIdColumnName() : "row.names") + ")");
    if (magmaAssignROperation.withUpdatedColumn()) {
      magmaAssignROperation.doEval("base::rm(" + getTmpVectorName(magmaAssignROperation.getSymbol(),
          magmaAssignROperation.getUpdatedColumnName()) + ")");
    }
  }

  private String getTmpVectorName(String symbol, String name) {
    return ("opal__" + symbol + "__" + name).replace("-", ".").replace("+", ".").replace(" ", ".").replace("\"", ".")
        .replace("'", ".");
  }

  private REXP getIdsVector(boolean withMissings) {
    return getVector(new VariableEntityValueSource(magmaAssignROperation.getIdColumnName()), magmaAssignROperation.getEntities(), withMissings);
  }

  private REXP getUpdatedVector(boolean withMissings) {
    return getVector(new ValueSetUpdatedValueSource(magmaAssignROperation.getUpdatedColumnName()), magmaAssignROperation.getEntities(), withMissings);
  }

  private RList getVariableVectors() {
    return table.isView() ? getVariableVectorsFromView() : getVariableVectorsFromRawTable();
  }

  /**
   * Parallelize vector extraction per variable as it is safe and optimal to do so for a raw table.
   *
   * @return
   */
  private RList getVariableVectorsFromRawTable() {
    List<REXP> contents = Collections.synchronizedList(Lists.newArrayList());
    List<String> names = Collections.synchronizedList(Lists.newArrayList());

    // vector for each variable
    StreamSupport.stream(filterVariables().spliterator(), true) //
        .map(v -> table.getVariableValueSource(v.getName())) //
        .filter(vvs -> !vvs.getVariable().isRepeatable()) //
        .forEach(vvs ->
            magmaAssignROperation.getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
              @Override
              protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                contents.add(getVector(vvs, magmaAssignROperation.getEntities(), magmaAssignROperation.withMissings()));
                names.add(vvs.getVariable().getName());
              }
            })
        );

    return new RList(contents, names);
  }

  /**
   * Parallelize vector extraction per value set as it is safe and optimal to do so for a view (some derive variables
   * can refer to each other in the same value set).
   *
   * @return
   */
  private RList getVariableVectorsFromView() {
    List<REXP> contents = Lists.newArrayList();
    List<String> names = Lists.newArrayList();
    SortedSet<VariableEntity> entities = magmaAssignROperation.getEntities();
    Iterable<Variable> variables = filterVariables();
    Map<String, Map<String, Value>> variableValues = Maps.newConcurrentMap();
    variables.forEach(variable -> variableValues.put(variable.getName(), Maps.newConcurrentMap()));

    // parallelize value set extraction
    StreamSupport.stream(table.getValueSets(entities).spliterator(), true) //
        .forEach(valueSet ->
            variables.forEach(variable -> {
              Value value = table.getValue(variable, valueSet);
              variableValues.get(variable.getName()).put(valueSet.getVariableEntity().getIdentifier(), value);
            })
        );

    // vector for each variable, values in the same order as entities
    variables.forEach(v -> {
      Map<String, Value> entityValueMap = variableValues.get(v.getName());
      List<Value> values = entities.stream().map(e -> entityValueMap.get(e.getIdentifier())).collect(Collectors.toList());
      contents.add(getVector(v, values, entities, magmaAssignROperation.withMissings()));
      names.add(v.getName());
    });

    return new RList(contents, names);
  }

  /**
   * Get the non repeatable variables filtered by a select clause (if any).
   *
   * @return
   */
  private Iterable<Variable> filterVariables() {
    List<Variable> filteredVariables;
    List<Variable> nonRepeatableVariables = StreamSupport.stream(table.getVariables().spliterator(), false) //
        .filter(v -> !v.isRepeatable()).collect(Collectors.toList());

    if (Strings.isNullOrEmpty(magmaAssignROperation.getVariableFilter())) {
      filteredVariables = nonRepeatableVariables;
    } else {
      JavascriptClause jsClause = new JavascriptClause(magmaAssignROperation.getVariableFilter());
      jsClause.initialise();
      filteredVariables = nonRepeatableVariables.stream().filter(v -> jsClause.select(v)).collect(Collectors.toList());
    }
    return filteredVariables;
  }

  /**
   * @param ids
   * @return
   */
  private REXP createDataFrame(REXP ids) {
    return new REXPGenericVector(null, new REXPList(new RList( //
        new REXP[]{ //
            new REXPString("data.frame"), //
            ids}, //
        new String[]{ //
            "class", //
            "row.names"})));
  }

  private void resolvePath(String path, String idMapping) {
    MagmaEngineReferenceResolver resolver = MagmaEngineTableResolver.valueOf(path);

    if (resolver.getDatasourceName() == null) {
      throw new MagmaRRuntimeException("Datasource is not defined in path: " + path);
    }
    Datasource ds = MagmaEngine.get().getDatasource(resolver.getDatasourceName());

    table = applyIdentifiersMapping(ds.getValueTable(resolver.getTableName()), idMapping);
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
