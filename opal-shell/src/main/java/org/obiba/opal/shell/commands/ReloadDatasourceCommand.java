/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.shell.commands;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.service.DatasourceLoaderService;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.ProjectsState;
import org.obiba.opal.core.service.ProjectsState.State;
import org.obiba.opal.shell.commands.options.ReloadDatasourceCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@CommandUsage(description = "Reload a project's underlying datasource.", syntax = "Syntax: reload --project PROJECT")
public class ReloadDatasourceCommand extends AbstractOpalRuntimeDependentCommand<ReloadDatasourceCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(ReloadDatasourceCommand.class);

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private ViewManager viewManager;

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private ProjectsState projectsState;

  @Autowired
  private DatasourceLoaderService datasourceLoaderService;

  @Override
  public int execute() {
    String projectName = getOptions().getProject();
    Project project = orientDbService.findUnique(new Project(projectName));

    if (project != null && MagmaEngine.get().hasDatasource(project.getName())) {
      projectsState.updateProjectState(projectName, State.LOADING);

      transactionTemplate.execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          Datasource datasource = MagmaEngine.get().getDatasource(project.getName());
          MagmaEngine.get().removeDatasource(datasource);
          viewManager.unregisterDatasource(datasource.getName());

          try {
            datasourceLoaderService.reloadDatasource(project);
            projectsState.updateProjectState(projectName, State.READY);
          } catch (Exception e) {
            log.error("{}: loading datasource of project {} failed for database: {}", getName(), project.getName(), project.getDatabase(), e);
            projectsState.updateProjectState(project.getName(), ProjectsState.State.ERRORS);
          }
        }
      });
    }
    return CommandResultCode.SUCCESS;
  }
}
