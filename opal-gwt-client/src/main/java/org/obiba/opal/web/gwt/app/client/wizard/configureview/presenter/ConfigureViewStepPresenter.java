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
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSaveRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.JavaScriptViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ConfigureViewStepPresenter extends WidgetPresenter<ConfigureViewStepPresenter.Display> {
  //
  // Instance Variables
  //

  @Inject
  private DataTabPresenter dataTabPresenter;

  @Inject
  private SelectScriptVariablesTabPresenter selectScriptVariablesTabPresenter;

  @Inject
  private EntitiesTabPresenter entitiesTabPresenter;

  private String datasourceName;

  private String viewName;

  /**
   * {@link ViewDto} of view being configured.
   * 
   * This is initialized upon a {@link ViewConfigurationRequiredEvent} and updated on every {@link ViewSaveRequiredEvent}.
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
    dataTabPresenter.bind();
    getDisplay().addDataTabWidget(dataTabPresenter.getDisplay().asWidget());

    selectScriptVariablesTabPresenter.bind();

    entitiesTabPresenter.bind();
    getDisplay().addEntitiesTabWidget(entitiesTabPresenter.getDisplay().asWidget());

    getDisplay().getHelpDeck().showWidget(0);

    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    dataTabPresenter.unbind();
    selectScriptVariablesTabPresenter.unbind();
    entitiesTabPresenter.unbind();
  }

  @Override
  public void revealDisplay() {
    revealTab();
  }

  private void revealTab() {
    // Always reveal data tab first.
    getDisplay().displayTab(0);
    refreshDisplay();
  }

  @Override
  public void refreshDisplay() {
    dataTabPresenter.setViewDto(viewDto);
    dataTabPresenter.refreshDisplay();
    entitiesTabPresenter.refreshDisplay();
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
    super.registerHandler(eventBus.addHandler(ViewSaveRequiredEvent.getType(), new ViewSaveRequiredHandler()));

    super.registerHandler(getDisplay().getViewTabs().addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {

      @Override
      public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
        // Widget w = getDisplay().getViewTabs().getWidget(getDisplay().getViewTabs().getSelectedIndex());
        // Switch help displayed.
      }
    }));
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {
    DeckPanel getHelpDeck();

    void addDataTabWidget(Widget widget);

    void addVariablesTabWidget(Widget widget);

    void addEntitiesTabWidget(Widget widget);

    TabLayoutPanel getViewTabs();

    void displayTab(int tabNumber);
  }

  class ViewConfigurationRequiredHandler implements ViewConfigurationRequiredEvent.Handler {

    @Override
    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      datasourceName = event.getDatasourceName();
      viewName = event.getViewName();

      ResourceRequestBuilderFactory.<ViewDto> newBuilder().forResource("/datasource/" + datasourceName + "/view/" + viewName).get().withCallback(new ResourceCallback<ViewDto>() {
        @Override
        public void onResource(Response response, ViewDto resource) {
          viewDto = resource;
          viewDto.setDatasourceName(datasourceName);
          viewDto.setName(viewName);

          // Set the variables tab widget according to the received ViewDto type.
          getDisplay().addVariablesTabWidget(getVariablesTabWidget());

          eventBus.fireEvent(new WorkbenchChangeEvent(ConfigureViewStepPresenter.this, false, false));
        }
      }).send();
    }

    private Widget getVariablesTabWidget() {
      Widget variablesTabWidget = null;

      JavaScriptViewDto jsViewDto = (JavaScriptViewDto) viewDto.getExtension(JavaScriptViewDto.ViewDtoExtensions.view);
      if(jsViewDto != null) {
        selectScriptVariablesTabPresenter.setViewDto(viewDto);
        variablesTabWidget = selectScriptVariablesTabPresenter.getDisplay().asWidget();
      }

      return variablesTabWidget;
    }
  }

  class ViewSaveRequiredHandler implements ViewSaveRequiredEvent.Handler {

    private ResponseCodeCallback callback;

    public ViewSaveRequiredHandler() {
      callback = createResponseCodeCallback();
    }

    @Override
    public void onViewUpdate(ViewSaveRequiredEvent event) {
      ViewDto viewDto = event.getViewDto();
      saveView(viewDto);
    }

    private void saveView(ViewDto viewDto) {
      ResourceRequestBuilderFactory.newBuilder()
      /**/.put()
      /**/.forResource("/datasource/" + datasourceName + "/view/" + viewName)
      /**/.accept("application/x-protobuf+json").withResourceBody(ViewDto.stringify(viewDto))
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
          } else {
            // Send event so save button and asterisk can be cleared.
            eventBus.fireEvent(new ViewSavedEvent());
          }
        }
      };
    }
  }
}