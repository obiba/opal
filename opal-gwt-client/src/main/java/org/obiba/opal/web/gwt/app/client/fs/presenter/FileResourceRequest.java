/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.fs.presenter;

import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.Authorizer;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.event.shared.EventBus;

/**
 *
 */
public class FileResourceRequest {

  private EventBus eventBus;

  private String path;

  private ResourceCallback<FileDto> callback;

  private FileResourceRequest() {
  }

  public FileResourceRequest(EventBus eventBus, String path, ResourceCallback<FileDto> callback) {
    super();
    this.eventBus = eventBus;
    this.path = path;
    this.callback = callback;
  }

  public void send() {
    final String resource = "/files/_meta" + path + "/";
    ResourceRequestBuilderFactory.<FileDto>newBuilder().forResource(resource).get()//
        .withCallback(callback).send();
  }

  public static Builder newBuilder(EventBus eventBus) {
    return new Builder(eventBus);
  }

  public static class Builder {

    private FileResourceRequest request;

    protected Builder(EventBus eventBus) {
      request = new FileResourceRequest();
      request.eventBus = eventBus;
    }

    public Builder path(String path) {
      request.path = path;
      return this;
    }

    public Builder withCallback(ResourceCallback<FileDto> callback) {
      request.callback = callback;
      return this;
    }

    public void send() {
      request.send();
    }

  }

}
