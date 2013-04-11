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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.wizard.BranchingWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class DeriveFromVariablePresenter extends DerivationPresenter<DeriveFromVariablePresenter.Display> {

  private WizardType wizardType;

  private String preSelectedDatasource;

  private String preSelectedTable;

  private TableDto table;

  private String preSelectedVariable;

  private final Collection<String> restrictedDatasources = new HashSet<String>();

  private final Collection<String> restrictedTables = new HashSet<String>();

  private final Map<String, TableDto> tablesByName = new HashMap<String, TableDto>();

  private final Map<String, VariableDto> variablesByName = new HashMap<String, VariableDto>();

  @Inject
  public DeriveFromVariablePresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
  }

  @Override
  public void generateDerivedVariable() {
  }

  @Override
  public void onReveal() {
    if(wizardType == DeriveVariablePresenter.FromWizardType) {
      findDerivedFromVariable();
    }
  }

  @Override
  protected void onBind() {
    addChangeHandlers();
  }

  private void findDerivedFromVariable() {
    String derivedFromUri = VariableDtos.getDerivedFrom(getDerivedVariable());
    if(derivedFromUri == null) {
      loadTables();
    } else {
      ResponseCodeCallback failureCallback = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          loadTables();
        }
      };

      ResourceRequestBuilderFactory.<VariableDto>newBuilder().forResource(derivedFromUri).get()
          .withCallback(new ResourceCallback<VariableDto>() {
            @Override
            public void onResource(Response response, VariableDto derivedFromVariable) {
              preSelectedVariable = derivedFromVariable.getName();

              ResourceRequestBuilderFactory.<TableDto>newBuilder()
                  .forResource(derivedFromVariable.getParentLink().getLink()).get()
                  .withCallback(new ResourceCallback<TableDto>() {
                    @Override
                    public void onResource(Response response, TableDto tableDto) {
                      preSelectedTable = tableDto.getName();
                      table = tableDto;
                      preSelectedDatasource = tableDto.getDatasourceName();
                      loadTables();
                    }
                  }).send();

            }
          })
              //
          .withCallback(Response.SC_NOT_FOUND, failureCallback)//
          .withCallback(Response.SC_FORBIDDEN, failureCallback)//
          .withCallback(Response.SC_INTERNAL_SERVER_ERROR, failureCallback)//
          .withCallback(Response.SC_BAD_REQUEST, failureCallback)//
          .send();
    }
  }

  private void loadTables() {

    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource("/datasources/tables").get()
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            List<String> tables = new ArrayList<String>();
            tablesByName.clear();
            if(resource != null) {
              for(int i = 0; i < resource.length(); i++) {
                TableDto tableDto = resource.get(i);
                if(restrictedTables.contains(tableDto.getName())) {
                  tablesByName.put(tableDto.getName(), tableDto);
                }
              }
              tables.addAll(tablesByName.keySet());
              Collections.sort(tables);
            }
            getView().addTableSelections(JsArrays.toSafeArray(resource));
            if(table != null) {
              getView().selectTable(table);
            }
            onTableSelection();
            loadVariables();
          }
        }).send();
  }

  private void loadVariables() {
    getView().setVariableListEnabled(false);
    UriBuilder uriBuilder = UriBuilder.create()
        .segment("datasource", getView().getSelectedTable().getDatasourceName(), "table",
            getView().getSelectedTable().getName(), "variables");
    ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder().forResource(uriBuilder.build()).get()
        .withCallback(new ResourceCallback<JsArray<VariableDto>>() {
          @Override
          public void onResource(Response response, JsArray<VariableDto> resource) {
            getView().setVariables(resource, preSelectedVariable);
            variablesByName.clear();
            if(resource != null) {
              for(int i = 0; i < resource.length(); i++) {
                VariableDto variableDto = resource.get(i);
                variablesByName.put(variableDto.getName(), variableDto);
              }
            }
            getView().setVariableListEnabled(true);
            onVariableSelection();
          }
        }).send();
  }

  @Override
  List<DefaultWizardStepController.Builder> getWizardStepBuilders(
      @Nullable WizardStepController.StepInHandler stepInHandler) {
    List<DefaultWizardStepController.Builder> stepBuilders = new ArrayList<DefaultWizardStepController.Builder>();
    stepBuilders.add(getView().getDeriveFromVariableStepController(wizardType != DeriveVariablePresenter.FromWizardType)
        .onValidate(new ValidationHandler() {
          @Override
          public boolean validate() {
            if(wizardType == DeriveVariablePresenter.FromWizardType && getView().getSelectedVariable() == null) {
              getEventBus().fireEvent(NotificationEvent.newBuilder().error("VariableSelectionIsRequired").build());
              return false;
            }
            return true;
          }
        }));
    return stepBuilders;
  }

  private void onTableSelection() {
    setOriginalTable(getView().getSelectedTable());
  }

  private void onVariableSelection() {
    setOriginalVariable(variablesByName.get(getView().getSelectedVariable()));
    VariableDtos.setDerivedFrom(getDerivedVariable(), getOriginalVariable());
  }

  private void addChangeHandlers() {
    getView().getTableList().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        onTableSelection();
        loadVariables();
      }
    });
    getView().getVariableList().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        onVariableSelection();
      }
    });
  }

  public void setWizardType(WizardType wizardType) {
    this.wizardType = wizardType;
  }

  public interface Display extends View {

    BranchingWizardStepController.Builder getDeriveFromVariableStepController(boolean skip);

    HasChangeHandlers getTableList();

    HasChangeHandlers getVariableList();

    void setVariableListEnabled(boolean b);

    /**
     * Get the variable selected by the user.
     */
    String getSelectedVariable();

    void setVariables(JsArray<VariableDto> variables, @Nullable String selectedVariable);

    void addTableSelections(JsArray<TableDto> tables);

    void selectTable(TableDto table);

    TableDto getSelectedTable();

  }

}
