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
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.r.AbstractROperation;
import org.obiba.opal.r.MagmaRRuntimeException;
import org.rosuda.REngine.REXP;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.SortedSet;

/**
 * Assign Magma values (from a datasource, a table or a variable) to a R symbol.
 */
public class MagmaAssignROperation extends AbstractROperation {

  @NotNull
  private final IdentifiersTableService identifiersTableService;

  @NotNull
  private final TransactionTemplate transactionTemplate;

  @NotNull
  private final String symbol;

  @NotNull
  private final String path;

  private final String variableFilter;

  private final boolean withMissings;

  private final String identifiersMapping;

  private final String idColumnName;

  private final String updatedColumnName;

  private SortedSet<VariableEntity> entities;

  private final Set<MagmaRConverter> magmaRConverters = Sets
      .newHashSet((MagmaRConverter) new ValueTableRConverter(this), (MagmaRConverter) new VariableRConverter(this));

  @SuppressWarnings("ConstantConditions")
  public MagmaAssignROperation(@NotNull String symbol, @NotNull String path, String variableFilter,
                               boolean withMissings, String idColumnName, String updatedColumnName, String identifiersMapping,
                               @NotNull IdentifiersTableService identifiersTableService, @NotNull TransactionTemplate transactionTemplate) {
    if (symbol == null) throw new IllegalArgumentException("symbol cannot be null");
    if (path == null) throw new IllegalArgumentException("path cannot be null");
    if (identifiersTableService == null) throw new IllegalArgumentException("identifiers table service cannot be null");
    this.symbol = symbol;
    this.path = path;
    this.variableFilter = variableFilter;
    this.withMissings = withMissings;
    this.identifiersMapping = identifiersMapping;
    this.idColumnName = idColumnName;
    this.updatedColumnName = updatedColumnName;
    this.identifiersTableService = identifiersTableService;
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public void doWithConnection() {
    try {
      for (MagmaRConverter converter : magmaRConverters) {
        if (converter.canResolve(path)) {
          converter.doAssign(symbol, path, withMissings, identifiersMapping);
          return;
        }
      }
    } catch (MagmaRuntimeException e) {
      throw new MagmaRRuntimeException("Failed resolving path '" + path + "'", e);
    }
    throw new MagmaRRuntimeException("No R converter found for path '" + path + "'");
  }

  void doAssign(String sym, REXP ct) {
    assign(sym, ct);
  }

  REXP doEval(String script) {
    return eval(script, false);
  }

  IdentifiersTableService getIdentifiersTableService() {
    return identifiersTableService;
  }

  TransactionTemplate getTransactionTemplate() {
    return transactionTemplate;
  }

  SortedSet<VariableEntity> getEntities() {
    if (entities == null) throw new IllegalStateException("call setEntities() first");
    return entities;
  }

  void setEntities(ValueTable table) {
    this.entities = ImmutableSortedSet.copyOf(table.getVariableEntities());
  }

  public String getSymbol() {
    return symbol;
  }

  String getIdColumnName() {
    return idColumnName;
  }

  boolean withIdColumn() {
    return !Strings.isNullOrEmpty(idColumnName);
  }

  String getUpdatedColumnName() {
    return updatedColumnName;
  }

  boolean withUpdatedColumn() {
    return !Strings.isNullOrEmpty(updatedColumnName);
  }

  public boolean withMissings() {
    return withMissings;
  }

  String getVariableFilter() {
    return variableFilter;
  }

  @Override
  public String toString() {
    return symbol + " <- opal[" + path + "]";
  }

}
