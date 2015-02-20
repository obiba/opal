package org.obiba.opal.core.service;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceUpdateListener;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableUpdateListener;
import org.obiba.magma.Variable;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProjectPermissionsUpdateListener implements DatasourceUpdateListener, ValueTableUpdateListener {

  private final SubjectAclService subjectAclService;

  @Autowired
  public ProjectPermissionsUpdateListener(SubjectAclService subjectAclService) {
    this.subjectAclService = subjectAclService;
  }

  @Override
  public void onDelete(@Nonnull Datasource datasource) {
    // remove all permissions related to the project and the datasource
    subjectAclService.deleteNodePermissions("/datasource/" + datasource.getName());
    subjectAclService.deleteNodePermissions("/project/" + datasource.getName());
    subjectAclService.deleteNodePermissions("/files/projects/" + datasource.getName());
  }

  @Override
  public void onRename(@NotNull ValueTable vt, String newName) {
    Iterable<SubjectAclService.Permissions> perms = subjectAclService
        .getNodeHierarchyPermissions("opal", getNode(vt), null);
    onDelete(vt);
    String prefix = vt.isView() ? "/view/" : "/table/";
    String originalStr = prefix + vt.getName();
    String newStr = prefix + newName;
    for(SubjectAclService.Permissions perm : perms) {
      subjectAclService
          .addSubjectPermissions(perm.getDomain(), perm.getNode().replace(originalStr, newStr), perm.getSubject(),
              perm.getPermissions());
    }
  }

  @Override
  public void onRename(@Nonnull ValueTable vt, Variable v, String newName) {
    Iterable<SubjectAclService.Permissions> perms = subjectAclService
        .getNodeHierarchyPermissions("opal", getNode(vt, v), null);
    // remove all permissions related to the variable
    subjectAclService.deleteNodePermissions(getNode(vt, v));
    String prefix = "/variable/";
    String originalStr = prefix + v.getName();
    String newStr = prefix + newName;
    for(SubjectAclService.Permissions perm : perms) {
      subjectAclService
          .addSubjectPermissions(perm.getDomain(), perm.getNode().replace(originalStr, newStr), perm.getSubject(),
              perm.getPermissions());
    }
  }

  @Override
  public void onDelete(@Nonnull ValueTable vt) {
    // remove all permissions related to the table
    subjectAclService.deleteNodePermissions(getNode(vt));
  }

  private String getNode(ValueTable vt) {
    return "/datasource/" + vt.getDatasource().getName() +
        (vt.isView() ? "/view/" + vt.getName() : "/table/" + vt.getName());
  }

  private String getNode(ValueTable vt, Variable v) {
    return getNode(vt) + "/variable/" + v;
  }

}
