/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;

/**
 *
 */
public class ResourceRequestPresenter<T extends JavaScriptObject> extends WidgetPresenter<ResourceRequestPresenter.Display> {
  //
  // Instance Variables
  //

  private ResourceRequestBuilder<T> requestBuilder;

  //
  // Constructors
  //

  public ResourceRequestPresenter(final Display display, final EventBus eventBus, final ResourceRequestBuilder<T> requestBuilder) {
    super(display, eventBus);

    this.requestBuilder = requestBuilder;
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  protected void addEventHandlers() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Methods
  //

  public void setSuccessCodes(Integer... codes) {
    final ResponseCodeCallback callback = new ResponseCodeCallback() {

      public void onResponseCode(Request request, Response response) {
        getDisplay().completed();
      }
    };

    if(codes != null) {
      for(int code : codes) {
        requestBuilder.withCallback(code, callback);
      }
    }
  }

  public void setErrorCodes(Integer... codes) {
    final ResponseCodeCallback callback = new ResponseCodeCallback() {

      public void onResponseCode(Request request, Response response) {
        getDisplay().failed();
      }
    };

    if(codes != null) {
      for(int code : codes) {
        requestBuilder.withCallback(code, callback);
      }
    }
  }

  public void sendRequest() {
    getDisplay().inProgress();
    requestBuilder.send();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void setResourceName(String resourceName);

    void inProgress();

    void completed();

    void failed();
  }
}
