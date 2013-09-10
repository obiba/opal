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

import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.web.bindery.event.shared.EventBus;

/**
 *
 */
public class FileResourceRequest {

  private EventBus eventBus;

  private String path;

  private ResourceCallback<FileDto> callback;

  private FileResourceRequest() {
  }

  public void send() {
    String resource = "/files/_meta" + path + "/";
    ResourceRequestBuilderFactory.<FileDto>newBuilder().forResource(resource).get()//
        .withCallback(callback).send();
  }

  public static Builder newBuilder(String path) {
    return new Builder(path);
  }

  public static class Builder {

    private final FileResourceRequest request;

    protected Builder(String path) {
      request = new FileResourceRequest();
      request.path = path;
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
