/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
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

import org.obiba.magma.ValueTable;
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
    return getSchedule(vt).getType().equals(Opal.ScheduleType.NOT_SCHEDULED) == false;
  }

  /**
   * Get from the Index manager configuration whether a given value table is ready for indexing by comparing
   * the last update of the table with the last update of the index (and a grace period).
   */
  public boolean isReadyForIndexing(ValueTable vt, ValueTableIndex index) {
    if(isIndexable(vt) && !index.isUpToDate()) {
      // check with schedule
      //Value value = index.getTimestamps().getLastUpdate();

      if(shouldUpdate(getSchedule(vt), index.now())) {
        return true;
      }
    }
    return false;
  }

  public void updateSchedule(ValueTable vt, Schedule schedule) {
    indexConfigurations.put(getFullyQualifiedName(vt), schedule);
  }

  public void removeSchedule(ValueTable vt) {
    Schedule schedule = indexConfigurations.get(getFullyQualifiedName(vt));
    schedule.setType(Opal.ScheduleType.NOT_SCHEDULED);
    indexConfigurations.put(vt.getName(), schedule);
  }

  public Schedule getSchedule(ValueTable vt) {
    Schedule schedule = indexConfigurations.get(getFullyQualifiedName(vt));

    return schedule == null ? new Schedule() : schedule;
  }

  private boolean shouldUpdate(Schedule schedule, Calendar now) {
    switch(schedule.getType()) {
      case MINUTES_5:
        return (now.get(Calendar.MINUTE) % 5 <= 1);

      case MINUTES_15:
        return (now.get(Calendar.MINUTE) % 15 <= 1);

      case MINUTES_30:
        return (now.get(Calendar.MINUTE) % 30 <= 1);

      case HOURLY:
        return now.get(Calendar.MINUTE) <= 1;

      case DAILY:
        // must be the exact time of the day (+ 1 min).
        return now.get(Calendar.HOUR_OF_DAY) == schedule.getHours() && (now.get(Calendar.MINUTE) == schedule
            .getMinutes() || now.get(Calendar.MINUTE) - 1 == schedule.getMinutes());

      case WEEKLY:
        // must be the exact day at the exact time
        return now.get(Calendar.DAY_OF_WEEK) == schedule.getDay().getNumber() && now
            .get(Calendar.HOUR_OF_DAY) == schedule.getHours() && (now.get(Calendar.MINUTE) == schedule
            .getMinutes() || now.get(Calendar.MINUTE) - 1 == schedule.getMinutes());

      default:
        return false;
    }
  }

  private String getFullyQualifiedName(ValueTable vt) {
    return vt.getDatasource().getName() + "." + vt.getName();
  }
}
