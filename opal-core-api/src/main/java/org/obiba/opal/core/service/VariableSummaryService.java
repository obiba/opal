/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

import javax.validation.constraints.NotNull;

import org.obiba.magma.math.summary.VariableSummary;
import org.obiba.magma.math.summary.VariableSummaryFactory;
import org.obiba.opal.spi.search.VariableSummaryHandler;

/**
 *
 */
public interface VariableSummaryService extends VariableSummaryHandler {

  @NotNull
  <TVariableSummary extends VariableSummary, //
      TVariableSummaryFactory extends VariableSummaryFactory<TVariableSummary>> TVariableSummary getSummary(
      @NotNull VariableSummaryFactory<TVariableSummary> summaryFactory, boolean refreshCache);

  <TVariableSummary extends VariableSummary, //
      TVariableSummaryFactory extends VariableSummaryFactory<TVariableSummary>> boolean isSummaryCached(
      @NotNull VariableSummaryFactory<TVariableSummary> summaryFactory);

}
