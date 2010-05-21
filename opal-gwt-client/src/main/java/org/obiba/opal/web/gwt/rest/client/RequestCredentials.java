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

import com.google.gwt.http.client.RequestBuilder;

/**
 *
 */
public class RequestCredentials {

  private String credentials = null;

  public RequestBuilder provideCredentials(RequestBuilder builder) {
    if(credentials != null) {
      builder.setHeader("X-Opal-Auth", credentials);
    }
    return builder;
  }

  public void setCredentials(String credentials) {
    this.credentials = credentials;
  }
}
