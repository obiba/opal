/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.shell;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.Dtos;
import org.obiba.opal.shell.service.CommandJobService;
import org.obiba.opal.shell.service.NoSuchCommandJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/project/{name}/command/{id}")
public class ProjectCommandResource {

  @PathParam("name")
  private String name;

  @PathParam("id")
  private Integer id;

  @Autowired
  protected CommandJobService commandJobService;

  @GET
  public Response getCommand() {
    CommandJob commandJob = getCommandJob();

    return commandJob == null
        ? Response.status(Response.Status.NOT_FOUND).build()
        : Response.ok(Dtos.asDto(commandJob)).build();
  }

  @DELETE
  public Response deleteCommand() {
    try {
      CommandJob commandJob = getCommandJob();
      if(commandJob == null) {
        Response.status(Response.Status.NOT_FOUND).build();
      }
      commandJobService.deleteCommand(id);
      return Response.ok().build();
    } catch(NoSuchCommandJobException ex) {
      return Response.status(Response.Status.NOT_FOUND).entity("DeleteCommand_NotFound").build();
    } catch(IllegalStateException ex) {
      return Response.status(Response.Status.BAD_REQUEST).entity("DeleteCommand_BadRequest_NotDeletable").build();
    }
  }

  private CommandJob getCommandJob() {
    CommandJob commandJob = commandJobService.getCommand(id);

    return commandJob == null || !commandJob.hasProject() || !commandJob.getProject().equals(name) ? null : commandJob;
  }
}
