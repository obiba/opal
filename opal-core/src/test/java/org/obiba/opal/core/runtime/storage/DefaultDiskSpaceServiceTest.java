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
    ReflectionTestUtils.setField(service, "warnBytes", 5 * GIB);
    ReflectionTestUtils.setField(service, "degradedBytes", 2 * GIB);
    ReflectionTestUtils.setField(service, "criticalBytes", 512 * 1024 * 1024L);
  }

  @Test
  public void test_levels_on_a_small_volume() {
    long total = 20 * GIB;
    assertThat(service.levelOf(total, 10 * GIB)).isEqualTo(DiskLevel.OK);
    assertThat(service.levelOf(total, 4 * GIB)).isEqualTo(DiskLevel.WARN);
    assertThat(service.levelOf(total, GIB)).isEqualTo(DiskLevel.DEGRADED);
    assertThat(service.levelOf(total, 100 * 1024 * 1024L)).isEqualTo(DiskLevel.CRITICAL);
  }

  @Test
  public void test_the_same_free_space_reads_the_same_on_any_volume() {
    // what a level answers is whether there is room left to work, and that does not depend on how big the disk is.
    // A percentage got this wrong: 15% of a 468 GB volume asked for 70 GB of headroom nothing would ever use.
    for(long total : new long[] { 20 * GIB, 468 * GIB, 10000 * GIB }) {
      assertThat(service.levelOf(total, 10 * GIB)).isEqualTo(DiskLevel.OK);
      assertThat(service.levelOf(total, 4 * GIB)).isEqualTo(DiskLevel.WARN);
      assertThat(service.levelOf(total, GIB)).isEqualTo(DiskLevel.DEGRADED);
      assertThat(service.levelOf(total, 100 * 1024 * 1024L)).isEqualTo(DiskLevel.CRITICAL);
    }
  }

  @Test
  public void test_the_reading_that_started_this() {
    // 27.64 GB free of 467.89 GB, reported as WARN by the percentage thresholds and four gigabytes away from having
    // its imports refused, on a volume with ample room to operate
    assertThat(service.levelOf(467_890_000_000L, 27_640_000_000L)).isEqualTo(DiskLevel.OK);
  }

  @Test
  public void test_a_volume_that_could_not_be_read_is_unknown() {
    // and UNKNOWN blocks nothing, so a broken reading is not an outage
    assertThat(service.levelOf(-1, -1)).isEqualTo(DiskLevel.UNKNOWN);
    assertThat(service.levelOf(0, 0)).isEqualTo(DiskLevel.UNKNOWN);
    assertThat(DiskLevel.UNKNOWN.isAtLeast(DiskLevel.DEGRADED)).isFalse();
  }
}
