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

import com.google.common.collect.Sets;
import org.junit.Test;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

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
    // a project item under a datasource node, and the other way round
    assertNull(RestoreCommand.toProjectNode("/datasource/source/resources", "target"));
    assertNull(RestoreCommand.toProjectNode("/datasource/source/vcf-store", "target"));
    assertNull(RestoreCommand.toProjectNode("/datasource/source/resource/r", "target"));
    assertNull(RestoreCommand.toProjectNode("/project/source/table/T1", "target"));
    assertNull(RestoreCommand.toProjectNode("/project/source/view/V1/variable/V1", "target"));
  }

  @Test
  public void test_permissions_applicable_to_node_are_allowed() {
    assertTrue(RestoreCommand.isAllowed("/project/p", List.of("PROJECT_ALL")));
    assertTrue(RestoreCommand.isAllowed("/project/p/resources", List.of("RESOURCES_VIEW")));
    assertTrue(RestoreCommand.isAllowed("/project/p/resource/r", List.of("RESOURCE_ALL")));
    assertTrue(RestoreCommand.isAllowed("/project/p/vcf-store", List.of("VCF_STORE_VALUES")));
    assertTrue(RestoreCommand.isAllowed("/datasource/p", List.of("DATASOURCE_VIEW", "TABLE_ADD")));
    assertTrue(RestoreCommand.isAllowed("/datasource/p/table/t", List.of("TABLE_READ")));
    assertTrue(RestoreCommand.isAllowed("/datasource/p/view/v", List.of("TABLE_VALUES")));
    assertTrue(RestoreCommand.isAllowed("/datasource/p/table/t/variable/x", List.of("VARIABLE_READ")));
  }

  @Test
  public void test_permissions_of_another_kind_are_rejected() {
    assertFalse(RestoreCommand.isAllowed("/project/p", List.of("SYSTEM_ALL")));
    assertFalse(RestoreCommand.isAllowed("/project/p", List.of("PROJECT_ALL", "SYSTEM_ALL")));
    assertFalse(RestoreCommand.isAllowed("/project/p", List.of("DATASHIELD_ALL")));
    assertFalse(RestoreCommand.isAllowed("/project/p", List.of("TABLE_ALL")));
    assertFalse(RestoreCommand.isAllowed("/project/p/resources", List.of("RESOURCE_ALL")));
    assertFalse(RestoreCommand.isAllowed("/datasource/p", List.of("PROJECT_ALL")));
    assertFalse(RestoreCommand.isAllowed("/datasource/p/table/t", List.of("DATASOURCE_ALL")));
    assertFalse(RestoreCommand.isAllowed("/datasource/p/table/t/variable/x", List.of("TABLE_ALL")));
    assertFalse(RestoreCommand.isAllowed("/project/p", List.of()));
    assertFalse(RestoreCommand.isAllowed("/", List.of("SYSTEM_ALL")));
    assertFalse(RestoreCommand.isAllowed("/datasource/p/resources", List.of("VARIABLE_READ")));
    assertFalse(RestoreCommand.isAllowed("/project/p/table/t", List.of("RESOURCE_VIEW")));
  }

  @Test
  public void test_restored_permissions_replace_those_of_the_subject_on_the_item() {
    Set<String> rows = Sets.newHashSet(
        row("/datasource/p/table/t1", "bob", "TABLE_READ"),
        row("/datasource/p/table/t1", "alice", "TABLE_READ"),
        row("/datasource/p/table/t2", "bob", "TABLE_READ"),
        row("/project/p", "admin", "PROJECT_ALL"));

    RestoreCommand.replacePermissions(store(rows), List.of(
        acl("/datasource/p/table/t1", "bob", "TABLE_ALL"),
        acl("/datasource/p/table/t3", "carol", "TABLE_VALUES")));

    assertEquals(Sets.newHashSet(
        // replaced
        row("/datasource/p/table/t1", "bob", "TABLE_ALL"),
        // another subject on the same item, the same subject on another item and a subject not in the archive: kept
        row("/datasource/p/table/t1", "alice", "TABLE_READ"),
        row("/datasource/p/table/t2", "bob", "TABLE_READ"),
        row("/project/p", "admin", "PROJECT_ALL"),
        // new
        row("/datasource/p/table/t3", "carol", "TABLE_VALUES")), rows);
  }

  @Test
  public void test_entries_of_the_same_subject_and_item_are_all_restored() {
    Set<String> rows = Sets.newHashSet(row("/datasource/p", "bob", "DATASOURCE_VIEW"));

    RestoreCommand.replacePermissions(store(rows), List.of(
        acl("/datasource/p", "bob", "TABLE_ADD"),
        acl("/datasource/p", "bob", "DATASOURCE_ALL")));

    assertEquals(Sets.newHashSet(
        row("/datasource/p", "bob", "TABLE_ADD"),
        row("/datasource/p", "bob", "DATASOURCE_ALL")), rows);
  }

  private static String row(String node, String user, String permission) {
    return node + "|" + user + "|" + permission;
  }

  private static Opal.Acl acl(String node, String user, String permission) {
    return Opal.Acl.newBuilder().setDomain("opal").setResource(node).addActions(permission)
        .setSubject(Opal.Subject.newBuilder().setPrincipal(user).setType(Opal.Subject.SubjectType.USER)).build();
  }

  /**
   * A permission store holding user permission rows, as the configuration database does one per permission.
   */
  @SuppressWarnings("unchecked")
  private static SubjectAclService store(Set<String> rows) {
    SubjectAclService service = mock(SubjectAclService.class);
    doAnswer(invocation -> {
      String node = (String) invocation.getArguments()[1];
      SubjectAcl.Subject subject = (SubjectAcl.Subject) invocation.getArguments()[2];
      rows.removeIf(row -> row.startsWith(node + "|" + subject.getPrincipal() + "|"));
      return null;
    }).when(service).deleteSubjectPermissions(anyString(), anyString(), any(SubjectAcl.Subject.class));
    doAnswer(invocation -> {
      String node = (String) invocation.getArguments()[1];
      SubjectAcl.Subject subject = (SubjectAcl.Subject) invocation.getArguments()[2];
      for (String permission : (Iterable<String>) invocation.getArguments()[3])
        rows.add(row(node, subject.getPrincipal(), permission));
      return null;
    }).when(service).addSubjectPermissions(anyString(), anyString(), any(SubjectAcl.Subject.class), any(Iterable.class));
    return service;
  }
}
