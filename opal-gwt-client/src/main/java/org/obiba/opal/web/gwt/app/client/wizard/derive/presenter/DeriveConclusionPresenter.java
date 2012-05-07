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
import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController;
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
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.View;

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

  List<String> validate() {

    getView().setDerivedNameError(false);
    getView().setViewNameError(false);

    String viewName = getView().getViewName();

    List<String> errorMessages = new ArrayList<String>();
    if(getView().getDerivedName().isEmpty()) {
      getView().setDerivedNameError(true);
      errorMessages.add(translations.derivedVariableNameRequired());
    }
    if(viewName.isEmpty()) {
      getView().setViewNameError(true);
      errorMessages.add(translations.destinationViewNameRequired());
    } else {
      // if destination table exists, it must be a view
      validateDestinationView(errorMessages);
    }
    return errorMessages;
  }

  boolean isValidDestinationView(ViewDto view) {
    return hasTableInFrom(view);
  }

  private void validateDestinationView(Collection<String> errorMessages) {
    String datasourceName = getView().getDatasourceName();
    for(DatasourceDto ds : JsArrays.toIterable(datasources)) {
      if(ds.getName().equals(datasourceName)) {
        validateDestinationView(errorMessages, ds);
      }
    }
  }

  private void validateDestinationView(Collection<String> errorMessages, DatasourceDto ds) {
    if(ds.getTableArray() == null) return;

    String viewName = getView().getViewName();
    for(int i = 0; i < ds.getTableArray().length(); i++) {
      String tName = ds.getTableArray().get(i);
      if(tName.equals(viewName)) {
        if(!isView(ds, viewName)) {
          getView().setViewNameError(true);
          errorMessages.add(translations.addDerivedVariableToViewOnly());
        }
        break;
      }
    }
  }

  private boolean isView(DatasourceDto ds, String viewName) {
    for(int j = 0; j < ds.getViewArray().length(); j++) {
      String vName = ds.getViewArray().get(j);
      if(vName.equals(viewName)) return true;
    }
    return false;
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
              .withCallback(Response.SC_FORBIDDEN, new ResponseCodeCallback() {

                @Override
                public void onResponseCode(Request request, Response response) {
                  // ignore
                }
              }).send();

        }
      }
    }

  }

  public interface Display extends View {

    void setDefaultDerivedName(String name);

    DefaultWizardStepController.Builder getConclusionStepBuilder(boolean shouldSkip);

    void addViewSuggestion(DatasourceDto ds, String viewName);

    void populateDatasources(JsArray<DatasourceDto> datasources);

    String getDerivedName();

    String getDatasourceName();

    String getViewName();

    boolean isOpenEditorSelected();

    void setDerivedNameError(boolean error);

    void setViewNameError(boolean error);
  }
}
