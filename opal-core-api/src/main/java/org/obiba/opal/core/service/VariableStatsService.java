/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.math.summary.CategoricalVariableSummary;
import org.obiba.magma.math.summary.CategoricalVariableSummaryFactory;
import org.obiba.magma.math.summary.ContinuousVariableSummary;
import org.obiba.magma.math.summary.ContinuousVariableSummaryFactory;
import org.obiba.magma.math.summary.DefaultVariableSummary;
import org.obiba.magma.math.summary.DefaultVariableSummaryFactory;

/**
 *
 */
public interface VariableStatsService {

  void stackVariable(@NotNull ValueTable valueTable, @NotNull Variable variable, @NotNull Value value);

  void computeSummaries(@NotNull ValueTable valueTable);

  void clearComputingSummaries(@NotNull ValueTable valueTable);

  @NotNull
  ContinuousVariableSummary getContinuousSummary(@NotNull ContinuousVariableSummaryFactory summaryFactory,
      boolean refreshCache);

  @NotNull
  CategoricalVariableSummary getCategoricalSummary(@NotNull CategoricalVariableSummaryFactory summaryFactory,
      boolean refreshCache);

  @NotNull
  DefaultVariableSummary getDefaultSummary(@NotNull DefaultVariableSummaryFactory summaryFactory, boolean refreshCache);
}
