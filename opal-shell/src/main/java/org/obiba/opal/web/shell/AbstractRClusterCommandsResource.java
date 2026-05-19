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

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.Dtos;
import org.obiba.opal.shell.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRClusterCommandsResource extends AbstractCommandsResource {

  private static final Logger log = LoggerFactory.getLogger(AbstractRClusterCommandsResource.class);

  protected abstract String getName();

  @Override
  protected CommandJob newCommandJob(String jobName, Command<?> command) {
    CommandJob job = super.newCommandJob(jobName, command);
    job.setRCluster(getName());
    return job;
  }

  @Override
  protected Response buildLaunchCommandResponse(CommandJob commandJob) {
    return Response.created(
            UriBuilder.fromPath("/").path(WebShellResource.class).path(WebShellResource.class, "getCommand").build(commandJob.getId()))
        .entity(Dtos.asDto(commandJob))
        .build();
  }
}
