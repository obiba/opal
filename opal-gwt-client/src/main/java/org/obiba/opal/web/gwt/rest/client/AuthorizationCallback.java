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

import java.util.Set;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;

/**
 *
 */
public interface AuthorizationCallback {

  void onResponseCode(Request request, Response response, Set<HttpMethod> allowed);

}
