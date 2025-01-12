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
import org.obiba.opal.web.BaseResource;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.web.project.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Path("/project/{name}/resources")
public class ProjectResourceReferencesResource implements BaseResource {

  private final static Authorizer authorizer = new ShiroAuthorizer();

  private final ResourceReferenceService resourceReferenceService;

  @Autowired
  public ProjectResourceReferencesResource(ResourceReferenceService resourceReferenceService) {
    this.resourceReferenceService = resourceReferenceService;
  }

  @GET
  public List<Projects.ResourceReferenceDto> list(@PathParam("name") String name, @QueryParam("safe") @DefaultValue("true") Boolean safe) {
    return resourceReferenceService.getResourceReferences(name).stream()
        .sorted(Comparator.comparing(ResourceReference::getName))
        .map(ref -> Dtos.asDto(ref, resourceReferenceService.createResource(ref), !safe && isEditable(name, ref.getName())))
        .collect(Collectors.toList());
  }

  @POST
  public Response createResourceReference(@Context UriInfo uriInfo, @PathParam("name") String name, Projects.ResourceReferenceDto referenceDto) {
    if (!name.equals(referenceDto.getProject()))
      throw new IllegalArgumentException("Expected project name: " + name);

    ResourceReference reference = Dtos.fromDto(referenceDto);
    resourceReferenceService.save(reference);
    URI uri = uriInfo.getBaseUriBuilder().path("project").path(name).path("resource").path(reference.getName()).build();
    return Response.created(uri).build();
  }

  @DELETE
  public Response deleteAll(@PathParam("name") String name, @QueryParam("names") List<String> names) {
    if (names != null && !names.isEmpty())
      names.forEach(n -> resourceReferenceService.delete(name, n));
    else
      resourceReferenceService.deleteAll(name);
    return Response.noContent().build();
  }

  private boolean isEditable(String project, String name) {
    return authorizer.isPermitted("rest:/project/" + project + "/resource/" + name + ":PUT");
  }
}
