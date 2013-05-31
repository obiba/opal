/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.math;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.type.BooleanType;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.springframework.util.Assert;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class AbstractSummaryResource {

  protected final OpalSearchService opalSearchService;

  protected final StatsIndexManager statsIndexManager;

  protected final ElasticSearchProvider esProvider;

  @Nonnull
  private final ValueTable valueTable;

  @Nonnull
  private final Variable variable;

  @Nonnull
  private final VariableValueSource variableValueSource;

  protected AbstractSummaryResource(OpalSearchService opalSearchService, StatsIndexManager statsIndexManager,
      ElasticSearchProvider esProvider, @Nonnull ValueTable valueTable, @Nonnull Variable variable,
      @Nonnull VariableValueSource variableValueSource) {
    this.variableValueSource = variableValueSource;
    Assert.notNull(valueTable);
    Assert.notNull(variable);
    Assert.notNull(variableValueSource);

    this.opalSearchService = opalSearchService;
    this.statsIndexManager = statsIndexManager;
    this.esProvider = esProvider;
    this.valueTable = valueTable;
    this.variable = variable;
  }

  @Nonnull
  public ValueTable getValueTable() {
    return valueTable;
  }

  @Nonnull
  public Variable getVariable() {
    return variable;
  }

  @Nonnull
  public VariableValueSource getVariableValueSource() {
    return variableValueSource;
  }

  protected boolean isEsAvailable() {
    return opalSearchService.isEnabled() && opalSearchService.isRunning() && statsIndexManager.isReady();
  }

  protected boolean canQueryEsIndex() {
    return isEsAvailable() && statsIndexManager.isIndexUpToDate(valueTable);
  }

}
