package org.obiba.opal.core.service.impl;

import java.util.Calendar;
import java.util.Date;

import org.obiba.magma.datasource.hibernate.domain.Timestamped;

import static org.apache.commons.lang.time.DateUtils.round;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Asserts {

  private Asserts() {}

  public static void assertCreatedTimestamps(Timestamped expected, Timestamped found) {
    // do not compare seconds because Orient does not seem to serialize milliseconds
    assertEquals(round(expected.getCreated(), Calendar.MINUTE), round(found.getCreated(), Calendar.MINUTE));
  }

  public static void assertUpdatedTimestamps(Timestamped expected, Timestamped found) {
    Date expectedUpdated = expected.getUpdated();
    Date foundUpdated = found.getUpdated();
    if(expectedUpdated == null) {
      assertNotNull(foundUpdated);
    } else {
      assertTrue("Expected lastUpdate (" + expectedUpdated + ") should be before or same as found lastUpdate (" +
          foundUpdated + ")", expectedUpdated.before(foundUpdated));
    }
  }

}
