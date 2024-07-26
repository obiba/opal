/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.project.resource;


import org.obiba.magma.security.Authorizer;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.opal.core.domain.ResourceReference;
import org.obiba.opal.core.service.ResourceReferenceService;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.spi.r.RServerException;
import org.obiba.opal.spi.r.ResourceAssignROperation;
import org.obiba.opal.web.BaseResource;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.web.project.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Component
@Path("/project/{project}/resource/{name}")
public class ProjectResourceReferenceResource implements BaseResource {

  private final static Authorizer authorizer = new ShiroAuthorizer();

  private final ResourceReferenceService resourceReferenceService;

  private final RServerManagerService rServerManagerService;

  @Autowired
  public ProjectResourceReferenceResource(ResourceReferenceService resourceReferenceService, RServerManagerService rServerManagerService) {
    this.resourceReferenceService = resourceReferenceService;
    this.rServerManagerService = rServerManagerService;
  }

  @GET
  public Projects.ResourceReferenceDto get(@PathParam("project") String project, @PathParam("name") String name) {
    ResourceReference reference = resourceReferenceService.getResourceReference(project, name);
    return Dtos.asDto(reference, resourceReferenceService.createResource(reference), isEditable(project, name));
  }

  @PUT
  public Response update(@PathParam("project") String project, @PathParam("name") String name, Projects.ResourceReferenceDto referenceDto) {
    // check same project
    if (!project.equals(referenceDto.getProject()))
      throw new IllegalArgumentException("Expecting a resource of project: " + project);
    // check it is not a creation
    ResourceReference originalReference = resourceReferenceService.getResourceReference(project, name);
    ResourceReference updatedReference = Dtos.fromDto(referenceDto);
    updatedReference.setCreated(originalReference.getCreated());
    // note that it can be renamed
    resourceReferenceService.delete(originalReference);
    resourceReferenceService.save(updatedReference);
    if (!originalReference.getName().equals(updatedReference.getName())) {
      // TODO change permissions for the new name
    }
    return Response.ok().build();
  }

  @PUT
  @Path("_test")
  public Response test(@PathParam("project") String project, @PathParam("name") String name) throws RServerException {
    ResourceAssignROperation rop = resourceReferenceService.asAssignOperation(project, name, "rsrc");
    // test in the R server where the resource provider is defined
    rServerManagerService.getRServerWithPackages(rop.getRequiredPackages()).execute(rop);
    return Response.ok().build();
  }

  @DELETE
  public Response delete(@PathParam("project") String project, @PathParam("name") String name) {
    resourceReferenceService.delete(project, name);
    return Response.noContent().build();
  }

  private boolean isEditable(String project, String name) {
    return authorizer.isPermitted("rest:/project/" + project + "/resource/" + name + ":PUT");
  }

}
