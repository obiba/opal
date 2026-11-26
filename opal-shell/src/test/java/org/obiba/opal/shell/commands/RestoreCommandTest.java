/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.shell.commands;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RestoreCommandTest {

  @Test
  public void test_project_nodes_are_retargeted() {
    assertEquals("/project/target", RestoreCommand.toProjectNode("/project/source", "target"));
    assertEquals("/project/target/resources", RestoreCommand.toProjectNode("/project/source/resources", "target"));
    assertEquals("/project/target/resource/res1", RestoreCommand.toProjectNode("/project/source/resource/res1", "target"));
    assertEquals("/project/target/vcf-store", RestoreCommand.toProjectNode("/project/source/vcf-store", "target"));
    assertEquals("/datasource/target", RestoreCommand.toProjectNode("/datasource/source", "target"));
    assertEquals("/datasource/target/table/T1", RestoreCommand.toProjectNode("/datasource/source/table/T1", "target"));
    assertEquals("/datasource/target/view/V1", RestoreCommand.toProjectNode("/datasource/source/view/V1", "target"));
    assertEquals("/datasource/target/table/T1/variable/V1",
        RestoreCommand.toProjectNode("/datasource/source/table/T1/variable/V1", "target"));
  }

  @Test
  public void test_nodes_outside_project_are_rejected() {
    assertNull(RestoreCommand.toProjectNode("/", "target"));
    assertNull(RestoreCommand.toProjectNode("/datashield", "target"));
    assertNull(RestoreCommand.toProjectNode("/r", "target"));
    assertNull(RestoreCommand.toProjectNode("/datashield/profile/default", "target"));
    assertNull(RestoreCommand.toProjectNode("/files/projects/source", "target"));
    assertNull(RestoreCommand.toProjectNode("/project", "target"));
    assertNull(RestoreCommand.toProjectNode("/project/", "target"));
    assertNull(RestoreCommand.toProjectNode("/project/source/", "target"));
    assertNull(RestoreCommand.toProjectNode("/project/source/other", "target"));
    assertNull(RestoreCommand.toProjectNode("/project/source/resource/a/b", "target"));
    assertNull(RestoreCommand.toProjectNode("/datasource/source/table", "target"));
    assertNull(RestoreCommand.toProjectNode("/datasource/source/table/T1/other", "target"));
    assertNull(RestoreCommand.toProjectNode("/project/source/../other", "target"));
    assertNull(RestoreCommand.toProjectNode("x/project/source", "target"));
  }
}
