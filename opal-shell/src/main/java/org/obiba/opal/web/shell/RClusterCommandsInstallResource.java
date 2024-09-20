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
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.Dtos;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.RPackageCommandOptions;
import org.obiba.opal.shell.web.RPackageCommandOptionsDtoImpl;
import org.obiba.opal.web.model.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/service/r/cluster/{cname}/commands/_install")
public class RClusterCommandsInstallResource extends AbstractCommandsResource {

  private static final Logger log = LoggerFactory.getLogger(RClusterCommandsInstallResource.class);

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  @Autowired
  @Qualifier("web")
  private CommandRegistry commandRegistry;

  @POST
  public Response installPackage(@PathParam("cname") String name, Commands.RPackageCommandOptionsDto optionsDto) {
    if (!opalGeneralConfigService.getConfig().isAllowRPackageManagement())
      return Response.status(Response.Status.FORBIDDEN).build();

    Command<RPackageCommandOptions> rCommand = commandRegistry.newCommand("r-package");
    rCommand.setOptions(new RPackageCommandOptionsDtoImpl(optionsDto));
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
