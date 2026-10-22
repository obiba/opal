package org.obiba.opal.web.magma.math;

import org.junit.Test;
import org.obiba.opal.web.model.Math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ContinuousSummaryResourceImplTest {

  @Test
  public void test_individual_values_removed_without_values_permission() {
    ContinuousSummaryResourceImpl resource = new ContinuousSummaryResourceImpl();
    Math.ContinuousSummaryDto dto = resource.withoutIndividualValues(summary()).build();
    assertFalse(dto.getSummary().hasMin());
    assertFalse(dto.getSummary().hasMax());
    assertEquals(0, dto.getIntervalFrequencyCount());
    assertEquals(0, dto.getDistributionPercentilesCount());
    assertEquals(2.5, dto.getSummary().getMean(), 0);
    assertEquals(1, dto.getSummary().getPercentilesCount());

    resource.setViewValuesPermitted(true);
    dto = resource.withoutIndividualValues(summary()).build();
    assertTrue(dto.getSummary().hasMin());
    assertEquals(1, dto.getIntervalFrequencyCount());
  }

  private static Math.ContinuousSummaryDto.Builder summary() {
    return Math.ContinuousSummaryDto.newBuilder()
        .setSummary(Math.DescriptiveStatsDto.newBuilder().setN(10).setMin(1).setMax(4).setMean(2.5).addPercentiles(2.5))
        .addIntervalFrequency(Math.IntervalFrequencyDto.newBuilder().setLower(1).setUpper(4).setFreq(10).setDensity(1))
        .addDistributionPercentiles(2.4);
  }

}
