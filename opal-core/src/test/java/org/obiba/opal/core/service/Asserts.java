/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.Timestamped;

import java.util.Calendar;
import java.util.Date;

import static org.apache.commons.lang.time.DateUtils.round;
import static org.fest.assertions.api.Assertions.assertThat;

public class Asserts {

  private Asserts() {}

  public static void assertCreatedTimestamps(Timestamped expected, Timestamped found) {
    // do not compare seconds because Orient does not seem to serialize milliseconds
    assertThat(round(found.getCreated(), Calendar.MINUTE)).isEqualTo(round(expected.getCreated(), Calendar.MINUTE));
  }

  public static void assertUpdatedTimestamps(Timestamped expected, Timestamped found) {
    Date expectedUpdated = expected.getUpdated();
    Date foundUpdated = found.getUpdated();
    if(expectedUpdated == null) {
      assertThat(foundUpdated).isNotNull();
    } else {
      assertThat(expectedUpdated).isBefore(foundUpdated).overridingErrorMessage(
          "Expected lastUpdate (" + expectedUpdated + ") should be before or same as found lastUpdate (" +
              foundUpdated + ")");
    }
  }

}
