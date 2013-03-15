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

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.type.TextType;
import org.obiba.opal.r.service.VariableEntitiesHolder;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPList;
import org.rosuda.REngine.RList;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Assign Magma values (from a datasource, a table or a variable) to a R symbol.
 */
public class MagmaAssignROperation extends AbstractROperation {

  private final VariableEntitiesHolder holder;

  private final String symbol;

  private final String path;

  private final Set<MagmaRConverter> magmaRConverters = Sets
      .newHashSet(new DatasourceRConverter(), new ValueTableRConverter(), new VariableRConverter());

  public MagmaAssignROperation(VariableEntitiesHolder holder, String symbol, String path) {
    super();
    if(holder == null) throw new IllegalArgumentException("holder cannot be null");
    if(symbol == null) throw new IllegalArgumentException("symbol cannot be null");
    if(path == null) throw new IllegalArgumentException("path cannot be null");
    this.holder = holder;
    this.symbol = symbol;
    this.path = path;
  }

  @Override
  public void doWithConnection() {
    try {
      for(MagmaRConverter converter : magmaRConverters) {
        if(converter.canResolve(path)) {
          assign(symbol, converter.asVector(path));
          return;
        }
      }
    } catch(MagmaRuntimeException e) {
      throw new MagmaRRuntimeException("Failed resolving path '" + path + "'", e);
    }
    throw new MagmaRRuntimeException("Failed resolving path '" + path + "'");
  }

  void prepareEntities(ValueTable table) {
    if(holder.hasEntities() == false) {
      holder.setEntities(ImmutableSortedSet.copyOf(table.getVariableEntities()));
    }
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
     * @return
     */
    public REXP asVector(String path);

    /**
     * Check if path can be resolved as a datasource, table or variable.
     *
     * @param path
     * @return
     */
    public boolean canResolve(String path);

  }

  /**
   * Base implementation of Magma vector providers.
   */
  private abstract class AbstractMagmaRConverter implements MagmaRConverter {

    protected REXP getVector(VariableValueSource vvs, SortedSet<VariableEntity> entities) {
      VectorType vt = VectorType.forValueType(vvs.getValueType());
      return vt.asVector(vvs, entities);
    }
  }

  /**
   * Build a R vector from a datasource: list of vectors of tables.
   */
  private class DatasourceRConverter implements MagmaRConverter {

    @Override
    public boolean canResolve(String path) {
      return path != null && !path.contains(".") && !path.contains(":");
    }

    @Override
    public REXP asVector(String path) {
      Datasource datasource = MagmaEngine.get().getDatasource(path);

      if(holder.hasEntities() == false) {
        SortedSet<VariableEntity> entities = Sets.newTreeSet();
        for(ValueTable vt : datasource.getValueTables()) {
          entities.addAll(vt.getVariableEntities());
        }
        holder.setEntities(entities);
      }

      // build a list of list of vectors
      List<REXP> contents = Lists.newArrayList();
      List<String> names = Lists.newArrayList();
      for(ValueTable vt : datasource.getValueTables()) {
        ValueTableRConverter vtv = new ValueTableRConverter(vt);
        contents.add(vtv.asVector());
        names.add(vt.getName());
      }
      return new REXPList(new RList(contents, names));
    }
  }

  /**
   * Build a R vector from a table: list of vectors of variables.
   */
  private class ValueTableRConverter extends AbstractMagmaRConverter {

    private static final String ENTITY_ID_SYMBOL = "ID__";

    private ValueTable table;

    public ValueTableRConverter() {
    }

    public ValueTableRConverter(ValueTable table) {
      this.table = table;
    }

    @Override
    public boolean canResolve(String path) {
      return path != null && path.contains(".") && !path.contains(":");
    }

    @Override
    public REXP asVector(String path) {
      resolvePath(path);
      return asVector();
    }

    /**
     * Build a R vector from an already set ValueTable.
     *
     * @return
     */
    REXP asVector() {
      if(table == null) throw new IllegalStateException("Table must not be null");
      prepareEntities(table);
      // build a list of vectors
      List<REXP> contents = Lists.newArrayList();
      List<String> names = Lists.newArrayList();

      // entity identifiers
      contents.add(getVector(new VariableEntityValueSource(), holder.getEntities()));
      names.add(ENTITY_ID_SYMBOL);

      // vector for each variable
      for(Variable v : table.getVariables()) {
        VariableValueSource vvs = table.getVariableValueSource(v.getName());
        contents.add(getVector(vvs, holder.getEntities()));
        names.add(vvs.getVariable().getName());
      }
      return new REXPList(new RList(contents, names));
    }

    private void resolvePath(String path) {
      MagmaEngineReferenceResolver resolver = MagmaEngineTableResolver.valueOf(path);

      if(resolver.getDatasourceName() == null) {
        throw new MagmaRRuntimeException("Datasource is not defined in path: " + path);
      }
      Datasource ds = MagmaEngine.get().getDatasource(resolver.getDatasourceName());

      table = ds.getValueTable(resolver.getTableName());
    }

    /**
     * Represents the entity identifiers as values of a variable.
     */
    private class VariableEntityValueSource implements VariableValueSource {
      @Override
      public Variable getVariable() {
        return Variable.Builder.newVariable(ENTITY_ID_SYMBOL, TextType.get(), table.getEntityType()).build();
      }

      @Nonnull
      @Override
      public ValueType getValueType() {
        return TextType.get();
      }

      @Nonnull
      @Override
      public Value getValue(ValueSet valueSet) {
        return TextType.get().valueOf(valueSet.getVariableEntity().getIdentifier());
      }

      @Nullable
      @Override
      public VectorSource asVectorSource() {
        return new VectorSource() {
          @Override
          public ValueType getValueType() {
            return TextType.get();
          }

          @Override
          public Iterable<Value> getValues(SortedSet<VariableEntity> entities) {
            return Iterables.transform(holder.getEntities(), new Function<VariableEntity, Value>() {
              @Override
              public Value apply(@Nonnull VariableEntity input) {
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

    private void resolvePath(String path) {
      MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf(path);

      if(resolver.getVariableName() == null) {
        throw new MagmaRRuntimeException("Variable is not defined in path: " + path);
      }

      if(resolver.getDatasourceName() == null) {
        throw new MagmaRRuntimeException("Datasource is not defined in path: " + path);
      }
      table = resolver.resolveTable((ValueTable) null);
      variableValueSource = resolver.resolveSource();
    }

    @Override
    public REXP asVector(String path) {
      resolvePath(path);
      prepareEntities(table);
      return getVector(variableValueSource, holder.getEntities());
    }
  }

}
