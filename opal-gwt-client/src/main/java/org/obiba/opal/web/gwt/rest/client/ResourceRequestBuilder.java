/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.rest.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;

public interface ResourceRequestBuilder<T extends JavaScriptObject> {

  public ResourceRequestBuilder<T> forResource(String resource);

  public ResourceRequestBuilder<T> withCallback(ResourceCallback<T> callback);

  public ResourceRequestBuilder<T> withCallback(int code, ResponseCodeCallback callback);

  public ResourceRequestBuilder<T> withAuthorizationCallback(AuthorizationCallback callback);

  public ResourceRequestBuilder<T> accept(String acceptHeader);

  public ResourceRequestBuilder<T> withBody(String contentType, String body);

  public ResourceRequestBuilder<T> withResourceBody(/* T.stringify() */String dto);

  public ResourceRequestBuilder<T> withFormBody(String key1, String value1, String... keyValues);

  public ResourceRequestBuilder<T> get();

  public ResourceRequestBuilder<T> head();

  public ResourceRequestBuilder<T> post();

  public ResourceRequestBuilder<T> put();

  public ResourceRequestBuilder<T> delete();

  public ResourceRequestBuilder<T> options();

  public RequestBuilder build();

  public Request send();

  public String getVersion();

}
