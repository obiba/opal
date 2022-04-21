/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.rest.client;

import javax.annotation.Nullable;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;

public interface ResourceRequestBuilder<T extends JavaScriptObject> {

  ResourceRequestBuilder<T> forResource(String resource);

  ResourceRequestBuilder<T> withCallback(ResourceCallback<T> callback);

  ResourceRequestBuilder<T> withCallback(int code, ResponseCodeCallback callback);

  ResourceRequestBuilder<T> withCallback(ResponseCodeCallback callback, int... codes);

  ResourceRequestBuilder<T> withAuthorizationCallback(AuthorizationCallback callback);

  ResourceRequestBuilder<T> accept(String acceptHeader);

  ResourceRequestBuilder<T> header(String header, String value);

  ResourceRequestBuilder<T> withBody(String contentType, String body);

  ResourceRequestBuilder<T> withResourceBody(String dto);

  ResourceRequestBuilder<T> withFormBody(String... keyValues);

  ResourceRequestBuilder<T> get();

  ResourceRequestBuilder<T> head();

  ResourceRequestBuilder<T> post();

  ResourceRequestBuilder<T> put();

  ResourceRequestBuilder<T> delete();

  ResourceRequestBuilder<T> options();

  RequestBuilder build();

  @Nullable
  Request send();

  String getVersion();

  String getUri();

  String getResource();

}
