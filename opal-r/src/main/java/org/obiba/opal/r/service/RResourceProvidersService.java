/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.core.service.NoSuchResourceFactoryException;
import org.obiba.opal.core.service.NoSuchResourceProviderException;
import org.obiba.opal.core.service.ResourceProvidersService;
import org.obiba.opal.core.service.event.OpalStartedEvent;
import org.obiba.opal.core.service.event.ResourceProvidersServiceStartedEvent;
import org.obiba.opal.r.cluster.RServerCluster;
import org.obiba.opal.r.service.event.*;
import org.obiba.opal.spi.r.AbstractROperationWithResult;
import org.obiba.opal.spi.r.RNamedList;
import org.obiba.opal.spi.r.RServerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RResourceProvidersService implements Service, ResourceProvidersService {

  private static final Logger log = LoggerFactory.getLogger(RResourceProvidersService.class);

  private static final String RESOURCE_JS_FILE = "resources/resource.js";

  private final RServerManagerService rServerManagerService;

  private final EventBus eventBus;

  private boolean running = false;

  private boolean ensureResourcerDone = false;

  private final Map<String, List<ResourceProvider>> resourceProviders = Maps.newHashMap();

  private final Object resourceProvidersTask = new Object();

  private boolean applicationStarted = false;

  @Autowired
  public RResourceProvidersService(RServerManagerService rServerManagerService, EventBus eventBus) {
    this.rServerManagerService = rServerManagerService;
    this.eventBus = eventBus;
  }

  //
  // Service methods
  //

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    running = true;
    ensureResourcerDone = false;
  }

  @Override
  public void stop() {
    running = false;
  }

  @Override
  public String getName() {
    return "r-resources";
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    throw new NoSuchServiceConfigurationException(getName());
  }

  //
  // ResourceProvidersService methods
  //

  @Override
  public List<ResourceProvider> getResourceProviders() {
    return Lists.newArrayList(getResourceProvidersMap().values());
  }

  @Override
  public ResourceProvider getResourceProvider(String name) throws NoSuchResourceProviderException {
    if (getResourceProvidersMap().containsKey(name)) return getResourceProvidersMap().get(name);
    throw new NoSuchResourceProviderException(name);
  }

  @Override
  public ResourceFactory getResourceFactory(String provider, String name) throws NoSuchResourceProviderException, NoSuchResourceFactoryException {
    ResourceProvider resourceProvider = getResourceProvider(provider);
    return resourceProvider.getFactories().stream()
        .filter(f -> f.getName().equals(name)).findFirst()
        .orElseThrow(() -> new NoSuchResourceFactoryException(provider, name));
  }

  @Override
  public List<Category> getAllCategories() {
    List<Category> allTags = Lists.newArrayList();
    getResourceProvidersMap().values().forEach(p -> allTags.addAll(p.getCategories()));
    return allTags;
  }

  @Override
  public List<ResourceFactory> getResourceFactories(List<String> tags) {
    return null;
  }

  //
  // Private methods
  //

  @Subscribe
  public void onRServiceInitialized(RServiceInitializedEvent event) {
    finalizeServiceStart();
    loadResourceProviders();
  }

  @Subscribe
  public void onRServiceStarted(RServerServiceStartedEvent event) {
    if (!event.hasName()) { // only when a cluster is started
      finalizeServiceStart();
      loadResourceProviders();
    }
  }

  @Subscribe
  public void onRServiceStopped(RServerServiceStoppedEvent event) {
    if (!event.hasName()) { // only when a cluster is stopped
      synchronized (resourceProvidersTask) {
        resourceProviders.clear();
      }
    }
  }

  @Subscribe
  public void onRPackageInstalled(RPackageInstalledEvent event) {
    synchronized (resourceProvidersTask) {
      resourceProviders.clear();
      loadResourceProviders();
    }
  }

  @Subscribe
  public void onRPackageRemoved(RPackageRemovedEvent event) {
    synchronized (resourceProvidersTask) {
      resourceProviders.clear();
      loadResourceProviders();
    }
  }

  @EventListener
  public void onOpalStarted(OpalStartedEvent event) {
    log.info("Opal started, R services to be finalized...");
    applicationStarted = true;
    eventBus.post(new ResourceProvidersServiceStartedEvent());
  }

  private void loadResourceProviders() {
    synchronized (resourceProvidersTask) {
      resourceProviders.clear();
      ResourcePackageScriptsROperation rop = new ResourcePackageScriptsROperation();
      try {
        // scan all R server clusters
        for (RServerCluster rServerCluster : rServerManagerService.getRServerClusters()) {
          try {
            rServerCluster.execute(rop);
            RServerResult result = rop.getResult();
            if (result.isNamedList()) {
              RNamedList<RServerResult> pkgList = result.asNamedList();
              for (String name : pkgList.keySet()) {
                RServerResult res = pkgList.get(name);
                if (!resourceProviders.containsKey(name)) {
                  resourceProviders.put(name, Lists.newArrayList());
                }
                resourceProviders.get(name).add(new RResourceProvider(rServerCluster.getName(), name, res.asStrings()[0]));
              }
            }
          } catch (Exception e) {
            log.error("Resource packages discovery failed for R servers cluster: {}", rServerCluster.getName(), e);
          }
        }
      } catch (Exception e) {
        log.error("Resource packages discovery failed", e);
      }
    }
    if (applicationStarted) {
      eventBus.post(new ResourceProvidersServiceStartedEvent());
    }
  }

  /**
   * Make sure a pending resource providers discovery task is completed before getting the map.
   *
   * @return
   */
  public synchronized Map<String, ResourceProvider> getResourceProvidersMap() {
    synchronized (resourceProvidersTask) {
      // get first provider even if it appears in multiple profiles
      return resourceProviders.entrySet().stream()
          .collect(Collectors.toMap(Map.Entry::getKey,
              e -> e.getValue().get(0)));
    }
  }

  /**
   * Ensure resourcer R package is installed only once after the service has been started and R server is ready.
   */
  private void finalizeServiceStart() {
    if (!ensureResourcerDone) {
      try {
        rServerManagerService.getRServerClusters().forEach(cluster -> cluster.ensureCRANPackage("resourcer"));
        ensureResourcerDone = true;
      } catch (Exception e) {
        log.error("Cannot ensure resourcer R package is installed", e);
      }
    }
  }

  /**
   * Fetch resource R package names and their location folder.
   */
  private static class ResourcePackageScriptsROperation extends AbstractROperationWithResult {

    @Override
    protected void doWithConnection() {
      setResult(null);

      eval(String.format("is.null(assign('x', lapply(installed.packages()[,1], function(p) { system.file('%s', package=p) })))", RESOURCE_JS_FILE), false);
      setResult(eval("lapply(x[lapply(x, nchar)>0], function(p) { readChar(p, file.info(p)$size) })", false));
    }
  }

}
