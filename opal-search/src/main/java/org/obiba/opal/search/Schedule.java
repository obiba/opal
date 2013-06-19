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

import org.obiba.opal.web.model.Opal;

public class Schedule {

  private Opal.ScheduleType type;

  private Opal.Day day;

  private Integer hours;

  private Integer minutes;

  public Schedule() {
    type = Opal.ScheduleType.NOT_SCHEDULED;
  }

  public Opal.ScheduleType getType() {
    return type;
  }

  public void setType(Opal.ScheduleType type) {
    this.type = type;
  }

  public Opal.Day getDay() {
    return day;
  }

  public void setDay(Opal.Day day) {
    this.day = day;
  }

  public Integer getMinutes() {
    return minutes;
  }

  public void setMinutes(Integer minutes) {
    this.minutes = minutes;
  }

  public Integer getHours() {
    return hours;
  }

  public void setHours(Integer hours) {
    this.hours = hours;
  }

}
