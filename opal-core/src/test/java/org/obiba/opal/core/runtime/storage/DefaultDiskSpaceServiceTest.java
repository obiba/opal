/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.storage;

import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.service.storage.DiskLevel;
import org.springframework.test.util.ReflectionTestUtils;

import static org.fest.assertions.api.Assertions.assertThat;

public class DefaultDiskSpaceServiceTest {

  private static final long GIB = 1024L * 1024 * 1024;

  private DefaultDiskSpaceService service;

  @Before
  public void setUp() {
    service = new DefaultDiskSpaceService();
    // the shipped defaults, so that the test says what an installation actually does
    ReflectionTestUtils.setField(service, "warnPercent", 15);
    ReflectionTestUtils.setField(service, "warnBytes", 5 * GIB);
    ReflectionTestUtils.setField(service, "degradedPercent", 5);
    ReflectionTestUtils.setField(service, "degradedBytes", GIB);
    ReflectionTestUtils.setField(service, "criticalPercent", 1);
    ReflectionTestUtils.setField(service, "criticalBytes", 256 * 1024 * 1024L);
  }

  @Test
  public void test_a_percentage_and_a_size_whichever_is_larger() {
    // on a small volume the percentage is meaningless and the absolute floor decides
    assertThat(service.requiredFreeSpace(20 * GIB, 5, GIB)).isEqualTo(GIB);
    // on a large one it is the other way round
    assertThat(service.requiredFreeSpace(1000 * GIB, 5, GIB)).isEqualTo(50 * GIB);
  }

  @Test
  public void test_levels_on_a_small_volume() {
    long total = 20 * GIB;
    assertThat(service.levelOf(total, 10 * GIB)).isEqualTo(DiskLevel.OK);
    // 15% of 20 GiB is 3 GiB, but the 5 GiB floor is larger
    assertThat(service.levelOf(total, 4 * GIB)).isEqualTo(DiskLevel.WARN);
    assertThat(service.levelOf(total, 900 * 1024 * 1024L)).isEqualTo(DiskLevel.DEGRADED);
    assertThat(service.levelOf(total, 100 * 1024 * 1024L)).isEqualTo(DiskLevel.CRITICAL);
  }

  @Test
  public void test_levels_on_a_large_volume() {
    long total = 10000 * GIB;
    assertThat(service.levelOf(total, 2000 * GIB)).isEqualTo(DiskLevel.OK);
    // there the percentages are what decide: 15%, 5% and 1% of 10 TiB
    assertThat(service.levelOf(total, 1000 * GIB)).isEqualTo(DiskLevel.WARN);
    assertThat(service.levelOf(total, 300 * GIB)).isEqualTo(DiskLevel.DEGRADED);
    assertThat(service.levelOf(total, 50 * GIB)).isEqualTo(DiskLevel.CRITICAL);
  }

  @Test
  public void test_a_volume_that_could_not_be_read_is_unknown() {
    // and UNKNOWN blocks nothing, so a broken reading is not an outage
    assertThat(service.levelOf(-1, -1)).isEqualTo(DiskLevel.UNKNOWN);
    assertThat(service.levelOf(0, 0)).isEqualTo(DiskLevel.UNKNOWN);
    assertThat(DiskLevel.UNKNOWN.isAtLeast(DiskLevel.DEGRADED)).isFalse();
  }
}
