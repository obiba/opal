/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.resources;

import com.google.common.collect.Maps;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Singleton;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.ResourceCategoryDto;
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ResourceProviderDto;
import org.obiba.opal.web.model.client.opal.ResourceProvidersDto;

import java.util.List;
import java.util.Map;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;

@Singleton
public class ResourceProvidersService {

  private ResourceProvidersDto resourceProviders;

  private Map<String, ResourceFactoryDto> resourceFactories = Maps.newHashMap();

  public interface ResourceProvidersHandler {
    void onReceived(boolean hasProviders);
  }

  public void reset() {
    setResourceProviders(null);
    initialize(null);
  }

  public void initialize(final ResourceProvidersHandler handler) {
    if (resourceProviders == null) {
      ResourceRequestBuilderFactory.<ResourceProvidersDto>newBuilder()
          .forResource(UriBuilders.RESOURCE_PROVIDERS.create().build())
          .withCallback(new ResourceCallback<ResourceProvidersDto>() {
            @Override
            public void onResource(Response response, ResourceProvidersDto providers) {
              setResourceProviders(providers);
              for (ResourceProviderDto provider : JsArrays.toIterable(providers.getProvidersArray())) {
                for (ResourceFactoryDto factory : JsArrays.toIterable(provider.getResourceFactoriesArray())) {
                  resourceFactories.put(makeResourceFactoryKey(factory), factory);
                }
              }
              if (handler != null) handler.onReceived(hasResourceProviders());
            }
          })
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              setResourceProviders(null);
              if (handler != null) handler.onReceived(hasResourceProviders());
            }
          }, SC_BAD_REQUEST, SC_INTERNAL_SERVER_ERROR)
          .get().send();
    }
  }

  private void setResourceProviders(ResourceProvidersDto resourceProviders) {
    this.resourceProviders = resourceProviders;
    resourceFactories.clear();
  }

  public ResourceProviderDto getResourceProvider(String name) {
    for (ResourceProviderDto provider : JsArrays.toIterable(resourceProviders.getProvidersArray())) {
      if (provider.getName().equals(name)) return provider;
    }
    return null;
  }

  public boolean hasResourceProviders() {
    return !resourceFactories.isEmpty();
  }

  public Map<String, ResourceFactoryDto> getResourceFactories() {
    return resourceFactories;
  }

  public ResourceFactoryDto getResourceFactory(String provider, String name) {
    return resourceFactories.get(provider + ":" + name);
  }

  public List<ResourceCategoryDto> getResourceCategories() {
    return JsArrays.toList(resourceProviders.getCategoriesArray());
  }

  private String makeResourceFactoryKey(ResourceFactoryDto factory) {
    return factory.getProvider() + ":" + factory.getName();
  }

  public static String[] splitResourceFactoryKey(String key) {
    return key.split(":");
  }

}
