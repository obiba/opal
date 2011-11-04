/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.presenter;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.Wizard;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

/**
 *
 */
public class DeriveVariablePresenter extends WidgetPresenter<DeriveVariablePresenter.Display> implements Wizard {

  public static final int PAGE_SIZE = 20;

  private static Translations translations = GWT.create(Translations.class);

  @Inject
  private DeriveCategoricalVariableStepPresenter categoricalPresenter;

  @Inject
  private DeriveBooleanVariableStepPresenter booleanPresenter;

  @Inject
  private DeriveNumericalVariableStepPresenter numericalPresenter;

  @Inject
  private ScriptEvaluationPresenter scriptEvaluationPresenter;

  private VariableDto variable;

  private TableDto table;

  private JsArray<DatasourceDto> datasources;

  private DerivationPresenter<?> derivationPresenter;

  private Runnable overwriteConfirmation;

  private Runnable viewCreationConfirmation;

  //
  // Constructors
  //

  @Inject
  public DeriveVariablePresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // Wizard Methods
  //

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length != 1) {
      throw new IllegalArgumentException("Variable DTO is expected as first wizard argument.");
    }

    if(!(event.getEventParameters()[0] instanceof VariableDto)) {
      throw new IllegalArgumentException("unexpected event parameter type (expected VariableDto)");
    }

    variable = (VariableDto) event.getEventParameters()[0];
    getDisplay().setDefaultDerivedName(variable.getName());

    ResourceRequestBuilderFactory.<TableDto> newBuilder().forResource(variable.getParentLink().getLink()).get().withCallback(new ResourceCallback<TableDto>() {
      @Override
      public void onResource(Response response, TableDto resource) {
        table = resource;
        scriptEvaluationPresenter.setTable(table);
      }

    }).send();

    updateDatasources();

    if(event.getWizardType() == WizardType.DERIVE_CATEGORIZE_VARIABLE) {
      // TODO
      String valueType = variable.getValueType();
      derivationPresenter = null;
      if(valueType.equals("integer") || valueType.equals("decimal")) {
        derivationPresenter = numericalPresenter;
      } else if(valueType.equals("text") && variable.getCategoriesArray() != null && variable.getCategoriesArray().length() > 0) {
        derivationPresenter = categoricalPresenter;
      } else if(valueType.equals("boolean")) {
        derivationPresenter = booleanPresenter;
      }

      if(derivationPresenter != null) {
        derivationPresenter.initialize(variable);
        getDisplay().appendWizardSteps(derivationPresenter.getWizardSteps());
      }
    } else {
      // TODO
    }
  }

  private void updateDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new DatasourcesCallback()).send();
  }

  private boolean hasTableInFrom(ViewDto resource) {
    if(resource.getFromArray() == null) return false;
    JsArrayString froms = resource.getFromArray();
    for(int i = 0; i < froms.length(); i++) {
      String from = froms.get(i);
      if(from.equals(table.getDatasourceName() + "." + table.getName())) {
        return true;
      }
    }
    return false;
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
  }

  @Override
  protected void onBind() {
    categoricalPresenter.bind();
    booleanPresenter.bind();
    numericalPresenter.bind();

    scriptEvaluationPresenter.bind();
    getDisplay().setScriptEvaluationWidget(scriptEvaluationPresenter.getDisplay());
    getDisplay().setScriptEvaluationStepInHandler(new ScriptEvaluationStepInHandler());
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    categoricalPresenter.unbind();
    booleanPresenter.unbind();
    numericalPresenter.unbind();

    scriptEvaluationPresenter.unbind();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  protected void addEventHandlers() {
    super.registerHandler(eventBus.addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
    super.registerHandler(getDisplay().addCancelClickHandler(new CancelClickHandler()));
    super.registerHandler(getDisplay().addFinishClickHandler(new FinishClickHandler()));
  }

  //
  // Inner classes and Interfaces
  //

  private final class DatasourcesCallback implements ResourceCallback<JsArray<DatasourceDto>> {
    @Override
    public void onResource(Response response, JsArray<DatasourceDto> resources) {
      datasources = JsArrays.toSafeArray(resources);
      getDisplay().populateDatasources(datasources);
      for(DatasourceDto ds : JsArrays.toIterable(datasources)) {
        addViewSuggestions(ds);
      }
    }

    private void addViewSuggestions(final DatasourceDto ds) {
      JsArrayString array = ds.getViewArray();
      if(array != null) {
        for(int j = 0; j < array.length(); j++) {
          final String viewName = array.get(j);
          ResourceRequestBuilderFactory.<ViewDto> newBuilder().forResource("/datasource/" + ds.getName() + "/view/" + viewName).get().withCallback(new ResourceCallback<ViewDto>() {

            @Override
            public void onResource(Response response, ViewDto resource) {
              if(hasTableInFrom(resource)) {
                getDisplay().addViewSuggestion(ds, viewName);
              }
            }

          }).send();

        }
      }
    }
  }

  final class ScriptEvaluationStepInHandler implements StepInHandler {
    @Override
    public void onStepIn() {
      VariableDto derived = derivationPresenter.getDerivedVariable();
      scriptEvaluationPresenter.setVariable(derived);
      scriptEvaluationPresenter.refreshDisplay();
    }
  }

  class CancelClickHandler implements ClickHandler {

    public void onClick(ClickEvent arg0) {
      getDisplay().hideDialog();
    }
  }

  class FinishClickHandler implements ClickHandler {

    public void onClick(ClickEvent arg0) {
      // validation
      List<String> errorMessages = validate();
      if(errorMessages.size() > 0) {
        eventBus.fireEvent(NotificationEvent.newBuilder().error(errorMessages).build());
        return;
      }

      final String datasourceName = getDisplay().getDatasourceName();
      final String viewName = getDisplay().getViewName();
      final VariableDto derived = derivationPresenter.getDerivedVariable();
      derived.setName(getDisplay().getDerivedName());

      ResourceRequestBuilderFactory.<ViewDto> newBuilder().forResource("/datasource/" + datasourceName + "/view/" + viewName).get().withCallback(new ResourceCallback<ViewDto>() {

        @Override
        public void onResource(Response response, final ViewDto resource) {
          if(!hasTableInFrom(resource)) {
            eventBus.fireEvent(NotificationEvent.newBuilder().error(translations.invalidDestinationView()).build());
          } else {
            if(getVariablePosition(resource, derived) != -1) {
              overwriteConfirmation = new Runnable() {
                public void run() {
                  saveVariable(resource, derived);
                }
              };
              eventBus.fireEvent(new ConfirmationRequiredEvent(overwriteConfirmation, "overwriteVariable", "confirmOverwriteVariable"));
            } else {
              saveVariable(resource, derived);
            }
          }
        }

      }).withCallback(404, new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          viewCreationConfirmation = new Runnable() {
            public void run() {
              saveVariable(datasourceName, viewName, derived);
            }
          };
          eventBus.fireEvent(new ConfirmationRequiredEvent(viewCreationConfirmation, "createView", "confirmCreateView"));
        }
      }).send();

    }

    /**
     * Get the position of the variable with the same name in the view
     * @param view
     * @param derived
     * @return -1 if not found
     */
    private int getVariablePosition(ViewDto view, VariableDto derived) {
      VariableListViewDto variableListViewDto = (VariableListViewDto) view.getExtension(VariableListViewDto.ViewDtoExtensions.view);
      JsArray<VariableDto> variables = JsArrays.toSafeArray(variableListViewDto.getVariablesArray());
      int pos = -1;
      for(int i = 0; i < variables.length(); i++) {
        VariableDto var = variables.get(i);
        if(var.getName().equals(derived.getName())) {
          pos = i;
          break;
        }
      }
      return pos;
    }

    /**
     * Update a view with the derived variable.
     * @param view
     * @param derived
     */
    private void saveVariable(final ViewDto view, final VariableDto derived) {
      // add or update derived variable
      int pos = getVariablePosition(view, derived);
      VariableListViewDto variableListViewDto = (VariableListViewDto) view.getExtension(VariableListViewDto.ViewDtoExtensions.view);
      JsArray<VariableDto> variables = JsArrays.toSafeArray(variableListViewDto.getVariablesArray());
      if(pos != -1) {
        variables.set(pos, derived);
      } else {
        variables.push(derived);
      }
      variableListViewDto.setVariablesArray(variables);

      // request callback
      ResponseCodeCallback callback = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == Response.SC_OK) {
            close(view, derived);
          } else {
            eventBus.fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
          }
        }
      };

      // update view request
      ResourceRequestBuilderFactory.newBuilder() //
      .put() //
      .forResource("/datasource/" + view.getDatasourceName() + "/view/" + view.getName()) //
      .accept("application/x-protobuf+json").withResourceBody(ViewDto.stringify(view)) //
      .withCallback(Response.SC_OK, callback) //
      .withCallback(Response.SC_BAD_REQUEST, callback)//
      .withCallback(Response.SC_NOT_FOUND, callback)//
      .withCallback(Response.SC_METHOD_NOT_ALLOWED, callback)//
      .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callback) //
      .send();
    }

    /**
     * Create a view with the derived variable.
     * @param datasourceName
     * @param viewName
     * @param derived
     */
    private void saveVariable(String datasourceName, String viewName, final VariableDto derived) {
      // Build the ViewDto for the request.
      List<TableDto> tableDtos = new ArrayList<TableDto>();
      tableDtos.add(table);
      ViewDtoBuilder viewDtoBuilder = ViewDtoBuilder.newBuilder().setName(viewName).fromTables(tableDtos);
      viewDtoBuilder.defaultVariableListView();
      final ViewDto view = viewDtoBuilder.build();
      view.setDatasourceName(datasourceName);

      // add derived variable
      VariableListViewDto variableListViewDto = (VariableListViewDto) view.getExtension(VariableListViewDto.ViewDtoExtensions.view);
      JsArray<VariableDto> variables = JsArrays.create();
      variables.push(derived);
      variableListViewDto.setVariablesArray(variables);

      // request callback
      ResponseCodeCallback callback = new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_CREATED) {
            eventBus.fireEvent(new DatasourceUpdatedEvent(view.getDatasourceName()));
            close(view, derived);
          } else {
            eventBus.fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
          }
        }
      };

      // create view request
      ResourceRequestBuilderFactory.newBuilder()//
      .post()//
      .forResource("/datasource/" + datasourceName + "/views")//
      .accept("application/x-protobuf+json").withResourceBody(ViewDto.stringify(view))//
      .withCallback(Response.SC_CREATED, callback)//
      .withCallback(Response.SC_OK, callback)//
      .withCallback(Response.SC_BAD_REQUEST, callback)//
      .withCallback(Response.SC_NOT_FOUND, callback)//
      .withCallback(Response.SC_METHOD_NOT_ALLOWED, callback)//
      .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callback) //
      .send();
    }

    private void close(ViewDto view, VariableDto derived) {
      getDisplay().hideDialog();
      if(getDisplay().isOpenEditorSelected()) {
        eventBus.fireEvent(new ViewConfigurationRequiredEvent(view, derived));
      }
    }

    private List<String> validate() {
      getDisplay().setDerivedNameError(false);
      getDisplay().setViewNameError(false);
      String datasourceName = getDisplay().getDatasourceName();
      String viewName = getDisplay().getViewName();

      List<String> errorMessages = new ArrayList<String>();
      if(getDisplay().getDerivedName().isEmpty()) {
        getDisplay().setDerivedNameError(true);
        errorMessages.add(translations.derivedVariableNameRequired());
      }
      if(viewName.isEmpty()) {
        getDisplay().setViewNameError(true);
        errorMessages.add(translations.destinationViewNameRequired());
      } else {
        // if destination table exists, it must be a view
        for(DatasourceDto ds : JsArrays.toIterable(datasources)) {
          if(ds.getName().equals(datasourceName)) {
            for(int i = 0; i < ds.getTableArray().length(); i++) {
              String tName = ds.getTableArray().get(i);
              if(tName.equals(viewName)) {
                boolean isView = false;
                for(int j = 0; j < ds.getViewArray().length(); j++) {
                  String vName = ds.getViewArray().get(j);
                  if(vName.equals(viewName)) {
                    isView = true;
                  }
                }
                if(!isView) {
                  getDisplay().setViewNameError(true);
                  errorMessages.add(translations.addDerivedVariableToViewOnly());
                }
                break;
              }
            }
          }
        }
      }
      return errorMessages;
    }
  }

  class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    public void onConfirmation(ConfirmationEvent event) {
      if(viewCreationConfirmation != null && event.getSource().equals(viewCreationConfirmation) && event.isConfirmed()) {
        viewCreationConfirmation.run();
        viewCreationConfirmation = null;
      } else if(overwriteConfirmation != null && event.getSource().equals(overwriteConfirmation) && event.isConfirmed()) {
        overwriteConfirmation.run();
        overwriteConfirmation = null;
      }
    }
  }

  public interface Display extends WidgetDisplay {

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    void addViewSuggestion(DatasourceDto ds, String viewName);

    void populateDatasources(JsArray<DatasourceDto> datasources);

    HandlerRegistration addFinishClickHandler(ClickHandler handler);

    void showDialog();

    void hideDialog();

    void clear();

    void setScriptEvaluationWidget(WidgetDisplay widget);

    void setScriptEvaluationStepInHandler(StepInHandler handler);

    void appendWizardSteps(List<DefaultWizardStepController> stepCtrls);

    void setDefaultDerivedName(String name);

    String getDerivedName();

    String getDatasourceName();

    String getViewName();

    boolean isOpenEditorSelected();

    void setDerivedNameError(boolean error);

    void setViewNameError(boolean error);
  }

}
