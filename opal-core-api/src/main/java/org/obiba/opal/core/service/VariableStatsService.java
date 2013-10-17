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

import java.util.List;

import javax.annotation.Nonnull;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.magma.math.CategoricalVariableSummary;
import org.obiba.opal.core.magma.math.ContinuousVariableSummary;

/**
 *
 */
public interface VariableStatsService {

  void computeVariable(@Nonnull ValueTable valueTable, @Nonnull Variable variable, @Nonnull Value value);

  void computeSummaries(@Nonnull ValueTable valueTable);

  void clearComputingSummaries(@Nonnull ValueTable valueTable);

  @Nonnull
  ContinuousVariableSummary getContinuousSummary(@Nonnull Variable variable, @Nonnull ValueTable table,
      @Nonnull ValueSource valueSource, @Nonnull ContinuousVariableSummary.Distribution distribution,
      List<Double> percentiles, int intervals, Integer offset, Integer limit);

  @Nonnull
  CategoricalVariableSummary getCategoricalSummary(@Nonnull Variable variable, @Nonnull ValueTable table,
      @Nonnull ValueSource valueSource, boolean distinct, Integer offset, Integer limit);

}
