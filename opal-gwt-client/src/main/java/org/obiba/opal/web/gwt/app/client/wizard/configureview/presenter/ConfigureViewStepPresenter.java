/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.support.JsonUtil;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewUpdateEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.inject.Inject;

public class ConfigureViewStepPresenter extends WidgetPresenter<ConfigureViewStepPresenter.Display> {
  //
  // Instance Variables
  //

  /**
   * {@link ViewDto} of view being configured.
   * 
   * This is initialized upon a {@link ViewConfigurationRequiredEvent} and updated on every {@link ViewUpdateEvent}.
   */
  private ViewDto viewDto;

  //
  // Constructors
  //

  @Inject
  public ConfigureViewStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    addEventHandlers();
    getDisplay().getHelpDeck().showWidget(1);
  }

  @Override
  protected void onUnbind() {
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

  private void addEventHandlers() {
    super.registerHandler(eventBus.addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredHandler()));
    super.registerHandler(eventBus.addHandler(ViewUpdateEvent.getType(), new ViewUpdateHandler()));
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {
    DeckPanel getHelpDeck();
  }

  class ViewConfigurationRequiredHandler implements ViewConfigurationRequiredEvent.Handler {

    private ResourceCallback<ViewDto> callback;

    public ViewConfigurationRequiredHandler() {
      callback = createResponseCodeCallback();
    }

    @Override
    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      // Get the ViewDto of the view to be configured. Once received, keep a copy of it.
      // This ViewDto should be communicated to, and shared with, all the *TabPresenters (Data, Variables, Entities).
      // Whenever a *TabPresenter fires a ViewUpdateEvent to signal changes, the event should contain the updated
      // ViewDto.
      ResourceRequestBuilderFactory.<ViewDto> newBuilder()
      /**/.get()
      /**/.forResource("/datasource/" + event.getDatasourceName() + "/view/" + event.getViewName())
      /**/.accept("application/x-protobuf+json")
      /**/.withCallback(callback)
      /**/.send();
    }

    private ResourceCallback<ViewDto> createResponseCodeCallback() {
      return new ResourceCallback<ViewDto>() {

        @Override
        public void onResource(Response response, ViewDto viewDto) {
          // Initialize viewDto.
          ConfigureViewStepPresenter.this.viewDto = viewDto;

          // Go ahead and display the ConfigureViewStepPresenter.
          eventBus.fireEvent(new WorkbenchChangeEvent(ConfigureViewStepPresenter.this, false, false));
        }
      };
    }
  }

  class ViewUpdateHandler implements ViewUpdateEvent.Handler {

    private ResponseCodeCallback callback;

    public ViewUpdateHandler() {
      callback = createResponseCodeCallback();
    }

    @Override
    public void onViewUpdate(ViewUpdateEvent event) {
      // Keep viewDto current (set it to the one contained in the event).
      viewDto = event.getViewDto();

      // Go ahead and update the view.
      updateView(viewDto);
    }

    private void updateView(ViewDto viewDto) {
      ResourceRequestBuilderFactory.newBuilder()
      /**/.put()
      /**/.forResource("/datasource/" + viewDto.getDatasourceName() + "/view/" + viewDto.getName())
      /**/.accept("application/x-protobuf+json").withResourceBody(JsonUtil.stringify(viewDto))
      /**/.withCallback(Response.SC_OK, callback)
      /**/.withCallback(Response.SC_BAD_REQUEST, callback)
      /**/.send();
    }

    private ResponseCodeCallback createResponseCodeCallback() {
      return new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == Response.SC_BAD_REQUEST) {
            eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, response.getText(), null));
          }
        }
      };
    }
  }
}