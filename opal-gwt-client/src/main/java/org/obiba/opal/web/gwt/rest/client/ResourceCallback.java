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
import com.google.gwt.http.client.Response;

/**
 * A callback for handling a resource javascript representation. This can only be used when the resource returned by the
 * server can be parsed into a JavaScriptObject.
 */
public interface ResourceCallback<T extends JavaScriptObject> {

  public void onResource(Response response, T resource);

}
