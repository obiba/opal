package org.obiba.opal.project.security;

import javax.annotation.Nonnull;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceUpdateListener;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableUpdateListener;
import org.obiba.opal.core.service.SubjectAclService;
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
    subjectAclService.deleteNodeHierarchyPermissions("opal", "/datasource/" + datasource.getName());
    subjectAclService.deleteNodeHierarchyPermissions("opal", "/project/" + datasource.getName());
    subjectAclService.deleteNodeHierarchyPermissions("opal", "/files/projects/" + datasource.getName());
  }

  @Override
  public void onDelete(@Nonnull ValueTable vt) {
    // remove all permissions related to the table
    String node = "/datasource/" + vt.getDatasource().getName() +
        (vt.isView() ? "/view/" + vt.getName() : "/table/" + vt.getName());
    subjectAclService.deleteNodeHierarchyPermissions("opal", node);
  }

}
