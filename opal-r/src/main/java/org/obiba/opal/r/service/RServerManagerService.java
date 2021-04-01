/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.event.*;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.NoSuchServiceException;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.r.cluster.RServerCluster;
import org.obiba.opal.r.rock.RockService;
import org.obiba.opal.r.rserve.RserveService;
import org.obiba.opal.r.service.event.RServiceInitializedEvent;
import org.obiba.opal.r.service.event.RServiceStartedEvent;
import org.obiba.opal.r.service.event.RServiceStoppedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Management of the R servers available.
 */
@Component
public class RServerManagerService implements Service {

  private static final Logger log = LoggerFactory.getLogger(RServerManagerService.class);

  private static final String R_CACHE_DIR = System.getenv().get("OPAL_HOME") + File.separatorChar + "work" + File.separatorChar + "R" + File.separatorChar + "cache";

  public static final String DEFAULT_CLUSTER_NAME = "default";

  private static final String ROCK_APP_TYPE = "rock";

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private EventBus eventBus;

  // legacy
  @Autowired
  private RserveService rserveService;

  private boolean rserveServiceInDefaultCluster;

  private final Map<String, RServerCluster> rClusters = Maps.newConcurrentMap();

  private boolean running;

  public RServerService getDefaultRServer() {
    if (rClusters.containsKey(getDefaultClusterName()))
      return rClusters.get(getDefaultClusterName());
    throw new NoSuchServiceException("R server (default)");
  }

  public Collection<RServerCluster> getRServerClusters() {
    return rClusters.values();
  }

  public RServerCluster getRServerCluster(String name) {
    if (rClusters.containsKey(name))
      return rClusters.get(name);
    throw new NoSuchElementException("No R server cluster with name: " + name);
  }

  @Subscribe
  public synchronized void onAppRegistered(AppRegisteredEvent event) {
    if (ROCK_APP_TYPE.equals(event.getApp().getType())) {
      log.info("Register R server: {}", event.getApp().toString());
      RockService rServerService = applicationContext.getBean("rockRService", RockService.class);
      rServerService.setApp(event.getApp());
      try {
        // R server can only be in one cluster
        String clusterName = rServerService.getState().getCluster();
        if (rClusters.containsKey(clusterName))
          // ensure a service built on same app is not already registered in the cluster
          rClusters.get(clusterName).removeRServerService(event.getApp());
        else
          rClusters.put(clusterName, new RServerCluster(clusterName, eventBus));
        rClusters.get(clusterName).addRServerService(rServerService);
        rServerService.setRServerClusterName(clusterName);
        if (running)
          rServerService.start();
      } catch (Exception e) {
        log.error("Rock R server registration failed: {}", event.getApp().getName(), e);
        eventBus.post(new AppRejectedEvent(event.getApp()));
      }
      notifyInitialized();
    }
  }

  @Subscribe
  public synchronized void onAppUnregistered(AppUnregisteredEvent event) {
    if (ROCK_APP_TYPE.equals(event.getApp().getType())) {
      log.info("Unregister R server: {}", event.getApp().toString());
      rClusters.values().forEach(cluster -> cluster.removeRServerService(event.getApp()));
      for (Map.Entry<String, RServerCluster> entry : rClusters.entrySet()) {
        if (entry.getValue().isEmpty())
          rClusters.remove(entry.getKey());
      }
    }
  }

  @Subscribe
  public synchronized void onTableDeleted(ValueTableDeletedEvent event) {
    evictTableCache(event.getValueTable());
  }

  @Subscribe
  public synchronized void onTableUpdated(ValueTableUpdatedEvent event) {
    evictTableCache(event.getValueTable());
  }

  @Subscribe
  public synchronized void onTableRenamed(ValueTableRenamedEvent event) {
    evictTableCache(event.getValueTable());
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    if (!rserveServiceInDefaultCluster && rserveService.isServiceAvailable()) {
      if (!rClusters.containsKey(getDefaultClusterName()))
        rClusters.put(getDefaultClusterName(), new RServerCluster(getDefaultClusterName(), eventBus));
      rClusters.get(getDefaultClusterName()).addRServerService(rserveService);
      rserveServiceInDefaultCluster = true;
    }
    rClusters.values().forEach(RServerCluster::start);
    running = true;
    eventBus.post(new RServiceStartedEvent(getName()));
    notifyInitialized();
  }

  @Override
  public void stop() {
    try {
      rClusters.values().forEach(RServerCluster::stop);
    } catch (Exception e) {
      // ignore
      log.debug("Failure when stopping R servers", e);
    }
    running = false;
    eventBus.post(new RServiceStoppedEvent(getName()));
    evictTableCaches();
  }

  @Override
  public String getName() {
    return "r";
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    return null;
  }

  //
  // Private methods
  //

  private void evictTableCaches() {
    try {
      File cacheDir = new File(R_CACHE_DIR);
      FileUtil.delete(cacheDir);
    } catch (Exception e) {
      log.warn("Failure when evicting table caches", e);
    }
  }

  private void evictTableCache(ValueTable table) {
    try {
      File cacheDir = new File(R_CACHE_DIR);
      String tableCachePrefix = String.format("%s-%s-", table.getDatasource().getName(), table.getName());
      File[] files = cacheDir.listFiles(file -> file.getName().startsWith(tableCachePrefix));
      if (files != null) {
        for (File file : files) {
          file.delete();
        }
      }
    } catch (Exception e) {
      log.warn("Failure when evicting table cache: {}", table.getName(), e);
    }
  }

  private void notifyInitialized() {
    if (rClusters.containsKey(getDefaultClusterName()))
      eventBus.post(new RServiceInitializedEvent(getName()));
  }

  private String getDefaultClusterName() {
    // TODO make it configurable (live?)
    return DEFAULT_CLUSTER_NAME;
  }
}
