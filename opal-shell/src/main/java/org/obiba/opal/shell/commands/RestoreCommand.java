/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.shell.commands;

import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.ProjectsState;
import org.obiba.opal.core.domain.ProjectsState.State;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.shell.commands.options.RestoreCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@CommandUsage(description = "Restore a project's data.", syntax = "Syntax: restore --project PROJECT --archive FILE [--password PASSWORD]")
public class RestoreCommand extends AbstractOpalRuntimeDependentCommand<RestoreCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(RestoreCommand.class);

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private ProjectsState projectsState;

  @Override
  public int execute() {
    String projectName = getOptions().getProject();
    Project project = orientDbService.findUnique(new Project(projectName));
    projectsState.updateProjectState(projectName, State.BUSY);

    // TODO

    projectsState.updateProjectState(projectName, State.READY);
    return CommandResultCode.SUCCESS;
  }
}
