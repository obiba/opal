/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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
import com.google.common.eventbus.Subscribe;
import org.json.JSONObject;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.core.service.NoSuchResourceFactoryException;
import org.obiba.opal.core.service.NoSuchResourceProviderException;
import org.obiba.opal.core.service.ResourceProvidersService;
import org.obiba.opal.r.service.event.RServiceStartedEvent;
import org.obiba.opal.spi.r.AbstractROperationWithResult;
import org.obiba.opal.web.r.RPackageResourceHelper;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.RList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RResourceProvidersService implements Service, ResourceProvidersService {

  private static final Logger log = LoggerFactory.getLogger(RResourceProvidersService.class);

  private static final String RESOURCE_JS_FILE = "resources/resource.js";

  @Autowired
  private RPackageResourceHelper rPackageHelper;

  private boolean running = false;

  private Map<String, ResourceProvider> resourceProviders = Maps.newHashMap();

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
    return Lists.newArrayList(resourceProviders.values());
  }

  @Override
  public ResourceProvider getResourceProvider(String name) throws NoSuchResourceProviderException {
    if (resourceProviders.containsKey(name)) return resourceProviders.get(name);
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
    resourceProviders.values().forEach(p -> allTags.addAll(p.getCategories()));
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
  public void onRServiceStarted(RServiceStartedEvent event) {
    resourceProviders.clear();
    try {

      ResourcePackageScriptsROperation rop = new ResourcePackageScriptsROperation();
      REXP result = rPackageHelper.execute(rop).getResult();
      RList pkgList = result.asList();
      if (pkgList.isNamed()) {
        for (Object name : pkgList.names) {
          REXP rexp = pkgList.at(name.toString());
          resourceProviders.put(name.toString(), new RResourceProvider(name.toString(), rexp.asString()));
        }
      }
    } catch (Exception e) {
      log.error("DataShield packages properties extraction failed", e);
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
