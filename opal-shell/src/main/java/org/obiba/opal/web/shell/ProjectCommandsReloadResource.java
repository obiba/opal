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
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.web.ReloadDatasourceCommandOptionsDtoImpl;
import org.obiba.opal.web.model.Commands;
import org.obiba.opal.web.support.ConflictingRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/project/{name}/commands/_reload")
public class ProjectCommandsReloadResource extends AbstractProjectCommandsResource {

  private static final Logger log = LoggerFactory.getLogger(ProjectCommandsReloadResource.class);

  @PathParam("name")
  protected String name;

  @Override
  public String getName() {
    return name;
  }

  @POST
  public Response reloadProject() {
    if (checkCommandIsBlocked(name, true)) throw new ConflictingRequestException("ProjectMomentarilyNotReloadable", name);

    Command<Object> reloadCommand = commandRegistry.newCommand("reload");
    reloadCommand.setOptions(new ReloadDatasourceCommandOptionsDtoImpl(Commands.ReloadDatasourceCommandOptionsDto.newBuilder().setProject(name).build()));
    return launchCommand(reloadCommand);
  }
}
