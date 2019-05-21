package org.obiba.opal.shell.commands;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.ProjectsState;
import org.obiba.opal.core.domain.ProjectsState.State;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.ProjectsServiceImpl;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.shell.commands.options.RefreshCommandOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@CommandUsage(description = "Refresh a project's underlying datasource.", syntax = "Syntax: refresh --project PROJECT")
public class RefreshCommand extends AbstractOpalRuntimeDependentCommand<RefreshCommandOptions> {

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private ViewManager viewManager;

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private ProjectsState projectsState;

  @Override
  public int execute() {
    String projectName = getOptions().getProject();
    Project project = orientDbService.findUnique(new Project(projectName));

    if (project != null && MagmaEngine.get().hasDatasource(project.getName())) {
      projectsState.updateProjectState(projectName, State.REFRESHING);

      transactionTemplate.execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          Datasource datasource = MagmaEngine.get().getDatasource(project.getName());
          MagmaEngine.get().removeDatasource(datasource);
          viewManager.unregisterDatasource(datasource.getName());

          ProjectsServiceImpl.registerDatasource(project, transactionTemplate, databaseRegistry);
        }
      });
    }

    projectsState.updateProjectState(projectName, State.READY);
    return 0;
  }
}
