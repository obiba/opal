/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.r;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractVariableValueSource;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.magma.IdentifiersMappingView;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.REXPList;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Assign Magma values (from a datasource, a table or a variable) to a R symbol.
 */
public class MagmaAssignROperation extends AbstractROperation {

  private static final Logger log = LoggerFactory.getLogger(MagmaAssignROperation.class);

  @NotNull
  private final IdentifiersTableService identifiersTableService;

  @NotNull
  private final String symbol;

  @NotNull
  private final String path;

  private final String variableFilter;

  private final boolean withMissings;

  private final String identifiersMapping;

  private SortedSet<VariableEntity> entities;

  private final Set<MagmaRConverter> magmaRConverters = Sets
      .newHashSet((MagmaRConverter) new ValueTableRConverter(), (MagmaRConverter) new VariableRConverter());

  @SuppressWarnings("ConstantConditions")
  public MagmaAssignROperation(@NotNull String symbol, @NotNull String path, String variableFilter,
      boolean withMissings, String identifiersMapping, @NotNull IdentifiersTableService identifiersTableService) {
    if(symbol == null) throw new IllegalArgumentException("symbol cannot be null");
    if(path == null) throw new IllegalArgumentException("path cannot be null");
    if(identifiersTableService == null) throw new IllegalArgumentException("identifiers table service cannot be null");
    this.symbol = symbol;
    this.path = path;
    this.variableFilter = variableFilter;
    this.withMissings = withMissings;
    this.identifiersMapping = identifiersMapping;
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
      RList list = getVariableVectors();
      return createDataFrame(ids, list);
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
      if(names == null && names.length == 0) return;

      doAssignTmpVectors(ids, names, list);
      doAssignDataFrame(names);
      doRemoveTmpVectors(names);
    }

    private void doAssignTmpVectors(REXP ids, String[] names, RList list) {
      // one temporary vector per variable
      for(String name : names) {
        assign(getTmpVectorName(symbol, name), list.at(name));
      }
      // one temporary vector for the ids
      assign(getTmpVectorName(symbol, "row.names"), ids);
    }

    private void doAssignDataFrame(String... names) {
      // create the data.frame from the vectors
      StringBuffer args = new StringBuffer();
      for(String name : names) {
        if(args.length() > 0) args.append(", ");
        args.append("'").append(name).append("'=").append(getTmpVectorName(symbol, name));
      }
      args.append(", row.names=").append(getTmpVectorName(symbol, "row.names"));
      log.info("data.frame arguments: {}", args);
      eval(symbol + " <- data.frame(" + args + ")", false);
    }

    private void doRemoveTmpVectors(String... names) {
      // remove temporary vectors
      for(String name : names) {
        eval("base::rm(" + getTmpVectorName(symbol, name) + ")", false);
      }
      eval("base::rm(" + getTmpVectorName(symbol, "row.names") + ")", false);
    }

    private String getTmpVectorName(String symbol, String name) {
      return ("opal__" + symbol + "__" + name).replace("-", ".").replace("+", ".").replace(" ", ".").replace("\"", ".")
          .replace("'", ".");
    }

    private REXP getIdsVector(boolean withMissings) {
      return getVector(new VariableEntityValueSource(), getEntities(), withMissings);
    }

    private RList getVariableVectors() {
      // build a list of vectors
      List<REXP> contents = Lists.newArrayList();
      List<String> names = Lists.newArrayList();

      // vector for each variable
      for(Variable v : filterVariables()) {
        VariableValueSource vvs = table.getVariableValueSource(v.getName());
        if (!vvs.getVariable().isRepeatable()) {
          contents.add(getVector(vvs, getEntities(), withMissings));
          names.add(vvs.getVariable().getName());
        }
      }

      return new RList(contents, names);
    }

    protected Iterable<Variable> filterVariables() {
      List<Variable> filteredVariables = null;

      if(Strings.isNullOrEmpty(variableFilter)) {
        filteredVariables = Lists.newArrayList(table.getVariables());
      } else {
        JavascriptClause jsClause = new JavascriptClause(variableFilter);
        jsClause.initialise();

        filteredVariables = new ArrayList<>();
        for(Variable variable : table.getVariables()) {
          if(jsClause.select(variable)) {
            filteredVariables.add(variable);
          }
        }
      }
      return filteredVariables;
    }

    /**
     * @param ids
     * @param values
     * @return
     * @see REXP.createDataFrame()
     */
    private REXP createDataFrame(REXP ids, RList values) {
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
        return Variable.Builder.newVariable(ENTITY_ID_SYMBOL, TextType.get(), table.getEntityType()).build();
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
          public Iterable<Value> getValues(
              @SuppressWarnings("ParameterHidesMemberVariable") SortedSet<VariableEntity> entities) {
            return Iterables.transform(getEntities(), new Function<VariableEntity, Value>() {
              @Override
              public Value apply(@NotNull VariableEntity input) {
                return TextType.get().valueOf(input.getIdentifier());
              }
            });
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
