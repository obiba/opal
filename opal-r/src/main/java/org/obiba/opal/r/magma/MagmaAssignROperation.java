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
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.r.AbstractROperation;
import org.rosuda.REngine.REXP;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.constraints.NotNull;
import java.util.SortedSet;

/**
 * Assign Magma values (from a table or a variable) to a R symbol.
 */
public class MagmaAssignROperation extends AbstractROperation {

  public enum RClass {
    DATA_FRAME, TIBBLE, VECTOR
  }

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

  private final RClass rClass;

  private SortedSet<VariableEntity> entities;

  public MagmaAssignROperation(@NotNull String symbol, @NotNull String path, String variableFilter,
                               boolean withMissings, String idColumnName, String updatedColumnName, String identifiersMapping, RClass rClass,
                               @NotNull IdentifiersTableService identifiersTableService, @NotNull TransactionTemplate transactionTemplate) {
    if (symbol == null) throw new IllegalArgumentException("symbol cannot be null");
    if (path == null) throw new IllegalArgumentException("path cannot be null");
    if (!Strings.isNullOrEmpty(identifiersMapping) && identifiersTableService == null)
      throw new IllegalArgumentException("identifiers table service cannot be null");
    this.symbol = symbol;
    this.path = path;
    this.variableFilter = variableFilter;
    this.withMissings = withMissings;
    this.identifiersMapping = identifiersMapping;
    this.idColumnName = idColumnName;
    this.updatedColumnName = updatedColumnName;
    this.identifiersTableService = identifiersTableService;
    this.transactionTemplate = transactionTemplate;
    this.rClass = path.contains(":") ? RClass.VECTOR : RClass.VECTOR.equals(rClass) ? RClass.DATA_FRAME : rClass;
  }

  @Override
  public void doWithConnection() {
    MagmaRConverter converter;
    switch (rClass) {
      case VECTOR:
        converter = new VariableRConverter(this);
        break;
      case DATA_FRAME:
        converter = new ValueTableRDataFrameConverter(this);
        break;
      default:
        converter = new ValueTableRTibbleConverter(this);
    }

    converter.doAssign(symbol, path);
  }

  void doAssign(String sym, REXP ct) {
    assign(sym, ct);
  }

  REXP doEval(String script) {
    return eval(script, false);
  }

  REXP doEnsurePackage(String packageName) {
    return ensurePackage(packageName);
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

  String getSymbol() {
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

  boolean withMissings() {
    return withMissings;
  }

  String getIdentifiersMapping() {
    return identifiersMapping;
  }

  String getVariableFilter() {
    return variableFilter;
  }

  @Override
  public String toString() {
    return symbol + " <- opal[" + path + "]";
  }

}
