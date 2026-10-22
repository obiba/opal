/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma.math;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Response;

import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.math.Frequency;
import org.obiba.magma.math.summary.DefaultVariableSummary;
import org.obiba.magma.math.summary.DefaultVariableSummaryFactory;
import org.obiba.opal.core.service.VariableSummaryService;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Math;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractSummaryResource implements SummaryResource {

  @NotNull
  private ValueTable valueTable;

  @NotNull
  private Variable variable;

  @NotNull
  private ValueSource variableValueSource;

  private boolean viewValuesPermitted;

  @Value("${summary.minN:5}")
  protected int minN;

  @Autowired
  protected VariableSummaryService variableSummaryService;

  @Override
  public void setValueTable(@NotNull ValueTable valueTable) {
    this.valueTable = valueTable;
  }

  @Override
  public void setVariable(@NotNull Variable variable) {
    this.variable = variable;
  }

  @Override
  public void setVariableValueSource(@NotNull ValueSource variableValueSource) {
    this.variableValueSource = variableValueSource;
  }

  @Override
  public void setViewValuesPermitted(boolean viewValuesPermitted) {
    this.viewValuesPermitted = viewValuesPermitted;
  }

  protected boolean isViewValuesPermitted() {
    return viewValuesPermitted;
  }

  /**
   * The entity window (offset or limit) is ignored for users who cannot see the values: a summary of a single entity,
   * or the difference between two windows, would give away individual values.
   */
  protected Integer windowIfPermitted(Integer window) {
    return viewValuesPermitted ? window : null;
  }

  /**
   * Whether a count, between 1 and {@code summary.minN} - 1, would single out individuals to a user who cannot see the
   * values.
   */
  protected boolean isDisclosive(long count) {
    return !viewValuesPermitted && count > 0 && count < minN;
  }

  /**
   * Whether a non-missing frequency is disclosive, the missing ones being part of the default summary anyway.
   */
  protected boolean isDisclosive(Iterable<Frequency> frequencies) {
    for (Frequency frequency : frequencies) {
      if (!frequency.isMissing() && isDisclosive(frequency.getFreq())) return true;
    }
    return false;
  }

  /**
   * Counts of the non-missing values and of the missing ones only, which is what users who cannot see the values get
   * when a summary would disclose fewer than {@code summary.minN} individuals.
   */
  protected Response getDefaultSummary(Integer offset, Integer limit, boolean fullIfCached, boolean resetCache) {
    DefaultVariableSummaryFactory summaryFactory = new DefaultVariableSummaryFactory.Builder() //
        .variable(getVariable()) //
        .table(getValueTable()) //
        .valueSource(getVariableValueSource()) //
        .offset(windowIfPermitted(offset)).build();

    DefaultVariableSummary summary;
    if(fullIfCached && variableSummaryService.isSummaryCached(summaryFactory)) {
      summary = variableSummaryService.getSummary(summaryFactory, resetCache);
    } else {
      summaryFactory.setLimit(windowIfPermitted(limit));
      summary = variableSummaryService.getSummary(summaryFactory, resetCache);
    }
    SummaryStatisticsDto.Builder dtoBuilder = SummaryStatisticsDto.newBuilder() //
        .setResource(getVariable().getName()) //
        .setExtension(Math.DefaultSummaryDto.defaultSummary, Dtos.asDto(summary).build());
    if(summary.getLimit() != null) dtoBuilder.setLimit(summary.getLimit());
    SummaryStatisticsDto dto = dtoBuilder.build();
    return summary.getOffset() == null && summary.getLimit() == null //
        ? TimestampedResponses.ok(getValueTable(), dto).build() //
        : Response.ok(dto).build();
  }

  @Override
  @NotNull
  public ValueTable getValueTable() {
    return valueTable;
  }

  @Override
  @NotNull
  public Variable getVariable() {
    return variable;
  }

  @Override
  @NotNull
  public ValueSource getVariableValueSource() {
    return variableValueSource;
  }

}
