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

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.Dtos;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.RPackagesCommandOptions;
import org.obiba.opal.shell.web.RPackagesCommandOptionsDtoImpl;
import org.obiba.opal.web.model.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/service/r/cluster/{cname}/commands/_update")
public class RClusterCommandsUpdateResource extends AbstractCommandsResource {

  private static final Logger log = LoggerFactory.getLogger(RClusterCommandsUpdateResource.class);

  @Autowired
  @Qualifier("web")
  private CommandRegistry commandRegistry;

  @POST
  public Response updateAllPackages(@PathParam("cname") String name) {
    Commands.RPackagesCommandOptionsDto optionsDto = Commands.RPackagesCommandOptionsDto.newBuilder()
        .setCluster(name).build();
    Command<RPackagesCommandOptions> rCommand = commandRegistry.newCommand("r-packages");
    rCommand.setOptions(new RPackagesCommandOptionsDtoImpl(optionsDto));
    return launchCommand(rCommand);
  }

  @Override
  protected Response buildLaunchCommandResponse(CommandJob commandJob) {
    return Response.created(
            UriBuilder.fromPath("/").path(WebShellResource.class).path(WebShellResource.class, "getCommand").build(commandJob.getId()))
        .entity(Dtos.asDto(commandJob))
        .build();
  }
}
