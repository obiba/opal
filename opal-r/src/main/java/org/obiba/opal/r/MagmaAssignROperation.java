/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.r;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import org.obiba.magma.*;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.magma.IdentifiersMappingView;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.rosuda.REngine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Assign Magma values (from a datasource, a table or a variable) to a R symbol.
 */
public class MagmaAssignROperation extends AbstractROperation {

  private static final Logger log = LoggerFactory.getLogger(MagmaAssignROperation.class);

  private static final String ID_COLUMN = "opal_id";

  private static final String CREATED_COLUMN = "opal_created";

  private static final String UPDATED_COLUMN = "opal_updated";

  @NotNull
  private final IdentifiersTableService identifiersTableService;

  @NotNull
  private final String symbol;

  @NotNull
  private final String path;

  private final String variableFilter;

  private final boolean withMissings;

  private final String identifiersMapping;

  private final boolean withIdColumn;

  private final boolean withTimestampsColumns;

  private SortedSet<VariableEntity> entities;

  private final Set<MagmaRConverter> magmaRConverters = Sets
      .newHashSet((MagmaRConverter) new ValueTableRConverter(), (MagmaRConverter) new VariableRConverter());

  @SuppressWarnings("ConstantConditions")
  public MagmaAssignROperation(@NotNull String symbol, @NotNull String path, String variableFilter,
      boolean withMissings, boolean withIdentifiers, boolean withTimestamps, String identifiersMapping, @NotNull IdentifiersTableService identifiersTableService) {
    if(symbol == null) throw new IllegalArgumentException("symbol cannot be null");
    if(path == null) throw new IllegalArgumentException("path cannot be null");
    if(identifiersTableService == null) throw new IllegalArgumentException("identifiers table service cannot be null");
    this.symbol = symbol;
    this.path = path;
    this.variableFilter = variableFilter;
    this.withMissings = withMissings;
    this.identifiersMapping = identifiersMapping;
    this.withIdColumn = withIdentifiers;
    this.withTimestampsColumns = withTimestamps;
    this.identifiersTableService = identifiersTableService;
  }

  @Override
  public void doWithConnection() {
    try {
      for(MagmaRConverter converter : magmaRConverters) {
        if(converter.canResolve(path)) {
          converter.doAssign(symbol, path, withMissings, identifiersMapping);
          return;
        }
      }
    } catch(MagmaRuntimeException e) {
      throw new MagmaRRuntimeException("Failed resolving path '" + path + "'", e);
    }
    throw new MagmaRRuntimeException("No R converter found for path '" + path + "'");
  }

  private SortedSet<VariableEntity> getEntities() {
    if(entities == null) throw new IllegalStateException("call setEntities() first");
    return entities;
  }

  private void setEntities(Collection<VariableEntity> entities) {
    this.entities = ImmutableSortedSet.copyOf(entities);
  }

  void prepareEntities(ValueTable table) {
    setEntities(ImmutableSortedSet.copyOf(table.getVariableEntities()));
  }

  @Override
  public String toString() {
    return symbol + " <- opal[" + path + "]";
  }

  //
  // Magma R Convectors
  //

  /**
   * Provides a R vector from a Magma fully qualified path.
   */
  private interface MagmaRConverter {

    /**
     * Build a R vector from the Magma fully-qualified path.
     *
     * @param path
     * @param withMissings
     * @param identifiersMapping
     * @return
     */
    REXP asVector(String path, boolean withMissings, String identifiersMapping);

    /**
     * Check if path can be resolved as a datasource, table or variable.
     *
     * @param path
     * @return
     */
    boolean canResolve(String path);

    void doAssign(String symbol, String path, boolean withMissings, String identifiersMapping);

  }

  /**
   * Base implementation of Magma vector providers.
   */
  private abstract class AbstractMagmaRConverter implements MagmaRConverter {

    protected REXP getVector(VariableValueSource vvs, SortedSet<VariableEntity> entities, boolean withMissings) {
      VectorType vt = VectorType.forValueType(vvs.getValueType());
      return vt.asVector(vvs, entities, withMissings);
    }

    protected REXP getVector(Variable variable, Iterable<Value> values, SortedSet<VariableEntity> entities, boolean withMissings) {
      VectorType vt = VectorType.forValueType(variable.getValueType());
      return vt.asVector(variable, values, entities, withMissings);
    }

