/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.rest.client.authorization;

import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilder;

/**
 * If unauthorized, check for next authorization.
 */
public class OrAuthorizer extends CascadingAuthorizer {

  protected OrAuthorizer(ResourceAuthorizationRequestBuilder request, HasAuthorization authorizer) {
    super(request, authorizer);
  }

  @Override
  public void authorized() {
    getAuthorizer().authorized();
  }

  @Override
  public void unauthorized() {
    getRequest().authorize(getAuthorizer()).send();
  }

}
