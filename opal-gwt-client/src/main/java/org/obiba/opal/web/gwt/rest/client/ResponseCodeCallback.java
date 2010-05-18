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

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;

/**
 * A response callback for handling HTTP status codes. These callbacks are added to a {@code ResourceRequestBuilder} and
 * attached to a specific status code. If the response to the request is that specific code, then this handler is
 * invoked.
 */
public interface ResponseCodeCallback {

  public void onResponseCode(Request request, Response response);

}