    protected ValueTable applyIdentifiersMapping(ValueTable table, String idMapping) {
      // If the table contains an entity that requires identifiers separation, create a "identifers view" of the table (replace
      // public (system) identifiers with private identifiers).
      if(!Strings.isNullOrEmpty(idMapping) &&
          identifiersTableService.hasIdentifiersMapping(table.getEntityType(), idMapping)) {
        // Make a view that converts opal identifiers to unit identifiers
        return new IdentifiersMappingView(idMapping, IdentifiersMappingView.Policy.UNIT_IDENTIFIERS_ARE_PUBLIC, table,
            identifiersTableService.getIdentifiersTable(table.getEntityType()));
      }
      return table;
    }
  }

  /**
   * Build a R vector from a table: list of vectors of variables.
   */
  private class ValueTableRConverter extends AbstractMagmaRConverter {

    private static final String ENTITY_ID_SYMBOL = "ID__";

    private ValueTable table;

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
    REXP asVector(boolean withMissings) {
      if(table == null) throw new IllegalStateException("Table must not be null");
      prepareEntities(table);
      REXP ids = getIdsVector(withMissings);
      return createDataFrame(ids);
    }

    @Override
    public void doAssign(String symbol, String path, boolean withMissings, String identifiersMapping) {
      // OPAL-2710 assigning a data.frame directly fails with a lot of rows
      resolvePath(path, identifiersMapping);
      if(table == null) throw new IllegalStateException("Table must not be null");
      prepareEntities(table);
      REXP ids = getIdsVector(withMissings);
      RList list = getVariableVectors();

      String[] names = list.keys();
      if(names == null || names.length == 0) return;

      doAssignTmpVectors(ids, names, list);
      doAssignDataFrame(names);
      doRemoveTmpVectors(names);
    }

    private void doAssignTmpVectors(REXP ids, String[] names, RList list) {
      // one temporary vector per variable
      for(String name : names) {
        assign(getTmpVectorName(symbol, name), list.at(name));
      }
      if (withTimestampsColumns) {
        assign(getTmpVectorName(symbol, CREATED_COLUMN), getCreatedVector(withMissings));
        assign(getTmpVectorName(symbol, UPDATED_COLUMN), getUpdatedVector(withMissings));
      }
      // one temporary vector for the ids
      assign(getTmpVectorName(symbol, withIdColumn ? ID_COLUMN : "row.names"), ids);
    }

    private void doAssignDataFrame(String... names) {
      // create the data.frame from the vectors
      StringBuilder args = new StringBuilder();
      if (withIdColumn)
        args.append("'").append(ID_COLUMN).append("'=").append(getTmpVectorName(symbol, ID_COLUMN));
      if (withTimestampsColumns) {
        if(args.length() > 0) args.append(", ");
        args.append("'").append(CREATED_COLUMN).append("'=").append(getTmpVectorName(symbol, CREATED_COLUMN));
        args.append(", '").append(UPDATED_COLUMN).append("'=").append(getTmpVectorName(symbol, UPDATED_COLUMN));
      }
      for(String name : names) {
        if(args.length() > 0) args.append(", ");
        args.append("'").append(name).append("'=").append(getTmpVectorName(symbol, name));
      }
      if (!withIdColumn)
        args.append(", row.names=").append(getTmpVectorName(symbol, "row.names"));
      log.info("data.frame arguments: {}", args);
      eval(symbol + " <- data.frame(" + args + ")", false);
    }

    private void doRemoveTmpVectors(String... names) {
      // remove temporary vectors
      for(String name : names) {
        eval("base::rm(" + getTmpVectorName(symbol, name) + ")", false);
      }
      eval("base::rm(" + getTmpVectorName(symbol, withIdColumn ? ID_COLUMN : "row.names") + ")", false);
    }

    private String getTmpVectorName(String symbol, String name) {
      return ("opal__" + symbol + "__" + name).replace("-", ".").replace("+", ".").replace(" ", ".").replace("\"", ".")
          .replace("'", ".");
    }

    private REXP getIdsVector(boolean withMissings) {
      return getVector(new VariableEntityValueSource(), getEntities(), withMissings);
    }

    private REXP getCreatedVector(boolean withMissings) {
      return getVector(new ValueSetCreatedValueSource(), getEntities(), withMissings);
    }

