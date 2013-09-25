/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.project;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.project.NoSuchProjectException;
import org.obiba.opal.project.ProjectService;
import org.obiba.opal.web.model.Projects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;

@Component
@Scope("request")
@Path("/project/{name}")
public class ProjectResource {

  private final ProjectService projectService;

  private final ViewManager viewManager;

  private final OpalRuntime opalRuntime;

  @Autowired
  public ProjectResource(OpalRuntime opalRuntime, ProjectService projectService, ViewManager viewManager) {
    this.opalRuntime = opalRuntime;
    this.projectService = projectService;
    this.viewManager = viewManager;
  }

  @PathParam("name")
  private String name;

  @GET
  public Projects.ProjectDto get(@QueryParam("counts") @DefaultValue("true") Boolean counts) {
    if(MagmaEngine.get().hasDatasource(name)) {
      Datasource ds = MagmaEngine.get().getDatasource(name);
      return Dtos.asDto(projectService.getOrCreateProject(ds), ds, projectService.getProjectDirectoryPath(name), counts)
          .build();
    }
    throw new NoSuchProjectException(name);
  }

  @PUT
  public Response update(Projects.ProjectDto projectDto) {
    // will throw a no such project exception
    projectService.getProject(name);

    if(!name.equals(projectDto.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    projectService.addOrReplaceProject(Dtos.fromDto(projectDto));

    return Response.ok().build();
  }

  @DELETE
  public Response delete() throws FileSystemException {
    // silently ignore project not found
    if(projectService.hasProject(name)) {
      projectService.removeProject(name);
    }

    // TODO remove all permissions, index etc.
    if(MagmaEngine.get().hasDatasource(name)) {
      Datasource ds = MagmaEngine.get().getDatasource(name);
      // disconnect datasource
      MagmaEngine.get().removeDatasource(ds);
      // remove all views
      viewManager.removeAllViews(ds.getName());
      // remove all tables
      for(ValueTable table : ds.getValueTables()) {
        if(!table.isView() && ds.canDropTable(table.getName())) {
          ds.dropTable(table.getName());
        }
      }
      // remove datasource
      if(ds.canDrop()) ds.drop();

    }
    // remove project folder
    deleteFolder(opalRuntime.getFileSystem().getRoot().resolveFile("/projects/" + name));

    return Response.ok().build();
  }

  private void deleteFolder(FileObject folder) throws FileSystemException {
    if(!folder.isWriteable()) return;

    FileObject[] files = folder.getChildren();
    for(FileObject file : files) {
      if(file.getType() == FileType.FOLDER) {
        deleteFolder(file);
      } else if(file.isWriteable()) {
        file.delete();
      }
    }
    if(folder.getChildren().length == 0) {
      folder.delete();
    }
  }

}
