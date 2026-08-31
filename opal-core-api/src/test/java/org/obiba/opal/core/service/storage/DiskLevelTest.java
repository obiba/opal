/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service.storage;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class DiskLevelTest {

  @Test
  public void test_severity_order() {
    assertThat(DiskLevel.CRITICAL.isAtLeast(DiskLevel.DEGRADED)).isTrue();
    assertThat(DiskLevel.DEGRADED.isAtLeast(DiskLevel.DEGRADED)).isTrue();
    assertThat(DiskLevel.WARN.isAtLeast(DiskLevel.DEGRADED)).isFalse();
    assertThat(DiskLevel.OK.isAtLeast(DiskLevel.WARN)).isFalse();
  }

  @Test
  public void test_unknown_never_blocks() {
    // a checker that cannot see must not be the reason a server stops accepting work
    for(DiskLevel level : DiskLevel.values()) {
      assertThat(DiskLevel.UNKNOWN.isAtLeast(level)).isFalse();
    }
  }

  @Test
  public void test_worst_keeps_the_more_severe() {
    assertThat(DiskLevel.OK.worst(DiskLevel.CRITICAL)).isEqualTo(DiskLevel.CRITICAL);
    assertThat(DiskLevel.CRITICAL.worst(DiskLevel.OK)).isEqualTo(DiskLevel.CRITICAL);
    assertThat(DiskLevel.WARN.worst(DiskLevel.WARN)).isEqualTo(DiskLevel.WARN);
  }

  @Test
  public void test_an_unmeasured_volume_neither_hides_nor_invents_a_problem() {
    // a volume that cannot be read loses to every measured one, in both directions
    assertThat(DiskLevel.UNKNOWN.worst(DiskLevel.OK)).isEqualTo(DiskLevel.OK);
    assertThat(DiskLevel.UNKNOWN.worst(DiskLevel.CRITICAL)).isEqualTo(DiskLevel.CRITICAL);
    assertThat(DiskLevel.OK.worst(DiskLevel.UNKNOWN)).isEqualTo(DiskLevel.OK);
    // and folding over volumes that could none of them be read stays UNKNOWN
    assertThat(DiskLevel.UNKNOWN.worst(DiskLevel.UNKNOWN)).isEqualTo(DiskLevel.UNKNOWN);
  }
}
