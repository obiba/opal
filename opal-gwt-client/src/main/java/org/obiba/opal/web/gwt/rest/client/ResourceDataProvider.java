/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.rest.client;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;

/**
 * An implementation of {@code AbstractDataProvider} that uses a {@code ResourceRequestBuilder} to populate the
 * displays. Note that the provided resource should return an array of resources ({@code JsArray<T>}).
 */
public class ResourceDataProvider<T extends JavaScriptObject> extends AsyncDataProvider<T> {

  private final ResourceRequestBuilder<JsArray<T>> resourceRequestBuilder;

  public ResourceDataProvider(String resource) {
    this(ResourceRequestBuilderFactory.<JsArray<T>> newBuilder().forResource(resource).get());
  }

  public ResourceDataProvider(ResourceRequestBuilder<JsArray<T>> resourceRequestBuilder) {
    this.resourceRequestBuilder = resourceRequestBuilder.withCallback(new ResourceCallback<JsArray<T>>() {

      @Override
      public void onResource(Response response, JsArray<T> resource) {
        updateRowCount(resource != null ? resource.length() : 0, true);
        updateRowData(0, JsArrays.toList(resource));
      }
    });
  }

  @Override
  protected void onRangeChanged(HasData<T> display) {
    resourceRequestBuilder.send();
  }

}
