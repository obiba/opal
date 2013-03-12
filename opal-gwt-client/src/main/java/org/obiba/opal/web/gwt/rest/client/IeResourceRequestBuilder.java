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
import com.google.gwt.user.client.Random;

/**
 * OPAL-617: IE-specific extension of DefaultResourceRequestBuilder that adds a random query parameter to make the URI
 * unique. Prevents the browser from caching the result.
 */
public class IeResourceRequestBuilder<T extends JavaScriptObject> extends DefaultResourceRequestBuilder<T> {

  @Override
  public DefaultResourceRequestBuilder<T> forResource(String resource) {
    // Adds a random query parameter to prevent the browser from caching the result (each request will have a unique
    // URI)
    return super.forResource(
        new StringBuilder(resource).append(resource.contains("?") ? "&" : "?").append("_=").append(Random.nextInt())
            .toString());
  }
}
