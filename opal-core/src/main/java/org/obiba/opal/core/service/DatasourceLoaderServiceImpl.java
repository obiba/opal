/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.beust.jcommander.internal.Lists;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.datasource.nil.support.NullDatasourceFactory;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.repository.ProjectRepository;
import org.obiba.opal.core.security.BackgroundJobServiceAuthToken;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class DatasourceLoaderServiceImpl implements DatasourceLoaderService {

  private static final Logger log = LoggerFactory.getLogger(DatasourceLoaderServiceImpl.class);

  private final TransactionTemplate transactionTemplate;

  private final ProjectsState projectsState;

  private final DatabaseRegistry databaseRegistry;

  private final ProjectRepository projectRepository;

  private final BlockingQueue<LoadRequest> datasourceLoadQueue = new LinkedBlockingQueue<>();

  private List<DatasourceLoader> datasourceLoaders = Lists.newArrayList();

  private volatile boolean stopped;

  @Autowired
  public DatasourceLoaderServiceImpl(TransactionTemplate transactionTemplate, ProjectsState projectsState, DatabaseRegistry databaseRegistry,
                                     ProjectRepository projectRepository) {
    this.transactionTemplate = transactionTemplate;
    this.projectsState = projectsState;
    this.databaseRegistry = databaseRegistry;
    this.projectRepository = projectRepository;
  }

  @Override
  public void start() {
    startDatasourceLoaderThreads();
  }

  @Override
  public void stop() {
    stopped = true;
    terminateDatasourceLoaderThreads();
  }

  private void startDatasourceLoaderThreads() {
    datasourceLoaders.add(newDatasourceLoader("1"));
    //datasourceLoaders.add(newDatasourceLoader("2"));
    //datasourceLoaders.add(newDatasourceLoader("3"));
  }

  private DatasourceLoader newDatasourceLoader(String id) {
    DatasourceLoader datasourceLoader = new DatasourceLoader();
    datasourceLoader.setName("Datasource Loader " + id);
    datasourceLoader.setPriority(Thread.MIN_PRIORITY);
    datasourceLoader.start();
    return datasourceLoader;
  }

  public void terminateDatasourceLoaderThreads() {
    datasourceLoaders.forEach(datasourceLoader -> {
      try {
        if (datasourceLoader != null && datasourceLoader.isAlive()) datasourceLoader.interrupt();
      } catch (Exception e) {
        // ignore
      }
    });
  }

  @Override
  public void reloadDatasource(final Project project) {
    transactionTemplate.execute(status -> {
      log.info("  Datasource load start: {}", project.getName());
      DatasourceFactory dataSourceFactory;
      if (project.hasDatabase()) {
        Database database = databaseRegistry.getDatabase(project.getDatabase());
        dataSourceFactory = databaseRegistry.createDatasourceFactory(project.getName(), database);
      } else {
        dataSourceFactory = new NullDatasourceFactory();
        dataSourceFactory.setName(project.getName());
      }

      if (MagmaEngine.get().hasDatasource(project.getName())) {
        MagmaEngine.get().removeDatasource(MagmaEngine.get().getDatasource(project.getName()));
      }

      MagmaEngine.get().addDatasource(dataSourceFactory);
      log.info("Datasource load end: {} ({})", project.getName(), dataSourceFactory.getClass().getSimpleName());
      return dataSourceFactory;
    });
  }

  @Override
  public void registerDatasource(Project project) {
    datasourceLoadQueue.offer(new LoadRequest(project, LoadRequest.MAX_LOAD_ATTEMPTS));
  }

  /**
   * A datasource that could not be loaded, typically because its database was not reachable when Opal started, is
   * loaded again once its database is back, without an administrator having to reload the project.
   */
  @Scheduled(initialDelayString = "${org.obiba.opal.datasource.retryInterval}",
      fixedDelayString = "${org.obiba.opal.datasource.retryInterval}")
  public void retryFailedDatasources() {
    if (stopped) return;
    for (Project project : projectRepository.findAll()) {
      // LOADING keeps the project from being queued twice, or reloaded by a user meanwhile
      if (projectsState.updateProjectState(project.getName(), ProjectsState.State.ERRORS, ProjectsState.State.LOADING)) {
        datasourceLoadQueue.offer(new LoadRequest(project, 1));
      }
    }
  }

  private record LoadRequest(Project project, int maxAttempts) {
    private static final int MAX_LOAD_ATTEMPTS = 5;
  }

  private class DatasourceLoader extends Thread {

    private static final int LOAD_ATTEMPT_DELAY = 1000;

    @Override
    public void run() {
      log.info("{}: started", getName());
      try {
        //noinspection InfiniteLoopStatement
        while (true) {
          LoadRequest request = datasourceLoadQueue.take();
          // as system user: the secured MagmaEngine would not see, hence not replace, a datasource already registered
          try {
            getSystemSubject().execute(() -> load(request));
          } catch (RuntimeException e) {
            // keep the loader alive for the next projects
            log.error("{}: loading datasource of project {} failed", getName(), request.project().getName(), e);
            projectsState.updateProjectState(request.project().getName(), ProjectsState.State.ERRORS);
          }
        }
      } catch (InterruptedException ignored) {
        log.debug("{}: interrupted", getName());
      }
      log.info("{}: Stopped", getName());
    }

    private void load(LoadRequest request) {
      Project project = request.project();
      boolean retry = request.maxAttempts() == 1;
      log.info("{}: loading datasource of project {}", getName(), project.getName());
      int attempt = 1;
      boolean done = false;
      while (!done && attempt <= request.maxAttempts()) {
        try {
          loadAttempt(project);
          done = true;
        } catch (Exception e) {
          if (retry) {
            // retried every interval while the database is down, a stack trace each time would flood the log
            log.warn("{}: loading datasource of project {} failed again for database {}: {}", getName(), project.getName(), project.getDatabase(), e.getMessage());
          } else {
            log.error("{}: #{} loading datasource of project {} failed for database: {}", getName(), attempt, project.getName(), project.getDatabase(), e);
          }
          attempt++;
          if (attempt > request.maxAttempts()) {
            databaseRegistry.unregister(project.getDatabase(), project.getName());
            projectsState.updateProjectState(project.getName(), ProjectsState.State.ERRORS);
          } else {
            try {
              Thread.sleep(LOAD_ATTEMPT_DELAY);
            } catch (InterruptedException ex) {
              projectsState.updateProjectState(project.getName(), ProjectsState.State.ERRORS);
              done = true;
              // so that the loader stops instead of waiting for the next project
              Thread.currentThread().interrupt();
            }
          }
        }
      }
    }

    private Subject getSystemSubject() {
      return new Subject.Builder()
          .principals(SecurityUtils.getSecurityManager().authenticate(BackgroundJobServiceAuthToken.INSTANCE).getPrincipals())
          .authenticated(true).buildSubject();
    }

    private void loadAttempt(Project project) {
        reloadDatasource(project);
        projectsState.updateProjectState(project.getName(), ProjectsState.State.READY);

    }
  }

}