    private REXP getUpdatedVector(boolean withMissings) {
      return getVector(new ValueSetUpdatedValueSource(), getEntities(), withMissings);
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
          .forEach(vvs -> {
            contents.add(getVector(vvs, getEntities(), withMissings));
            names.add(vvs.getVariable().getName());
          });

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
      SortedSet<VariableEntity> entities = getEntities();
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
        contents.add(getVector(v, values, entities, withMissings));
        names.add(v.getName());
      });

      return new RList(contents, names);
    }

    /**
     * Get the non repeatable variables filtered by a select clause (if any).
     *
     * @return
     */
    protected Iterable<Variable> filterVariables() {
      List<Variable> filteredVariables;
      List<Variable> nonRepeatableVariables = StreamSupport.stream(table.getVariables().spliterator(), false) //
      .filter(v -> !v.isRepeatable()).collect(Collectors.toList());

      if(Strings.isNullOrEmpty(variableFilter)) {
        filteredVariables = nonRepeatableVariables;
      } else {
        JavascriptClause jsClause = new JavascriptClause(variableFilter);
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
          new REXP[] { //
              new REXPString("data.frame"), //
              ids }, //
          new String[] { //
              "class", //
              "row.names" })));
    }

    private void resolvePath(String path, String idMapping) {
      MagmaEngineReferenceResolver resolver = MagmaEngineTableResolver.valueOf(path);

      if(resolver.getDatasourceName() == null) {
        throw new MagmaRRuntimeException("Datasource is not defined in path: " + path);
      }
      Datasource ds = MagmaEngine.get().getDatasource(resolver.getDatasourceName());

      table = applyIdentifiersMapping(ds.getValueTable(resolver.getTableName()), idMapping);
    }

    /**
     * Represents the entity identifiers as values of a variable.
     */
    private class VariableEntityValueSource extends AbstractVariableValueSource implements VariableValueSource {
      @Override
      public Variable getVariable() {
        return Variable.Builder.newVariable(ID_COLUMN, TextType.get(), table.getEntityType()).build();
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

    private class ValueSetCreatedValueSource extends AbstractVariableValueSource implements VariableValueSource {
      @Override
      public Variable getVariable() {
        return Variable.Builder.newVariable(CREATED_COLUMN, DateTimeType.get(), table.getEntityType()).build();
      }

      @NotNull
      @Override
      public ValueType getValueType() {
        return DateTimeType.get();
      }

      @NotNull
      @Override
      public Value getValue(ValueSet valueSet) {
        return valueSet.getTimestamps().getCreated();
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
            return entities.stream().map(e -> table.getValueSet(e).getTimestamps().getCreated()).collect(Collectors.toList());
          }
        };
      }
    }

    private class ValueSetUpdatedValueSource extends AbstractVariableValueSource implements VariableValueSource {
      @Override
      public Variable getVariable() {
        return Variable.Builder.newVariable(UPDATED_COLUMN, DateTimeType.get(), table.getEntityType()).build();
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

  /**
   * Build a R vector from a variable: vector of values.
   *
   * @see VectorType
   */
  private class VariableRConverter extends AbstractMagmaRConverter {

    private ValueTable table;

    private VariableValueSource variableValueSource;

    @Override
    public boolean canResolve(String path) {
      return path != null && path.contains(".") && path.contains(":");
    }

    private void resolvePath(String path, String idMapping) {
      MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf(path);

      if(resolver.getVariableName() == null) {
        throw new MagmaRRuntimeException("Variable is not defined in path: " + path);
      }

      if(resolver.getDatasourceName() == null) {
        throw new MagmaRRuntimeException("Datasource is not defined in path: " + path);
      }

      table = applyIdentifiersMapping(resolver.resolveTable((ValueTable) null), idMapping);
      variableValueSource = table.getVariableValueSource(resolver.getVariableName());
    }

    @Override
    public REXP asVector(String path, boolean withMissings, String identifiersMapping) {
      resolvePath(path, identifiersMapping);
      prepareEntities(table);
      return getVector(variableValueSource, getEntities(), withMissings);
    }

    @Override
    public void doAssign(String symbol, String path, boolean withMissings, String identifiersMapping) {
      assign(symbol, asVector(path, withMissings, identifiersMapping));
    }
  }

}
