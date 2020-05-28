/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;
import org.obiba.opal.web.model.client.opal.PluginPackagesDto;

import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;

public abstract class PluginsResource {

  public interface Handler {
    void handle(List<PluginPackageDto> plugins);
  }

  public void getPlugins(final Handler handler) {
    ResourceRequestBuilderFactory.<PluginPackagesDto>newBuilder()
        .forResource(getResource())
        .withCallback(new ResourceCallback<PluginPackagesDto>() {
          @Override
          public void onResource(Response response, PluginPackagesDto resource) {
            handler.handle(JsArrays.toList(resource.getPackagesArray()));
          }
        })
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            handler.handle(new ArrayList<PluginPackageDto>());
          }
        }, SC_BAD_REQUEST, SC_INTERNAL_SERVER_ERROR)
        .get().send();
  }

  public abstract String getResource();
}
