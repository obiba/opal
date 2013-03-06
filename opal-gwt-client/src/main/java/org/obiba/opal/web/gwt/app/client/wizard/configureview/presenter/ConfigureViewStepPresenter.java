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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavePendingEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSaveRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.JavaScriptViewDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class ConfigureViewStepPresenter extends PresenterWidget<ConfigureViewStepPresenter.Display> {

  private final DataTabPresenter dataTabPresenter;

  private final SelectScriptVariablesTabPresenter selectScriptVariablesTabPresenter;

  private final VariablesListTabPresenter variablesListTabPresenter;

  /**
   * {@link ViewDto} of view being configured.
   * <p/>
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
  public ConfigureViewStepPresenter(Display display, EventBus eventBus, //
      DataTabPresenter dataTabPresenter, SelectScriptVariablesTabPresenter selectScriptVariablesTabPresenter, //
      VariablesListTabPresenter variablesListTabPresenter) {
    super(eventBus, display);
    this.dataTabPresenter = dataTabPresenter;
    this.selectScriptVariablesTabPresenter = selectScriptVariablesTabPresenter;
    this.variablesListTabPresenter = variablesListTabPresenter;
  }

  @Override
  protected void onBind() {
    dataTabPresenter.bind();
    getView().addDataTabWidget(dataTabPresenter.getDisplay().asWidget());

    getView().getHelpDeck().showWidget(0);
    viewSavePending = false;
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    dataTabPresenter.unbind();
  }

  @Override
  public void onReveal() {
    revealTab();
  }

  private void revealTab() {
    // Always reveal data tab first.
    getView().displayTab(0);
    onReset();
  }

  @Override
  public void onReset() {
    dataTabPresenter.refreshDisplay();
  }

  //
  // Methods
  //

  private void addEventHandlers() {
    registerHandler(
        getEventBus().addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredHandler()));
    registerHandler(getEventBus().addHandler(ViewSaveRequiredEvent.getType(), new ViewSaveRequiredHandler()));

    registerHandler(getView().getViewTabs().addSelectionHandler(new SelectionHandler<Integer>() {

      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        // Switch help displayed.
        getView().getHelpDeck().showWidget(event.getSelectedItem());
      }
    }));
    registerHandler(getView().getViewTabs().addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {

      @Override
      public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
        if(viewSavePending) {
          getEventBus()
              .fireEvent(NotificationEvent.newBuilder().error("cannotSwitchTabBecauseOfUnsavedChanges").build());
          // Stop this event. If the user still wants to switch tabs we will handle it manually.
          event.cancel();
        }
      }
    }));
    registerHandler(getEventBus().addHandler(ViewSavePendingEvent.getType(), new ViewSavePendingHandler()));
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));

    registerHandler(getView().addCloseClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getView().hideDialog();
        // getEventBus().fireEvent(new ScriptEvaluationHideEvent());
      }
    }));
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends PopupView {
    enum Slots {
      Variables
    }

    DeckPanel getHelpDeck();

    HandlerRegistration addCloseClickHandler(ClickHandler handler);

    void addDataTabWidget(Widget widget);

    HorizontalTabLayout getViewTabs();

    void displayTab(int tabNumber);

    void hideDialog();
  }

  class ViewSavePendingHandler implements ViewSavePendingEvent.Handler {

    @Override
    public void onSavePending(ViewSavePendingEvent event) {
      viewSavePending = event.isPending();
    }

  }

  class ViewConfigurationRequiredHandler implements ViewConfigurationRequiredEvent.Handler {

    @Override
    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      viewSavePending = false; // Save is not required when form is initially displayed.
      viewDto = event.getView();

      onReveal();

      // Set the variables tab widget according to the received ViewDto type.
      setInSlot(Display.Slots.Variables, getVariablesTabWidget());

      RevealRootPopupContentEvent.fire(getEventBus(), ConfigureViewStepPresenter.this);
      if(event.getVariable() != null) {
        getView().displayTab(1);
      }
      dataTabPresenter.setViewDto(viewDto);
    }

    private PresenterWidget<?> getVariablesTabWidget() {
      PresenterWidget<?> variablesTabWidget = null;

      JavaScriptViewDto jsViewDto = (JavaScriptViewDto) viewDto.getExtension(JavaScriptViewDto.ViewDtoExtensions.view);
      VariableListViewDto variableListDto = (VariableListViewDto) viewDto
          .getExtension(VariableListViewDto.ViewDtoExtensions.view);

      if(jsViewDto != null) {
        variablesTabWidget = selectScriptVariablesTabPresenter;
        // Set the help widget for the current type of view (remove/insert).
        getView().getHelpDeck().remove(1);
        getView().getHelpDeck().insert(selectScriptVariablesTabPresenter.getView().getHelpWidget(), 1);
      } else if(variableListDto != null) {
        variablesTabWidget = variablesListTabPresenter;
      }

      return variablesTabWidget;
    }
  }

  class ViewSaveRequiredHandler implements ViewSaveRequiredEvent.Handler {

    private final ResponseCodeCallback callback;

    ViewSaveRequiredHandler() {
      callback = createResponseCodeCallback();
    }

    @Override
    public void onViewUpdate(ViewSaveRequiredEvent event) {
      ViewDto dto = event.getViewDto();
      saveView(dto);
    }

    private void saveView(ViewDto dto) {
      UriBuilder ub = UriBuilder.create().segment("datasource", dto.getDatasourceName(), "view", dto.getName());
      ResourceRequestBuilderFactory.newBuilder().put().forResource(ub.build()).accept("application/x-protobuf+json")
          .withResourceBody(ViewDto.stringify(dto)).withCallback(Response.SC_OK, callback)
          .withCallback(Response.SC_BAD_REQUEST, callback).send();
    }

    private ResponseCodeCallback createResponseCodeCallback() {
      return new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == Response.SC_BAD_REQUEST) {
            getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
          } else {
            // Send event so save button and asterisk can be cleared.
            viewSavePending = false;
            getEventBus().fireEvent(new ViewSavedEvent(viewDto));
          }
        }
      };
    }
  }

  private class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) &&
          event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }
}