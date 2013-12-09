package org.obiba.opal.core.service;

import java.util.Calendar;
import java.util.Date;

import org.hamcrest.core.Is;
import org.obiba.magma.datasource.hibernate.domain.Timestamped;

import static org.apache.commons.lang.time.DateUtils.round;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class Asserts {

  private Asserts() {}

  public static void assertCreatedTimestamps(Timestamped expected, Timestamped found) {
    // do not compare seconds because Orient does not seem to serialize milliseconds
    assertThat(round(found.getCreated(), Calendar.MINUTE), Is.is(round(expected.getCreated(), Calendar.MINUTE)));
  }

  public static void assertUpdatedTimestamps(Timestamped expected, Timestamped found) {
    Date expectedUpdated = expected.getUpdated();
    Date foundUpdated = found.getUpdated();
    if(expectedUpdated == null) {
      assertThat(foundUpdated, notNullValue());
    } else {
      assertTrue("Expected lastUpdate (" + expectedUpdated + ") should be before or same as found lastUpdate (" +
          foundUpdated + ")", expectedUpdated.before(foundUpdated));
    }
  }

}
