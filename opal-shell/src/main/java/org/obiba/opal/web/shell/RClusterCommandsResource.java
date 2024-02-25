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

import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.Dtos;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.*;
import org.obiba.opal.shell.web.*;
import org.obiba.opal.web.model.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.List;

@Component
@Transactional
@Scope("request")
@Path("/service/r/cluster/{cname}/commands")
@SuppressWarnings("OverlyCoupledClass")
public class RClusterCommandsResource extends AbstractCommandsResource {

  private static final Logger log = LoggerFactory.getLogger(RClusterCommandsResource.class);

  @Autowired
  @Qualifier("web")
  private CommandRegistry commandRegistry;

  @GET
  public List<Commands.CommandStateDto> getCommands(@PathParam("cname") String name) {
    List<Commands.CommandStateDto> commandDtoList = new ArrayList<>();

    List<CommandJob> history = commandJobService.getHistory();
    for(CommandJob commandJob : history) {
      if(commandJob.hasProject() && commandJob.getProject().equals(name)) {
        commandDtoList.add(Dtos.asDto(commandJob));
      }
    }

    return commandDtoList;
  }

  //
  //
  //

  @POST
  @Path("/_update")
  public Response updateAllPackages(@PathParam("cname") String name) {
    Commands.RPackagesCommandOptionsDto optionsDto = Commands.RPackagesCommandOptionsDto.newBuilder()
        .setCluster(name).build();
    Command<RPackagesCommandOptions> rCommand = commandRegistry.newCommand("r-packages");
    rCommand.setOptions(new RPackagesCommandOptionsDtoImpl(optionsDto));
    return launchCommand(rCommand);
  }

  @POST
  @Path("/_install")
  public Response installPackage(@PathParam("cname") String name, Commands.RPackageCommandOptionsDto optionsDto) {
    Command<RPackageCommandOptions> rCommand = commandRegistry.newCommand("r-package");
    rCommand.setOptions(new RPackageCommandOptionsDtoImpl(optionsDto));
    return launchCommand(rCommand);
  }

  @Override
  protected Response buildLaunchCommandResponse(Integer jobId) {
    return Response.created(
            UriBuilder.fromPath("/").path(WebShellResource.class).path(WebShellResource.class, "getCommand").build(jobId))
        .build();
  }
}
