/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.web.model.Opal;

public class IndexManagerConfiguration implements OpalConfigurationExtension {

  private Map<String, Schedule> indexConfigurations;

  public IndexManagerConfiguration() {
    indexConfigurations = new HashMap<String, Schedule>();
  }

  /**
   * Get from the Index manager configuration whether a given value table is indexable or not.
   */
  public boolean isIndexable(ValueTable vt) {
    return indexConfigurations.containsKey(vt.getName()) && !indexConfigurations.get(vt.getName()).getType()
        .equals(Opal.ScheduleType.NOT_SCHEDULED);
  }

  /**
   * Get from the Index manager configuration whether a given value table is ready for indexing by comparing
   * the last update of the table with the last update of the index (and a grace period).
   */
  public boolean isReadyForIndexing(ValueTable vt, IndexManager indexManager) {
    if(isIndexable(vt) && !indexManager.getIndex(vt).isUpToDate()) {
      // check with schedule
      // compare  indexManager.getIndex(vt).
      Value value = indexManager.getIndex(vt).getTimestamps().getLastUpdate();

      if(value.isNull() || value.compareTo(gracePeriod(indexConfigurations.get(vt.getName()))) < 0) {
        return true;
      }
    }
    return false;
  }

  public void updateSchedule(ValueTable vt, Schedule schedule) {
    indexConfigurations.put(vt.getName(), schedule);
  }

  public void removeSchedule(ValueTable vt) {
    Schedule schedule = indexConfigurations.get(vt.getName());
    schedule.setType(Opal.ScheduleType.NOT_SCHEDULED);
    indexConfigurations.put(vt.getName(), schedule);
  }

  public Schedule getSchedule(ValueTable vt) {

    return indexConfigurations.get(vt.getName());
  }

  private Value gracePeriod(Schedule schedule) {
    // Now
    Calendar gracePeriod = Calendar.getInstance();

    if(schedule.getType().equals(Opal.ScheduleType.HOURLY)) {
      // Move back in time by X minutes
      gracePeriod.add(Calendar.MINUTE, -schedule.getMinutes());
    } else if(schedule.getType().equals(Opal.ScheduleType.DAILY)) {
      // Move back in time by X hours
      gracePeriod.add(Calendar.HOUR, -schedule.getHours());
    } else {
      // Move back in time by X days
      gracePeriod.add(Calendar.DAY_OF_WEEK, -schedule.getDay().getNumber());
    }

    // Things modified before this value can be reindexed
    return DateTimeType.get().valueOf(gracePeriod);
  }
}
