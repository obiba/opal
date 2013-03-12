/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.wizard.derive.presenter;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.BranchingWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.ScriptEvaluationPresenter.ScriptEvaluationCallback;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;

import static org.obiba.opal.web.gwt.app.client.util.VariableDtos.allCategoriesMissing;
import static org.obiba.opal.web.gwt.app.client.util.VariableDtos.hasCategories;
import static org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController.Builder.createChain;

@SuppressWarnings("OverlyCoupledClass")
public class DeriveVariablePresenter extends WizardPresenterWidget<DeriveVariablePresenter.Display> {

  public static final WizardType CategorizeWizardType = new WizardType();

  public static final WizardType CustomWizardType = new WizardType();

  public static final WizardType FromWizardType = new WizardType();

  private final Translations translations;

  private final DeriveCategoricalVariableStepPresenter categoricalPresenter;

  private final DeriveBooleanVariableStepPresenter booleanPresenter;

  private final DeriveNumericalVariableStepPresenter numericalPresenter;

  private final DeriveTemporalVariableStepPresenter temporalPresenter;

  private final DeriveOpenTextualVariableStepPresenter openTextualPresenter;

  private final DeriveCustomVariablePresenter deriveCustomVariablePresenter;

  private final DeriveFromVariablePresenter deriveFromVariablePresenter;

  private final ScriptEvaluationPresenter scriptEvaluationPresenter;

  private final DeriveConclusionPresenter deriveConclusionPresenter;

  private VariableDto variable;

  private TableDto table;

  private DerivationPresenter<?> derivationPresenter;

  private Runnable overwriteConfirmation;

  private Runnable viewCreationConfirmation;

  private WizardType wizardType;

  private String destinationDatasource;

  private String destinationView;

  @Inject
  @SuppressWarnings({ "PMD.ExcessiveParameterList", "ConstructorWithTooManyParameters" })
  public DeriveVariablePresenter(EventBus eventBus, Display view, Translations translations,
      DeriveTemporalVariableStepPresenter temporalPresenter, //
      DeriveCategoricalVariableStepPresenter categoricalPresenter, //
      DeriveBooleanVariableStepPresenter booleanPresenter, //
      DeriveNumericalVariableStepPresenter numericalPresenter, //
      DeriveOpenTextualVariableStepPresenter openTextualPresenter, //
      DeriveCustomVariablePresenter deriveCustomVariablePresenter, //
      DeriveFromVariablePresenter deriveFromVariablePresenter, //
      ScriptEvaluationPresenter scriptEvaluationPresenter, //
      DeriveConclusionPresenter deriveConclusionPresenter) {
    super(eventBus, view);
    this.translations = translations;
    this.categoricalPresenter = categoricalPresenter;
    this.booleanPresenter = booleanPresenter;
    this.numericalPresenter = numericalPresenter;
    this.temporalPresenter = temporalPresenter;
    this.openTextualPresenter = openTextualPresenter;
    this.deriveCustomVariablePresenter = deriveCustomVariablePresenter;
    this.deriveFromVariablePresenter = deriveFromVariablePresenter;
    this.scriptEvaluationPresenter = scriptEvaluationPresenter;
    this.deriveConclusionPresenter = deriveConclusionPresenter;
  }

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
    checkWizardArguments(event);

    Object[] eventParameters = event.getEventParameters();
    variable = (VariableDto) eventParameters[0];
    table = (TableDto) eventParameters[1];
    wizardType = (WizardType) event.getAssociatedType();

