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
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.event.AppRegisteredEvent;
import org.obiba.opal.core.event.AppUnregisteredEvent;
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

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Management of the R servers available.
 */
@Component
public class RServerManagerService implements Service {

  private static final Logger log = LoggerFactory.getLogger(RServerManagerService.class);

  private static final String DEFAULT_CLUSTER_NAME = "default";

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
        rServerService.getState().getTags().forEach(tag -> {
          if (rClusters.containsKey(tag))
            // ensure a service built on same app is not already registered in the cluster
            rClusters.get(tag).removeRServerService(event.getApp());
          else
            rClusters.put(tag, new RServerCluster(tag));
          rClusters.get(tag).addRServerService(rServerService);
          if (running)
            rServerService.start();
        });
      } catch (Exception e) {
        log.error("Rock R server registration failed: {}", event.getApp().getName(), e);
      }
      notifyInitialized();
    }
  }

  @Subscribe
  public synchronized void onAppUnregistered(AppUnregisteredEvent event) {
    if (ROCK_APP_TYPE.equals(event.getApp().getType())) {
      rClusters.values().forEach(cluster -> cluster.removeRServerService(event.getApp()));
      for (Map.Entry<String, RServerCluster> entry : rClusters.entrySet()) {
        if (entry.getValue().isEmpty())
          rClusters.remove(entry.getKey());
      }
    }
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    if (!rserveServiceInDefaultCluster && isRserveServiceAvailable()) {
      if (!rClusters.containsKey(getDefaultClusterName()))
        rClusters.put(getDefaultClusterName(), new RServerCluster(getDefaultClusterName()));
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
    rClusters.values().forEach(RServerCluster::stop);
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

  private boolean isRserveServiceAvailable() {
    try {
      rserveService.getState();
      return true;
    } catch (Exception e) {
      return false;
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