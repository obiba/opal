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

import com.google.common.eventbus.Subscribe;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.event.*;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nonnull;

@Component
public class ProjectPermissionsUpdateListener {

  private final SubjectAclService subjectAclService;

  @Autowired
  public ProjectPermissionsUpdateListener(SubjectAclService subjectAclService) {
    this.subjectAclService = subjectAclService;
  }

  @Subscribe
  public void onDatasourceDeleted(@Nonnull DatasourceDeletedEvent event) {
    // remove all permissions related to the project and the datasource
    Datasource datasource = event.getDatasource();
    subjectAclService.deleteNodePermissions("/datasource/" + datasource.getName());
    subjectAclService.deleteNodePermissions("/project/" + datasource.getName());
    subjectAclService.deleteNodePermissions("/files/projects/" + datasource.getName());
  }

  @Subscribe
  public void onValueTableRenamed(ValueTableRenamedEvent event) {
    ValueTable vt = event.getValueTable();
    Iterable<SubjectAclService.Permissions> perms = subjectAclService
        .getNodeHierarchyPermissions("opal", getNode(vt), null);
    onDelete(vt);
    String prefix = vt.isView() ? "/view/" : "/table/";
    String originalStr = prefix + vt.getName();
    String newStr = prefix + event.getNewName();
    for(SubjectAclService.Permissions perm : perms) {
      subjectAclService
          .addSubjectPermissions(perm.getDomain(), perm.getNode().replace(originalStr, newStr), perm.getSubject(),
              perm.getPermissions());
    }
  }

  @Subscribe
  public void onVariableRenamed(VariableRenamedEvent event) {
    ValueTable vt = event.getValueTable();
    Variable v = event.getVariable();
    Iterable<SubjectAclService.Permissions> perms = subjectAclService
        .getNodeHierarchyPermissions("opal", getNode(vt, v), null);
    // remove all permissions related to the variable
    subjectAclService.deleteNodePermissions(getNode(vt, v));
    String prefix = "/variable/";
    String originalStr = prefix + v.getName();
    String newStr = prefix + event.getNewName();
    for(SubjectAclService.Permissions perm : perms) {
      subjectAclService
          .addSubjectPermissions(perm.getDomain(), perm.getNode().replace(originalStr, newStr), perm.getSubject(),
              perm.getPermissions());
    }
  }

  @Subscribe
  public void onValueTableDeleted(ValueTableDeletedEvent event) {
    onDelete(event.getValueTable());
  }

  private void onDelete(ValueTable vt) {
    // remove all permissions related to the table
    subjectAclService.deleteNodePermissions(getNode(vt));
  }

  @Subscribe
  public void onVariableDeleted(VariableDeletedEvent event) {
    ValueTable vt = event.getValueTable();Iterable<SubjectAclService.Permissions> perms = subjectAclService
        .getNodeHierarchyPermissions("opal", getNode(vt, event.getVariable()), null);
    // remove all permissions related to the variable
    subjectAclService.deleteNodePermissions(getNode(vt, event.getVariable()));
  }

  private String getNode(ValueTable vt) {
    return "/datasource/" + vt.getDatasource().getName() +
        (vt.isView() ? "/view/" + vt.getName() : "/table/" + vt.getName());
  }

  private String getNode(ValueTable vt, Variable v) {
    return getNode(vt) + "/variable/" + v;
  }

}
