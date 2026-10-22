package org.obiba.opal.web.magma.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AbstractSummaryResourceTest {

  @Test
  public void test_window_ignored_without_values_permission() {
    AbstractSummaryResource resource = new AbstractSummaryResource() {};
    assertNull(resource.windowIfPermitted(10));

    resource.setViewValuesPermitted(true);
    assertEquals(Integer.valueOf(10), resource.windowIfPermitted(10));
    assertNull(resource.windowIfPermitted(null));
  }

}
