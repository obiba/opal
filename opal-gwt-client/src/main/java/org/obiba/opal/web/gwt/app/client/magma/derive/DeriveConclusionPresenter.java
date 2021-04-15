/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.derive;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepController;
import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;

import static org.obiba.opal.web.gwt.app.client.magma.derive.DeriveConclusionPresenter.Display.FormField;

/**
 *
 */
public class DeriveConclusionPresenter extends DerivationPresenter<DeriveConclusionPresenter.Display> {

  private JsArray<DatasourceDto> datasources;

  @Inject
  public DeriveConclusionPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
  }

  @Override
  List<DefaultWizardStepController.Builder> getWizardStepBuilders(WizardStepController.StepInHandler stepInHandler) {
    return null;  // last wizard step
  }

  @Override
  void initialize(TableDto originalTable, TableDto destinationTable, VariableDto originalVariable,
      VariableDto derivedVariable) {
    super.initialize(originalTable, destinationTable, originalVariable, derivedVariable);
    findDatasources();
    getView().setDefaultDerivedName(originalVariable.getName());
  }

  @Override
  public void generateDerivedVariable() {
    // do nothing, derived variable is already generated
  }

  private void findDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder().forResource("/datasources").get()
        .withCallback(new DatasourcesCallback()).send();
  }

  boolean isValidDestinationView(ViewDto view) {
    return hasTableInFrom(view);
  }

  private boolean hasTableInFrom(ViewDto resource) {
    if(resource.getFromArray() == null) return false;
    JsArrayString froms = resource.getFromArray();
    for(int i = 0; i < froms.length(); i++) {
      String from = froms.get(i);
      if(from.equals(getOriginalTable().getDatasourceName() + "." + getOriginalTable().getName())) {
        return true;
      }
    }
    return false;
  }

  private final class DatasourcesCallback implements ResourceCallback<JsArray<DatasourceDto>> {

    @Override
    public void onResource(Response response, JsArray<DatasourceDto> resources) {
      datasources = JsArrays.toSafeArray(resources);
      getView().populateDatasources(datasources);
      for(DatasourceDto ds : JsArrays.toIterable(datasources)) {
        addViewSuggestions(ds);
      }
    }

    private void addViewSuggestions(final DatasourceDto ds) {
      JsArrayString array = ds.getViewArray();
      if(array != null) {
        for(int j = 0; j < array.length(); j++) {
          final String viewName = array.get(j);
          UriBuilder ub = UriBuilder.create().segment("datasource", ds.getName(), "view", viewName);
          ResourceRequestBuilderFactory.<ViewDto>newBuilder().forResource(ub.build()).get()//
              .withCallback(new ResourceCallback<ViewDto>() {

                @Override
                public void onResource(Response response, ViewDto resource) {
                  if(hasTableInFrom(resource)) {
                    getView().addViewSuggestion(ds, viewName);
                  }
                }

              })//
              .withCallback(Response.SC_FORBIDDEN, ResponseCodeCallback.NO_OP).send();

        }
      }
    }
  }

  public boolean validate() {
    getView().clearErrors();
    ValidationHandler validationHandler = new DeriveConclusionValidationHandler();
    return validationHandler.validate();
  }

  public class DeriveConclusionValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {

      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();

        validators.add(new RequiredTextValidator(getView().getDerivedName(), "DerivedVariableNameRequired",
            FormField.NAME.name()));
        validators.add(new RequiredTextValidator(getView().getViewName(), "DestinationViewNameRequired",
            FormField.VIEW_NAME.name()));

        validators.add(new ConditionValidator(
            destinationViewIsValid(getView().getDatasourceName(), getView().getViewName().getText()),
            "AddDerivedVariableToViewOnly", FormField.VIEW_NAME.name()));
      }

      return validators;
    }

    private HasValue<Boolean> destinationViewIsValid(final String datasourceName, final String viewName) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          for(DatasourceDto ds : JsArrays.toIterable(datasources)) {
            if(ds.getName().equals(datasourceName)) {
              for(String tName : JsArrays.toIterable(ds.getTableArray())) {
                if(tName.equals(viewName)) return isView(ds, viewName);
              }
            }
          }

          return true;
        }

        private boolean isView(DatasourceDto ds, String viewName) {
          for(int j = 0; j < ds.getViewArray().length(); j++) {
            String vName = ds.getViewArray().get(j);
            if(vName.equals(viewName)) return true;
          }
          return false;
        }
      };
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(FormField.valueOf(id), message);
    }
  }

  public interface Display extends View {

    enum FormField {
      NAME,
      VIEW_NAME
    }

    void setDefaultDerivedName(String name);

    DefaultWizardStepController.Builder getConclusionStepBuilder(boolean shouldSkip);

    void addViewSuggestion(DatasourceDto ds, String viewName);

    void populateDatasources(JsArray<DatasourceDto> datasources);

    HasText getDerivedName();

    String getDatasourceName();

    HasText getViewName();

    void clearErrors();

    void showError(@Nullable FormField formField, String message);
  }
}
