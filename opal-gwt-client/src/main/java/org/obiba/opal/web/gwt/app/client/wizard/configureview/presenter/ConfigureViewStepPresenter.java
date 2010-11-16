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
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavePendingEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSaveRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.wizard.createview.event.ShowEvaluateScriptResultEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.JavaScriptViewDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
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
  private VariablesListTabPresenter variablesListTabPresenter;

  @Inject
  private EntitiesTabPresenter entitiesTabPresenter;

  /**
   * {@link ViewDto} of view being configured.
   * 
   * This is initialized upon a {@link ViewConfigurationRequiredEvent} and updated on every
   * {@link ViewSaveRequiredEvent}.
   */
  private ViewDto viewDto;

  private boolean viewSavePending;

  private Runnable actionRequiringConfirmation;

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
    variablesListTabPresenter.bind();

    entitiesTabPresenter.bind();
    getDisplay().addEntitiesTabWidget(entitiesTabPresenter.getDisplay().asWidget());

    getDisplay().getHelpDeck().showWidget(0);
    viewSavePending = false;
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    dataTabPresenter.unbind();
    selectScriptVariablesTabPresenter.unbind();
    entitiesTabPresenter.unbind();
    variablesListTabPresenter.unbind();
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
    dataTabPresenter.refreshDisplay();
    entitiesTabPresenter.refreshDisplay();
    variablesListTabPresenter.refreshDisplay();
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

    super.registerHandler(getDisplay().getViewTabs().addSelectionHandler(new SelectionHandler<Integer>() {

      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        // Switch help displayed.
        getDisplay().getHelpDeck().showWidget(event.getSelectedItem());
      }
    }));
    super.registerHandler(getDisplay().getViewTabs().addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {

      @Override
      public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
        if(viewSavePending) {
          switchTabWithConfirmation(event.getItem());
          // Stop this event. If the user still wants to switch tabs we will handle it manually.
          event.cancel();
        }
      }
    }));
    super.registerHandler(eventBus.addHandler(ViewSavePendingEvent.getType(), new ViewSavePendingHandler()));
    super.registerHandler(eventBus.addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));

    super.registerHandler(eventBus.addHandler(ShowEvaluateScriptResultEvent.getType(), new ShowEvaluateScriptResultEvent.Handler() {

      @Override
      public void onShowEvaluateScriptResultEvent(ShowEvaluateScriptResultEvent event) {
        if(event.isResultsShown()) {
          getDisplay().setViewTabsHeight("80em");
        } else {
          getDisplay().setViewTabsHeight("50em");
        }
      }
    }));

  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {
    DeckPanel getHelpDeck();

    void setViewTabsHeight(String height);

    void addDataTabWidget(Widget widget);

    void addVariablesTabWidget(Widget widget);

    void addEntitiesTabWidget(Widget widget);

    TabLayoutPanel getViewTabs();

    void displayTab(int tabNumber);
  }

  class ViewSavePendingHandler implements ViewSavePendingEvent.Handler {

    @Override
    public void onSavePending(ViewSavePendingEvent event) {
      viewSavePending = true;
    }

  }

  class ViewConfigurationRequiredHandler implements ViewConfigurationRequiredEvent.Handler {

    @Override
    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      viewSavePending = false; // Save is not required when form is initially displayed.
      viewDto = event.getView();

      // Set the variables tab widget according to the received ViewDto type.
      getDisplay().addVariablesTabWidget(getVariablesTabWidget());

      eventBus.fireEvent(new WorkbenchChangeEvent(ConfigureViewStepPresenter.this, false, false));
    }

    private Widget getVariablesTabWidget() {
      Widget variablesTabWidget = null;

      JavaScriptViewDto jsViewDto = (JavaScriptViewDto) viewDto.getExtension(JavaScriptViewDto.ViewDtoExtensions.view);
      VariableListViewDto variableListDto = (VariableListViewDto) viewDto.getExtension(VariableListViewDto.ViewDtoExtensions.view);

      if(jsViewDto != null) {
        variablesTabWidget = selectScriptVariablesTabPresenter.getDisplay().asWidget();

        // Set the help widget for the current type of view (remove/insert).
        getDisplay().getHelpDeck().remove(1);
        getDisplay().getHelpDeck().insert(selectScriptVariablesTabPresenter.getDisplay().getHelpWidget(), 1);

      } else if(variableListDto != null) {
        variablesTabWidget = variablesListTabPresenter.getDisplay().asWidget();
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
      /**/.forResource("/datasource/" + viewDto.getDatasourceName() + "/view/" + viewDto.getName())
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
            viewSavePending = false;
          }
        }
      };
    }
  }

  private void switchTabWithConfirmation(final int index) {
    actionRequiringConfirmation = new Runnable() {
      public void run() {
        getDisplay().getViewTabs().selectTab(index, false);
        getDisplay().getHelpDeck().showWidget(index);
      }
    };
    eventBus.fireEvent(new ConfirmationRequiredEvent(actionRequiringConfirmation, "unsavedChangesTitle", "confirmUnsavedChanges"));
  }

  private class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    public void onConfirmation(ConfirmationEvent event) {
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) && event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }
}