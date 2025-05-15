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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.event.*;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.NoSuchServiceException;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.r.cluster.RServerAppsCluster;
import org.obiba.opal.r.rock.RockService;
import org.obiba.opal.r.service.event.RServiceInitializedEvent;
import org.obiba.opal.r.service.event.RServiceStartedEvent;
import org.obiba.opal.r.service.event.RServiceStoppedEvent;
import org.obiba.opal.web.model.OpalR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Management of the R servers available.
 */
@Component
public class RServerManagerService implements Service {

  private static final Logger log = LoggerFactory.getLogger(RServerManagerService.class);

  public static final String DEFAULT_CLUSTER_NAME = "default";

  private static final String ROCK_APP_TYPE = "rock";

  private final ApplicationContext applicationContext;

  private final EventBus eventBus;

  private final RCacheHelper rCacheHelper;

  private final Map<String, RServerClusterService> rClusters = Maps.newConcurrentMap();

  private boolean running;

  @Autowired
  public RServerManagerService(ApplicationContext applicationContext, EventBus eventBus, RCacheHelper rCacheHelper) {
    this.applicationContext = applicationContext;
    this.eventBus = eventBus;
    this.rCacheHelper = rCacheHelper;
  }

  /**
   * Get the R server profile corresponding to the default R server.
   *
   * @return
   */
  public RServerProfile getDefaultRServerProfile() {
    final RServerService service = getDefaultRServer();
    return new RServerProfile() {
      @Override
      public String getName() {
        return service.getName();
      }

      @Override
      public String getCluster() {
        return service.getName();
      }
    };
  }

  /**
   * Get R server service from default cluster.
   *
   * @return
   */
  public RServerService getDefaultRServer() {
    if (rClusters.containsKey(getDefaultClusterName()))
      return rClusters.get(getDefaultClusterName());
    throw new NoSuchServiceException("R server (default)");
  }

  /**
   * Get R server service from cluster name. If name is null or empty, get the R server from the default cluster.
   *
   * @param name
   * @return
   */
  public RServerService getRServer(String name) {
    return getRServerCluster(name);
  }

  public RServerService getRServerWithPackages(List<String> requiredPackages) {
    for (RServerClusterService cluster : getRServerClusters()) {
      Optional<RServerService> rServerServiceOpt = cluster.getRServerServices().stream().findFirst();
      if (rServerServiceOpt.isPresent()) {
        List<String> packageNames = rServerServiceOpt.get().getInstalledPackagesDtos().stream()
            .map(OpalR.RPackageDto::getName)
            .filter(requiredPackages::contains)
            .distinct()
            .toList();
        if (packageNames.size() == requiredPackages.size())
          return rServerServiceOpt.get();
      }
    }
    throw new NoSuchElementException("No R server with packages: " + Joiner.on(", ").join(requiredPackages));
  }

  /**
   * Get all R server clusters.
   *
   * @return
   */
  public Collection<RServerClusterService> getRServerClusters() {
    return rClusters.values();
  }

  /**
   * Get R server cluster by name. If name is null or empty, get the default cluster.
   *
   * @param name
   * @return
   */
  public RServerClusterService getRServerCluster(String name) {
    if (Strings.isNullOrEmpty(name) && rClusters.containsKey(getDefaultClusterName()))
      return rClusters.get(getDefaultClusterName());
    if (rClusters.containsKey(name))
      return rClusters.get(name);
    throw new NoSuchElementException("No R server cluster with name: " + name);
  }

  /**
   * Get the name of the default R server cluster.
   *
   * @return
   */
  public String getDefaultClusterName() {
    // TODO make it configurable (live?)
    return DEFAULT_CLUSTER_NAME;
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
        if (rClusters.containsKey(clusterName) && rClusters.get(clusterName) instanceof RServerAppsCluster)
          // ensure a service built on same app is not already registered in the cluster
          ((RServerAppsCluster) rClusters.get(clusterName)).removeRServerService(event.getApp());
        else
          rClusters.put(clusterName, new RServerAppsCluster(clusterName, eventBus));
        rClusters.get(clusterName).addRServerService(rServerService);
        rServerService.setRServerClusterName(clusterName);
        if (running)
          rServerService.start();
        log.info("R server '{}' added to cluster: {}", rServerService.getName(), clusterName);
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
      rClusters.values().stream()
          .filter(cluster -> cluster instanceof RServerAppsCluster)
          .forEach(cluster -> ((RServerAppsCluster) cluster).removeRServerService(event.getApp()));
      for (Map.Entry<String, RServerClusterService> entry : rClusters.entrySet()) {
        if (entry.getValue().isEmpty())
          rClusters.remove(entry.getKey());
      }
    }
  }

  @Subscribe
  public synchronized void onPodSpecRegistered(PodSpecRegisteredEvent event) {
    if (!ROCK_APP_TYPE.equals(event.getPodSpec().getType())) return;
    log.info("Register R pod: {}", event.getPodSpec().toString());
  }

  @Subscribe
  public synchronized void onPodSpecUnregistered(PodSpecUnregisteredEvent event) {
    if (!ROCK_APP_TYPE.equals(event.getPodSpec().getType())) return;
    log.info("Unregister R pod: {}", event.getPodSpec().toString());
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
    rClusters.values().forEach(RServerClusterService::start);
    running = true;
    eventBus.post(new RServiceStartedEvent(getName()));
    notifyInitialized();
  }

  @Override
  public void stop() {
    try {
      rClusters.values().forEach(RServerClusterService::stop);
    } catch (Exception e) {
      // ignore
      log.debug("Failure when stopping R servers", e);
    }
    running = false;
    eventBus.post(new RServiceStoppedEvent(getName()));
  }

  @Override
  public String getName() {
    return "r";
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    return null;
  }

  public void evictCache() {
    evictTableCaches();
  }

  //
  // Private methods
  //

  private void evictTableCaches() {
    rCacheHelper.evictAll();
  }

  private void evictTableCache(ValueTable table) {
    String tableCachePrefix = String.format("%s-%s-", table.getDatasource().getName(), table.getName());
    rCacheHelper.evictCache(tableCachePrefix);
  }

  private void notifyInitialized() {
    if (rClusters.containsKey(getDefaultClusterName()))
      eventBus.post(new RServiceInitializedEvent(getName()));
  }
}