    if(wizardType == CategorizeWizardType || wizardType == FromWizardType) {
      if("binary".equals(variable.getValueType())) {
        // should not arrive here
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("Cannot categorize binary values.").build());
        return;
      }
      prepareDerivation();
    } else if(wizardType == CustomWizardType) {
      prepareCustomDerivation();
    } else {
      GWT.log("Unknown wizard type");
      return;
    }
    prepareConclusionStep();
  }

  private void checkWizardArguments(WizardRequiredEvent event) {
    Object[] eventParameters = event.getEventParameters();
    if(eventParameters.length != 2) {
      throw new IllegalArgumentException("Wizard is expected 2 arguments: VariableDto & TableDto");
    }
    if(!(eventParameters[0] instanceof VariableDto)) {
      throw new IllegalArgumentException("Unexpected event 1st parameter type (expected VariableDto)");
    }
    if(!(eventParameters[1] instanceof TableDto)) {
      throw new IllegalArgumentException("Unexpected event 2nd parameter type (expected TableDto)");
    }
  }

  private void prepareDerivation() {
    prepareFromVariableDerivation();
    prepareCategoricalDerivation();
    prepareNumericalDerivation();
    prepareTemporalDerivation();
    prepareBooleanDerivation();
    prepareOpenTextualDerivation(); // must be last branch because used as fallback branch
  }

  private void prepareFromVariableDerivation() {
    derivationPresenter = deriveFromVariablePresenter;
    if(wizardType == FromWizardType) {
      // input variable is the derived variable
      deriveFromVariablePresenter.initialize(null, table, null, variable);
    } else {
      // input variable is the variable to derive
      deriveFromVariablePresenter.initialize(table, null, variable, null);
    }
    deriveFromVariablePresenter.setWizardType(wizardType);
    getView().setStartStep(deriveFromVariablePresenter.getWizardStepBuilders(null).get(0));
    setInSlot(Display.Slots.Derivation, deriveFromVariablePresenter);
  }

  private void prepareBooleanDerivation() {
    List<DefaultWizardStepController.Builder> steps = booleanPresenter
        .getWizardStepBuilders(new SetCurrentPresenterStepInHandler(booleanPresenter));
    steps.add(getScriptEvaluationStep());
    steps.add(getConclusionStepBuilder());

    getView().addBranchStep(createChain(steps).build(), new BranchingWizardStepController.Condition() {
      @Override
      public boolean apply() {
        return "boolean".equals(deriveFromVariablePresenter.getOriginalVariable().getValueType());
      }
    });
  }

  private void prepareCategoricalDerivation() {
    List<DefaultWizardStepController.Builder> steps = categoricalPresenter
        .getWizardStepBuilders(new SetCurrentPresenterStepInHandler(categoricalPresenter));
    steps.add(getScriptEvaluationStep());
    steps.add(getConclusionStepBuilder());

    getView().addBranchStep(createChain(steps).build(), new BranchingWizardStepController.Condition() {
      @Override
      public boolean apply() {
        VariableDto originalVariable = deriveFromVariablePresenter.getOriginalVariable();
        String valueType = originalVariable.getValueType();
        return hasCategories(originalVariable) && ("text".equals(valueType) ||
            "integer".equals(valueType) && allCategoriesMissing(originalVariable) == false);
      }
    });
  }

  private void prepareTemporalDerivation() {
    List<DefaultWizardStepController.Builder> steps = temporalPresenter
        .getWizardStepBuilders(new SetCurrentPresenterStepInHandler(temporalPresenter));
    steps.add(getScriptEvaluationStep());
    steps.add(getConclusionStepBuilder());

    getView().addBranchStep(createChain(steps).build(), new BranchingWizardStepController.Condition() {
      @Override
      public boolean apply() {
        String valueType = deriveFromVariablePresenter.getOriginalVariable().getValueType();
        return "date".equals(valueType) || "datetime".equals(valueType);
      }
    });
  }

  private void prepareNumericalDerivation() {
    List<DefaultWizardStepController.Builder> steps = numericalPresenter
        .getWizardStepBuilders(new SetCurrentPresenterStepInHandler(numericalPresenter));
    steps.add(getScriptEvaluationStep());
    steps.add(getConclusionStepBuilder());

    getView().addBranchStep(createChain(steps).build(), new BranchingWizardStepController.Condition() {

      @Override
      public boolean apply() {
        String valueType = deriveFromVariablePresenter.getOriginalVariable().getValueType();
        return "integer".equals(valueType) || "decimal".equals(valueType);
      }
    });
  }

  private void prepareOpenTextualDerivation() {
    List<DefaultWizardStepController.Builder> steps = openTextualPresenter
        .getWizardStepBuilders(new SetCurrentPresenterStepInHandler(openTextualPresenter));
    steps.add(getScriptEvaluationStep());
    steps.add(getConclusionStepBuilder());

    getView().addBranchStep(createChain(steps).build(), new BranchingWizardStepController.Condition() {
      @Override
      public boolean apply() {
        VariableDto originalVariable = deriveFromVariablePresenter.getOriginalVariable();
        return "text".equals(originalVariable.getValueType()) || allCategoriesMissing(originalVariable);
      }
    });
  }

  private void prepareCustomDerivation() {
    derivationPresenter = deriveCustomVariablePresenter;
    deriveCustomVariablePresenter.initialize(table, null, variable, null);

    setInSlot(Display.Slots.Derivation, deriveCustomVariablePresenter);

    List<DefaultWizardStepController.Builder> steps = deriveCustomVariablePresenter
        .getWizardStepBuilders(new SetCurrentPresenterStepInHandler(deriveCustomVariablePresenter));
    steps.add(getScriptEvaluationStep());
    steps.add(getConclusionStepBuilder());

    getView().setStartStep(createChain(steps));
  }

  private void prepareConclusionStep() {
    addToSlot(Display.Slots.Derivation, deriveConclusionPresenter);
  }

  private DefaultWizardStepController.Builder getScriptEvaluationStep() {
    return getView().getScriptEvaluationStepBuilder(new ScriptEvaluationStepInHandler());
  }

  private DefaultWizardStepController.Builder getConclusionStepBuilder() {
    return deriveConclusionPresenter.getView().getConclusionStepBuilder(wizardType == FromWizardType)
        .onStepIn(new ConclusionStepInHandler());
  }

  @Override
  protected void onBind() {
    super.onBind();

    scriptEvaluationPresenter.setScriptEvaluationCallback(new ScriptEvaluationCallback() {

      @Override
      public void onSuccess(VariableDto variableDto) {
        getView().setScriptEvaluationSuccess(true, wizardType != FromWizardType);
      }

      @Override
      public void onFailure(VariableDto variableDto) {
        getView().setScriptEvaluationSuccess(false, wizardType != FromWizardType);
      }
    });
    setInSlot(Display.Slots.Summary, scriptEvaluationPresenter);
    addEventHandlers();
  }

  @Override
  protected void onCancel() {
    super.onCancel();
    derivationPresenter.onClose();
  }

  protected void addEventHandlers() {
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
  }

  @Override
  protected void onFinish() {
    if(wizardType != FromWizardType) {
      // validation
      List<String> errorMessages = deriveConclusionPresenter.validate();
      if(errorMessages.size() > 0) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(errorMessages).build());
        return;
      }
    }

    VariableDto derived = derivationPresenter.getDerivedVariable();
    if(wizardType == FromWizardType) {
      destinationDatasource = table.getDatasourceName();
      destinationView = table.getName();
    } else {
      destinationDatasource = deriveConclusionPresenter.getView().getDatasourceName();
      destinationView = deriveConclusionPresenter.getView().getViewName();
      derived.setName(deriveConclusionPresenter.getView().getDerivedName());
    }

    UriBuilder ub = UriBuilder.create().segment("datasource", destinationDatasource, "view", destinationView);
    ResourceRequestBuilderFactory.<ViewDto>newBuilder().forResource(ub.build()).get()
        .withCallback(new UpdateViewCallback(derived)).withCallback(Response.SC_NOT_FOUND, new CreateViewCallback())
        .send();

  }

  private void close(ViewDto view, VariableDto derived) {
    getView().hide();
    derivationPresenter.onClose();
    if(wizardType == FromWizardType) {
      getEventBus().fireEvent(new VariableSelectionChangeEvent(table, variable));
    } else if(deriveConclusionPresenter.getView().isOpenEditorSelected()) {
      getEventBus().fireEvent(new ViewConfigurationRequiredEvent(view, derived));
    }
  }

  private class ScriptEvaluationStepInHandler implements StepInHandler {
    @Override
    public void onStepIn() {
      derivationPresenter.generateDerivedVariable();
      scriptEvaluationPresenter.setOriginalTable(derivationPresenter.getOriginalTable(), true);
      scriptEvaluationPresenter.setOriginalVariable(derivationPresenter.getDerivedVariable());
    }
  }

  private class SetCurrentPresenterStepInHandler implements StepInHandler {

    private final DerivationPresenter<?> presenter;

    private SetCurrentPresenterStepInHandler(DerivationPresenter<?> presenter) {
      this.presenter = presenter;
    }

    @Override
    public void onStepIn() {
      presenter.initialize(deriveFromVariablePresenter.getOriginalTable(), derivationPresenter.getDestinationTable(),
          deriveFromVariablePresenter.getOriginalVariable(), derivationPresenter.getDerivedVariable());

      if(derivationPresenter != presenter) {
        if(presenter.isVisible() == false) {
          addToSlot(Display.Slots.Derivation, presenter);
        }
        derivationPresenter = presenter;
      }
    }
  }

  private class ConclusionStepInHandler implements StepInHandler {

    @Override
    public void onStepIn() {
      deriveConclusionPresenter
          .initialize(derivationPresenter.getOriginalTable(), derivationPresenter.getDestinationTable(),
              derivationPresenter.getOriginalVariable(), derivationPresenter.getDerivedVariable());
      derivationPresenter = deriveConclusionPresenter;
    }
  }

  private class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    @SuppressWarnings("AssignmentToNull")
    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(viewCreationConfirmation != null && event.getSource().equals(viewCreationConfirmation) &&
          event.isConfirmed()) {
        viewCreationConfirmation.run();
        viewCreationConfirmation = null;
      } else if(overwriteConfirmation != null && event.getSource().equals(overwriteConfirmation) &&
          event.isConfirmed()) {
        overwriteConfirmation.run();
        overwriteConfirmation = null;
      }
    }
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private final class CreateViewCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      viewCreationConfirmation = new Runnable() {
        @Override
        public void run() {
          saveVariable();
        }
      };
      getEventBus().fireEvent(
          ConfirmationRequiredEvent.createWithKeys(viewCreationConfirmation, "createView", "confirmCreateView"));
    }

    /**
     * Create a view with the derived variable.
     */
    private void saveVariable() {

      VariableDto derived = derivationPresenter.getDerivedVariable();

      // Build the ViewDto for the request.
      List<TableDto> tableDtos = new ArrayList<TableDto>();
      tableDtos.add(table);
      ViewDtoBuilder viewDtoBuilder = ViewDtoBuilder.newBuilder().setName(destinationView).fromTables(tableDtos);
      viewDtoBuilder.defaultVariableListView();
      ViewDto view = viewDtoBuilder.build();
      view.setDatasourceName(destinationDatasource);

      // add derived variable
      VariableListViewDto variableListViewDto = (VariableListViewDto) view
          .getExtension(VariableListViewDto.ViewDtoExtensions.view);
      JsArray<VariableDto> variables = JsArrays.create();
      variables.push(derived);
      variableListViewDto.setVariablesArray(variables);

      doRequest(view, derived);
    }

    private void doRequest(final ViewDto view, final VariableDto derived) {
      // request callback
      ResponseCodeCallback callback = new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_CREATED) {
            getEventBus().fireEvent(new DatasourceUpdatedEvent(view.getDatasourceName()));
            close(view, derived);
          } else if(response.getStatusCode() == Response.SC_FORBIDDEN) {
            getEventBus().fireEvent(NotificationEvent.newBuilder().error("UnauthorizedOperation").build());
          } else {
            getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
          }
        }
      };

      // create view request
      UriBuilder ub = UriBuilder.create().segment("datasource", destinationDatasource, "views");
      ResourceRequestBuilderFactory.newBuilder()//
          .post()//
          .forResource(ub.build())//
          .accept("application/x-protobuf+json").withResourceBody(ViewDto.stringify(view))//
          .withCallback(Response.SC_CREATED, callback)//
          .withCallback(Response.SC_OK, callback)//
          .withCallback(Response.SC_BAD_REQUEST, callback)//
          .withCallback(Response.SC_FORBIDDEN, callback)//
          .withCallback(Response.SC_NOT_FOUND, callback)//
          .withCallback(Response.SC_METHOD_NOT_ALLOWED, callback)//
          .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callback) //
          .send();
    }
  }

  private final class UpdateViewCallback implements ResourceCallback<ViewDto> {

    private final VariableDto derived;

    private UpdateViewCallback(VariableDto derived) {
      this.derived = derived;
    }

    @Override
    public void onResource(Response response, final ViewDto resource) {
      if(wizardType == FromWizardType) {
        saveVariable(resource, derived);
      } else {
        if(deriveConclusionPresenter.isValidDestinationView(resource)) {
          if(getVariablePosition(resource, derived) == -1) {
            saveVariable(resource, derived);
          } else {
            overwriteConfirmation = new Runnable() {
              @Override
              public void run() {
                saveVariable(resource, derived);
              }
            };
            getEventBus().fireEvent(ConfirmationRequiredEvent
                .createWithKeys(overwriteConfirmation, "overwriteVariable", "confirmOverwriteVariable"));
          }
        } else {
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(translations.invalidDestinationView()).build());
        }
      }
    }

    /**
     * Update a view with the derived variable.
     *
     * @param view
     * @param newDerived
     */
    private void saveVariable(ViewDto view, VariableDto newDerived) {
      // add or update derived variable
      int pos = getVariablePosition(view, newDerived);
      VariableListViewDto variableListViewDto = (VariableListViewDto) view
          .getExtension(VariableListViewDto.ViewDtoExtensions.view);
      JsArray<VariableDto> variables = JsArrays.toSafeArray(variableListViewDto.getVariablesArray());
      if(pos == -1) {
        variables.push(newDerived);
      } else {
        variables.set(pos, newDerived);
      }
      variableListViewDto.setVariablesArray(variables);

      doRequest(view);
    }

    /**
     * Get the position of the variable with the same name in the view
     *
     * @param view
     * @param derived
     * @return -1 if not found
     */
    private int getVariablePosition(ViewDto view, VariableDto derivedVariable) {
      VariableListViewDto variableListViewDto = (VariableListViewDto) view
          .getExtension(VariableListViewDto.ViewDtoExtensions.view);
      JsArray<VariableDto> variables = JsArrays.toSafeArray(variableListViewDto.getVariablesArray());
      int pos = -1;
      for(int i = 0; i < variables.length(); i++) {
        VariableDto var = variables.get(i);
        if(var.getName().equals(derivedVariable.getName())) {
          pos = i;
          break;
        }
      }
      return pos;
    }

    private void doRequest(final ViewDto view) {
      // request callback
      ResponseCodeCallback callback = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == Response.SC_OK) {
            // Refresh the variable
            UriBuilder ub = UriBuilder.create()
                .segment("datasource", view.getDatasourceName(), "table", view.getName(), "variable",
                    variable.getName());
            ResourceRequestBuilderFactory.<VariableDto>newBuilder().forResource(ub.build()).get()
                .withCallback(new ResourceCallback<VariableDto>() {
                  @Override
                  public void onResource(Response response, VariableDto variableDto) {
                    variable = variableDto;
                    close(view, derived);
                  }
                }).send();
          } else {
            getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
          }
        }
      };

      // update view request
      UriBuilder ub = UriBuilder.create().segment("datasource", view.getDatasourceName(), "view", view.getName());
      ResourceRequestBuilderFactory.newBuilder() //
          .put() //
          .forResource(ub.build()) //
          .withResourceBody(ViewDto.stringify(view)) //
          .withCallback(Response.SC_OK, callback) //
          .withCallback(Response.SC_BAD_REQUEST, callback)//
          .withCallback(Response.SC_NOT_FOUND, callback)//
          .withCallback(Response.SC_METHOD_NOT_ALLOWED, callback)//
          .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callback) //
          .send();
    }
  }

  public static class CategorizeWizard extends WizardProxy<DeriveVariablePresenter> {

    @Inject
    protected CategorizeWizard(EventBus eventBus, Provider<DeriveVariablePresenter> wizardProvider) {
      super(eventBus, CategorizeWizardType, wizardProvider);
    }
  }

  public static class CustomWizard extends WizardProxy<DeriveVariablePresenter> {

    @Inject
    protected CustomWizard(EventBus eventBus, Provider<DeriveVariablePresenter> wizardProvider) {
      super(eventBus, CustomWizardType, wizardProvider);
    }
  }

  public static class FromWizard extends WizardProxy<DeriveVariablePresenter> {

    @Inject
    protected FromWizard(EventBus eventBus, Provider<DeriveVariablePresenter> wizardProvider) {
      super(eventBus, FromWizardType, wizardProvider);
    }
  }

  public interface Display extends WizardView {

    enum Slots {
      Summary, Derivation
    }

    DefaultWizardStepController.Builder getScriptEvaluationStepBuilder(StepInHandler handler);

    void setScriptEvaluationSuccess(boolean success, boolean hasNextStep);

    void setStartStep(DefaultWizardStepController.Builder stepControllerBuilder);

    void addBranchStep(DefaultWizardStepController stepController, BranchingWizardStepController.Condition condition);

  }

}
