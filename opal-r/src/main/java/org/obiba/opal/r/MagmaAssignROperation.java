/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.r;

import java.util.List;
import java.util.SortedSet;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPList;
import org.rosuda.REngine.RList;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Assign Magma values (from a datasource, a table or a variable) to a R symbol.
 */
public class MagmaAssignROperation extends AbstractROperation {

  private final String symbol;

  private final String path;

  private MagmaVectorProvider magmaVector;

  public MagmaAssignROperation(String symbol, String path) {
    super();
    this.symbol = symbol;
    this.path = path;
    this.magmaVector = new DatasourceVectorProvider();
    this.magmaVector.withNext(new ValueTableVectorProvider()).withNext(new VariableVectorProvider());
  }

  @Override
  public void doWithConnection() {
    try {
      assign(symbol, magmaVector.getVector(path));
    } catch(MagmaRuntimeException e) {
      throw new MagmaRRuntimeException("Failed resolving path '" + path + "'", e);
    }

  }

  //
  // Magma Vector Providers
  //

  /**
   * Provides a R vector from a Magma fully qualified path. Support for providers chaining.
   */
  private interface MagmaVectorProvider {

    /**
     * Build a R vector from the Magma fully-qualified path.
     * @param path
     * @return
     */
    public REXP getVector(String path);

    /**
     * Chain of providers.
     * @param next
     * @return the next provider
     */
    public MagmaVectorProvider withNext(MagmaVectorProvider next);

  }

  /**
   * Base implementation of Magma vector providers.
   */
  private abstract class AbstractMagmaVectorProvider implements MagmaVectorProvider {

    protected MagmaVectorProvider next;

    protected REXP getVector(SortedSet<VariableEntity> entities, VariableValueSource vvs) {
      VectorType vt = VectorType.forValueType(vvs.getValueType());
      return vt.asVector(vvs.getVariable().isRepeatable(), entities.size(), vvs.asVectorSource().getValues(entities));
    }

    /**
     * If Magma path can be resolved, build the corresponding R vector, else forward request to next Magma vector
     * provider (if one is defined).
     */
    @Override
    public REXP getVector(String path) {
      if(canResolve(path)) return getVectorInternal(path);
      else if(next != null) {
        return next.getVector(path);
      }
      throw new MagmaRRuntimeException("Failed resolving path: " + path);
    }

    @Override
    public MagmaVectorProvider withNext(MagmaVectorProvider next) {
      this.next = next;
      return next;
    }

    /**
     * Check if path can be resolved as a datasource, table or variable.
     * @param path
     * @return
     */
    protected abstract boolean canResolve(String path);

    /**
     * Build a R vector depending on the Magma element resolved.
     * @param path
     * @return
     */
    protected abstract REXP getVectorInternal(String path);

  }

  /**
   * Build a R vector from a datasource: list of vectors of tables.
   */
  private class DatasourceVectorProvider extends AbstractMagmaVectorProvider {

    @Override
    public boolean canResolve(String path) {
      return path != null && !path.contains(".") && !path.contains(":");
    }

    @Override
    protected REXP getVectorInternal(String path) {
      Datasource datasource = MagmaEngine.get().getDatasource(path);
      // build a list of list of vectors
      List<REXP> contents = Lists.newArrayList();
      List<String> names = Lists.newArrayList();
      for(ValueTable vt : datasource.getValueTables()) {
        ValueTableVectorProvider vtv = new ValueTableVectorProvider(vt);
        contents.add(vtv.getVector());
        names.add(vt.getName());
      }
      return new REXPList(new RList(contents, names));
    }
  }

  /**
   * Build a R vector from a table: list of vectors of variables.
   */
  private class ValueTableVectorProvider extends AbstractMagmaVectorProvider {

    private ValueTable table;

    public ValueTableVectorProvider() {
      super();
    }

    public ValueTableVectorProvider(ValueTable table) {
      super();
      this.table = table;
    }

    @Override
    public boolean canResolve(String path) {
      return path != null && path.contains(".") && !path.contains(":");
    }

    @Override
    protected REXP getVectorInternal(String path) {
      resolvePath(path);
      return getVector();
    }

    REXP getVector() {
      SortedSet<VariableEntity> entities = Sets.newTreeSet(table.getVariableEntities());
      // build a list of vectors
      List<REXP> contents = Lists.newArrayList();
      List<String> names = Lists.newArrayList();
      for(Variable v : table.getVariables()) {
        VariableValueSource vvs = table.getVariableValueSource(v.getName());
        contents.add(getVector(entities, vvs));
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

  }

  /**
   * Build a R vector from a variable: vector of values.
   * @see VectorType
   */
  private class VariableVectorProvider extends AbstractMagmaVectorProvider {

    private ValueTable table;

    private VariableValueSource variableValueSource;

    @Override
    public boolean canResolve(String path) {
      return path != null && path.contains(".") && path.contains(":");
    }

    private void resolvePath(String path) {
      MagmaEngineReferenceResolver resolver = MagmaEngineVariableResolver.valueOf(path);

      if(resolver.getVariableName() == null) {
        throw new MagmaRRuntimeException("Variable is not defined in path: " + path);
      }

      if(resolver.getDatasourceName() == null) {
        throw new MagmaRRuntimeException("Datasource is not defined in path: " + path);
      }
      Datasource ds = MagmaEngine.get().getDatasource(resolver.getDatasourceName());

      table = ds.getValueTable(resolver.getTableName());

      if(!table.hasVariable(resolver.getVariableName())) {
        throw new MagmaRRuntimeException("Variable is not defined: " + path);
      }
      variableValueSource = table.getVariableValueSource(resolver.getVariableName());
    }

    @Override
    protected REXP getVectorInternal(String path) {
      resolvePath(path);
      SortedSet<VariableEntity> entities = Sets.newTreeSet(table.getVariableEntities());
      // build a vector
      return getVector(entities, variableValueSource);
    }
  }

}
