/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.resources;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;
import org.obiba.opal.web.model.client.opal.PluginPackagesDto;
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ResourceProvidersDto;

import java.util.ArrayList;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;

public class ResourceProvidersResource {

  public interface Handler {
    void handle(ResourceProvidersDto providers);
  }

  public void getProviders(final Handler handler) {
    ResourceRequestBuilderFactory.<ResourceProvidersDto>newBuilder()
        .forResource(UriBuilders.RESOURCE_PROVIDERS.create().build())
        .withCallback(new ResourceCallback<ResourceProvidersDto>() {
          @Override
          public void onResource(Response response, ResourceProvidersDto resource) {
            handler.handle(resource);
          }
        })
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            handler.handle(null);
          }
        }, SC_BAD_REQUEST, SC_INTERNAL_SERVER_ERROR)
        .get().send();
  }

  public static ResourceProvidersResource getInstance() {
    return new ResourceProvidersResource();
  }

  public static String makeResourceFactoryKey(ResourceFactoryDto factory) {
    return factory.getProvider() + ":" + factory.getName();
  }

  public static String[] splitResourceFactoryKey(String key) {
    return key.split(":");
  }

}
