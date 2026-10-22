package org.obiba.opal.web.magma.math;

import java.util.List;

import org.junit.Test;
import org.obiba.magma.math.summary.support.DefaultFrequency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AbstractSummaryResourceTest {

  @Test
  public void test_window_ignored_without_values_permission() {
    AbstractSummaryResource resource = new AbstractSummaryResource() {};
    assertNull(resource.windowIfPermitted(10));

    resource.setViewValuesPermitted(true);
    assertEquals(Integer.valueOf(10), resource.windowIfPermitted(10));
    assertNull(resource.windowIfPermitted(null));
  }

  @Test
  public void test_small_counts_disclosive_without_values_permission() {
    AbstractSummaryResource resource = new AbstractSummaryResource() {};
    resource.minN = 5;
    assertTrue(resource.isDisclosive(1));
    assertTrue(resource.isDisclosive(4));
    assertFalse(resource.isDisclosive(0));
    assertFalse(resource.isDisclosive(5));

    // a small missing category is part of the default summary anyway
    assertFalse(resource.isDisclosive(List.of(new DefaultFrequency("1", 10, 0.8, false), new DefaultFrequency("9", 2, 0.2, true))));
    assertTrue(resource.isDisclosive(List.of(new DefaultFrequency("1", 10, 0.8, false), new DefaultFrequency("2", 2, 0.2, false))));

    resource.setViewValuesPermitted(true);
    assertFalse(resource.isDisclosive(1));

    resource.setViewValuesPermitted(false);
    resource.minN = 0;
    assertFalse(resource.isDisclosive(1));
  }

}
