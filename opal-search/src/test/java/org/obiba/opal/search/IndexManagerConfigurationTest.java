/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import java.util.Calendar;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.search.service.ValueTableIndex;
import org.obiba.opal.web.model.Opal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 *
 */
@SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount", "ReuseOfLocalVariable" })
public class IndexManagerConfigurationTest {

  @Before
  public void startYourEngine() {
    new MagmaEngine();
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_isReadyForIndexing_minutes() {
    ValueTable vt = createMock(ValueTable.class);
    Datasource datasource = createMock(Datasource.class);
    ValueTableIndex index = createMock(ValueTableIndex.class);

    IndexManagerConfiguration config = new IndexManagerConfiguration();

    expect(datasource.getName()).andReturn("datasource").anyTimes();
    expect(vt.getDatasource()).andReturn(datasource).anyTimes();
    expect(vt.getName()).andReturn("table").atLeastOnce();
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();

    Calendar now = Calendar.getInstance();
    now.set(2012, Calendar.NOVEMBER, 20, 10, 16);
    expect(index.now()).andReturn(now).atLeastOnce();

    EasyMock.replay(datasource, vt, index);

    // MINUTES_15
    Schedule schedule = new Schedule();
    schedule.setType(Opal.ScheduleType.MINUTES_15);
    config.updateSchedule(vt, schedule);
    assertThat(config.getSchedule(vt).getType()).isEqualTo(Opal.ScheduleType.MINUTES_15);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    // at 00 of each hour
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 12, 0);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    config.updateSchedule(vt, schedule);
    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    // at 15 of each hour
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 15);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    // at 16 of each hour
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 16);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    // at 30 of each hour
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 30);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    // at 45 of each hour
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 45);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    // at 17 of each hour
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 17);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(!config.isReadyForIndexing(vt, index)).isTrue();

    // at 17 of each hour
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 38);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(!config.isReadyForIndexing(vt, index)).isTrue();

    // MINUTES_5
    schedule = new Schedule();
    schedule.setType(Opal.ScheduleType.MINUTES_5);
    config.updateSchedule(vt, schedule);
    assertThat(config.getSchedule(vt).getType()).isEqualTo(Opal.ScheduleType.MINUTES_5);

    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 0);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 1);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 2);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(!config.isReadyForIndexing(vt, index)).isTrue();

    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 5);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 10);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 15);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 20);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 25);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 30);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 35);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 40);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 45);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 50);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 55);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();
  }

  @Test
  public void test_isReadyForIndexing_hourly() {
    ValueTable vt = createMock(ValueTable.class);
    Datasource datasource = createMock(Datasource.class);
    ValueTableIndex index = createMock(ValueTableIndex.class);

    IndexManagerConfiguration config = new IndexManagerConfiguration();

    expect(datasource.getName()).andReturn("datasource").anyTimes();
    expect(vt.getDatasource()).andReturn(datasource).anyTimes();
    expect(vt.getName()).andReturn("table").atLeastOnce();
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();

    Calendar now = Calendar.getInstance();
    now.set(2012, Calendar.NOVEMBER, 20, 10, 16);
    expect(index.now()).andReturn(now).atLeastOnce();

    EasyMock.replay(datasource, vt, index);

    // HOURLY
    Schedule schedule = new Schedule();
    schedule.setType(Opal.ScheduleType.HOURLY);
    schedule.setMinutes(0);
    config.updateSchedule(vt, schedule);
    assertThat(config.getSchedule(vt).getType()).isEqualTo(Opal.ScheduleType.HOURLY);

    // at 00 of each hour
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 12, 0);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    // at 00 of each hour
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 10, 0);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    // at 01 of each hour
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 12, 1);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    // at 02 of each hour
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 12, 2);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(!config.isReadyForIndexing(vt, index)).isTrue();

    // at 59 of each hour
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 9, 59);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(!config.isReadyForIndexing(vt, index)).isTrue();
  }

  @Test
  public void test_isReadyForIndexing_daily() {
    ValueTable vt = createMock(ValueTable.class);
    Datasource datasource = createMock(Datasource.class);
    ValueTableIndex index = createMock(ValueTableIndex.class);

    IndexManagerConfiguration config = new IndexManagerConfiguration();

    expect(datasource.getName()).andReturn("datasource").anyTimes();
    expect(vt.getDatasource()).andReturn(datasource).anyTimes();
    expect(vt.getName()).andReturn("table").atLeastOnce();
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();

    Calendar now = Calendar.getInstance();
    now.set(2012, Calendar.NOVEMBER, 20, 10, 16);
    expect(index.now()).andReturn(now).atLeastOnce();

    EasyMock.replay(datasource, vt, index);

    // DAILY
    Schedule schedule = new Schedule();
    schedule.setType(Opal.ScheduleType.DAILY);
    schedule.setHours(12);
    schedule.setMinutes(0);

    config.updateSchedule(vt, schedule);
    assertThat(config.getSchedule(vt).getType()).isEqualTo(Opal.ScheduleType.DAILY);
    assertThat(config.getSchedule(vt).getHours()).isEqualTo(12);
    assertThat(config.getSchedule(vt).getMinutes()).isEqualTo(0);

    // at everyday noon
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 12, 0);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    // at everyday noon
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.JANUARY, 20, 12, 1);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    // at everyday noon
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 21, 12, 0);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    // at everyday noon
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 1, 12, 0);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    // at everyday noon
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 17, 12, 0);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    // at everyday noon
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 12, 12, 2);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(!config.isReadyForIndexing(vt, index)).isTrue();

    // at everyday noon
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 18, 10, 0);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(!config.isReadyForIndexing(vt, index)).isTrue();

  }

  @Test
  public void test_isReadyForIndexing_weekly() {
    ValueTable vt = createMock(ValueTable.class);
    Datasource datasource = createMock(Datasource.class);
    ValueTableIndex index = createMock(ValueTableIndex.class);

    IndexManagerConfiguration config = new IndexManagerConfiguration();

    expect(datasource.getName()).andReturn("datasource").anyTimes();
    expect(vt.getDatasource()).andReturn(datasource).anyTimes();
    expect(vt.getName()).andReturn("table").atLeastOnce();
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();

    Calendar now = Calendar.getInstance();
    now.set(2012, Calendar.NOVEMBER, 20, 10, 16);
    expect(index.now()).andReturn(now).atLeastOnce();

    EasyMock.replay(datasource, vt, index);

    // DAILY
    Schedule schedule = new Schedule();
    schedule.setType(Opal.ScheduleType.WEEKLY);
    schedule.setDay(Opal.Day.FRIDAY);
    schedule.setHours(9);
    schedule.setMinutes(15);

    config.updateSchedule(vt, schedule);
    assertThat(config.getSchedule(vt).getType()).isEqualTo(Opal.ScheduleType.WEEKLY);
    assertThat(config.getSchedule(vt).getDay().getNumber()).isEqualTo(Calendar.FRIDAY);
    assertThat(Opal.Day.SUNDAY.getNumber()).isEqualTo(Calendar.SUNDAY);
    assertThat(Opal.Day.MONDAY.getNumber()).isEqualTo(Calendar.MONDAY);
    assertThat(Opal.Day.TUESDAY.getNumber()).isEqualTo(Calendar.TUESDAY);
    assertThat(Opal.Day.WEDNESDAY.getNumber()).isEqualTo(Calendar.WEDNESDAY);
    assertThat(Opal.Day.THURSDAY.getNumber()).isEqualTo(Calendar.THURSDAY);
    assertThat(Opal.Day.SATURDAY.getNumber()).isEqualTo(Calendar.SATURDAY);
    assertThat(config.getSchedule(vt).getHours()).isEqualTo(9);
    assertThat(config.getSchedule(vt).getMinutes()).isEqualTo(15);

    // at Friday at 9:15
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 12, 0);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(!config.isReadyForIndexing(vt, index)).isTrue();

    // at Friday at 9:15
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 20, 9, 0);
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(!config.isReadyForIndexing(vt, index)).isTrue();

    // at Friday at 9:15
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 23, 9, 0); // friday
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(!config.isReadyForIndexing(vt, index)).isTrue();

    // at Friday at 9:15
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 23, 9, 15); // friday
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

    // at Friday at 9:15
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 24, 9, 15); // friday
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(!config.isReadyForIndexing(vt, index)).isTrue();

    // at Friday at 9:15
    index = createMock(ValueTableIndex.class);
    now.set(2012, Calendar.NOVEMBER, 30, 9, 15); // friday
    expect(index.isUpToDate()).andReturn(false).atLeastOnce();
    expect(index.now()).andReturn(now).atLeastOnce();
    EasyMock.replay(index);

    assertThat(config.isReadyForIndexing(vt, index)).isTrue();

  }
}
