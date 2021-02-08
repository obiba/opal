/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.resource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.core.service.NoSuchResourceFactoryException;
import org.obiba.opal.core.service.NoSuchResourceProviderException;
import org.obiba.opal.core.service.ResourceProvidersService;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.r.service.event.RServiceInitializedEvent;
import org.obiba.opal.spi.r.AbstractROperationWithResult;
import org.obiba.opal.spi.r.RNamedList;
import org.obiba.opal.spi.r.RServerResult;
import org.obiba.opal.web.r.RPackageResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class RResourceProvidersService implements Service, ResourceProvidersService {

  private static final Logger log = LoggerFactory.getLogger(RResourceProvidersService.class);

  private static final String RESOURCE_JS_FILE = "resources/resource.js";

  @Autowired
  private RPackageResourceHelper rPackageHelper;

  @Autowired
  private RServerManagerService rServerManagerService;

  private boolean running = false;

  private boolean ensureResourcerDone = false;

  private Map<String, ResourceProvider> resourceProviders = Maps.newHashMap();

  private Future<Boolean> resourceProvidersTask;

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
  public void onRServiceStarted(RServiceInitializedEvent event) {
    finalizeServiceStart();
    loadResourceProviders();
  }

  private synchronized boolean loadResourceProviders() {
    resourceProviders.clear();
    try {
      ResourcePackageScriptsROperation rop = new ResourcePackageScriptsROperation();
      rServerManagerService.getDefaultRServer().execute(rop);
      RServerResult result = rop.getResult();
      if (result.isNamedList()) {
        RNamedList<RServerResult> pkgList = result.asNamedList();
        for (String name : pkgList.keySet()) {
          RServerResult rexp = pkgList.get(name);
          resourceProviders.put(name, new RResourceProvider(name, rexp.asStrings()[0]));
        }
      }
      return true;
    } catch (Exception e) {
      log.error("Resource packages discovery failed", e);
      return false;
    }
  }

  /**
   * Make sure a pending resource providers discovery task is completed before getting the map.
   *
   * @return
   */
  public synchronized Map<String, ResourceProvider> getResourceProvidersMap() {
    if (resourceProvidersTask != null && !resourceProvidersTask.isDone()) {
      try {
        resourceProvidersTask.get();
        resourceProvidersTask = null;
      } catch (Exception e) {
        log.error("Failed at waiting for the resource providers discovery task to complete", e);
      }
    }
    return resourceProviders;
  }

  /**
   * Ensure resourcer R package is installed only once after the service has been started and R server is ready.
   */
  private void finalizeServiceStart() {
    if (!ensureResourcerDone) {
      try {
        rPackageHelper.ensureCRANPackage(rServerManagerService.getDefaultRServer(), "resourcer");
        ensureResourcerDone = true;
      } catch (Exception e) {
        log.error("Cannot ensure resourcer R package is installed", e);
      }
    }
  }

  /**
   * Fetch resource R package names and their location folder.
   */
  private class ResourcePackageScriptsROperation extends AbstractROperationWithResult {

    @Override
    protected void doWithConnection() {
      setResult(null);

      eval(String.format("is.null(assign('x', lapply(installed.packages()[,1], function(p) { system.file('%s', package=p) })))", RESOURCE_JS_FILE), false);
      setResult(eval("lapply(x[lapply(x, nchar)>0], function(p) { readChar(p, file.info(p)$size) })", false));
    }
  }

}
