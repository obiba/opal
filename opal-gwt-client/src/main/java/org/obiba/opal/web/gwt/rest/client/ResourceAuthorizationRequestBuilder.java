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

import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

/**
 *
 */
public interface ResourceAuthorizationRequestBuilder {

  public ResourceAuthorizationRequestBuilder forResource(String resource);

  public ResourceAuthorizationRequestBuilder get();

  public ResourceAuthorizationRequestBuilder post();

  public ResourceAuthorizationRequestBuilder put();

  public ResourceAuthorizationRequestBuilder delete();

  public ResourceAuthorizationRequestBuilder request(String resource, HttpMethod method);

  public ResourceAuthorizationRequestBuilder authorize(HasAuthorization toAuthorize);

  public void send();

}
