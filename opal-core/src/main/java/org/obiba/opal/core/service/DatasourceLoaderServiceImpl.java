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
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.datasource.nil.support.NullDatasourceFactory;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  private final BlockingQueue<Project> datasourceLoadQueue = new LinkedBlockingQueue<>();

  private List<DatasourceLoader> datasourceLoaders = Lists.newArrayList();

  @Autowired
  public DatasourceLoaderServiceImpl(TransactionTemplate transactionTemplate, ProjectsState projectsState, DatabaseRegistry databaseRegistry) {
    this.transactionTemplate = transactionTemplate;
    this.projectsState = projectsState;
    this.databaseRegistry = databaseRegistry;
  }

  @Override
  public void start() {
    startDatasourceLoaderThreads();
  }

  @Override
  public void stop() {
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
    datasourceLoadQueue.offer(project);
  }

  private class DatasourceLoader extends Thread {

    @Override
    public void run() {
      log.info("{}: started", getName());
      try {
        //noinspection InfiniteLoopStatement
        while (true) {
          load(datasourceLoadQueue.take());
        }
      } catch (InterruptedException ignored) {
        log.debug("{}: interrupted", getName());
      }
      log.info("{}: Stopped", getName());
    }

    private void load(Project project) {
      log.info("{}: loading datasource of project {}", getName(), project.getName());
      try {
        reloadDatasource(project);
        projectsState.updateProjectState(project.getName(), ProjectsState.State.READY);
      } catch (Exception e) {
        log.error("{}: loading datasource of project {} failed for database: {}", getName(), project.getName(), project.getDatabase(), e);
        databaseRegistry.unregister(project.getDatabase(), project.getName());
        projectsState.updateProjectState(project.getName(), ProjectsState.State.ERRORS);
      }
    }
  }

}
