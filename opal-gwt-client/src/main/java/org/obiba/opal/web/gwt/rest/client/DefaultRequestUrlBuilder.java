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

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.inject.Inject;

public class DefaultRequestUrlBuilder implements RequestUrlBuilder {

  private final RequestCredentials credentials;

  @Inject
  public DefaultRequestUrlBuilder(RequestCredentials credentials) {
    this.credentials = credentials;
  }

  @Override
  public String buildAbsoluteUrl(String relativeUrl) {
    boolean startsWithSlash = relativeUrl.startsWith("/");
    String absolutePath = URL.encode("/ws" + (startsWithSlash ? relativeUrl : '/' + relativeUrl));
    credentials.provideCredentials(absolutePath);
    return GWT.getModuleBaseURL().replace("/" + GWT.getModuleName() + "/", "") + absolutePath;
  }

}
