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

import net.customware.gwt.presenter.client.EventBus;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.inject.Inject;

/**
 *
 */
public class ResourceRequestBuilderFactory {

  private EventBus eventBus;

  private RequestCredentials credentials;

  @Inject
  public ResourceRequestBuilderFactory(EventBus eventBus, RequestCredentials creds) {
    this.eventBus = eventBus;
    this.credentials = creds;
  }

  public <T extends JavaScriptObject> ResourceRequestBuilder<T> newBuilder() {
    return new ResourceRequestBuilder<T>(eventBus, credentials);
  }

}
