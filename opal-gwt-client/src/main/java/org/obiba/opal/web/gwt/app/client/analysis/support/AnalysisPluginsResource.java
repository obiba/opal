package org.obiba.opal.web.gwt.app.client.analysis.support;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;
import org.obiba.opal.web.model.client.opal.PluginPackagesDto;

import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;

public class AnalysisPluginsResource {

  public interface Handler {
    void handle(List<PluginPackageDto> plugins);
  }

  public static AnalysisPluginsResource getInstance() {
    return new AnalysisPluginsResource();
  }

  public void getAnalysisPlugins(final Handler handler) {
    ResourceRequestBuilderFactory.<PluginPackagesDto>newBuilder()
      .forResource(UriBuilders.PLUGINS_ANALYSIS.create().build())
      .withCallback(new ResourceCallback<PluginPackagesDto>() {
        @Override
        public void onResource(Response response, PluginPackagesDto resource) {
          handler.handle(resource == null
            ? new ArrayList<PluginPackageDto>()
            : JsArrays.toList(resource.getPackagesArray())
          );
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
}
