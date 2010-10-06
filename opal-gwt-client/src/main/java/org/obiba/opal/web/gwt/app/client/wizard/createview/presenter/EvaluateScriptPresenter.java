/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createview.presenter;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableEntityDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class EvaluateScriptPresenter extends WidgetPresenter<EvaluateScriptPresenter.Display> {

  public enum Mode {
    VARIABLE, ENTITY, ENTITY_VALUE;
  }

  private TableDto view;

  private Mode evaluationMode;

  @Inject
  public EvaluateScriptPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  public interface Display extends WidgetDisplay {
    String getScript();

    String getSelectedScript();

    void setSelectedScript(String script);

    HandlerRegistration addTestScriptClickHandler(ClickHandler handler);

    void addResults(List<Result> variables);

    HandlerRegistration addHideResultsPanelHandler(CloseHandler handler);

    HandlerRegistration addShowResultsPanelHandler(OpenHandler handler);

    void clearResults();

    void initializeResultTable();

    void addVariableColumn();

    void addValueColumn();

    void setTestEntityCount(int count);

    void setTestVariableCount(int count);

    void showResults(boolean visible);

    void showErrorMessage(ClientErrorDto errorDto);

  }

  @Override
  public void refreshDisplay() {
    // TODO Auto-generated method stub

  }

  @Override
  public void revealDisplay() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void onBind() {
    addEventHandlers();

  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().addTestScriptClickHandler(new TestScriptClickHandler()));
    // super.registerHandler(getDisplay().addHideResultsPanelHandler(new HideResultsCloseHandler()));
    // super.registerHandler(getDisplay().addShowResultsPanelHandler(new ShowResultsOpenHandler()));
  }

  @Override
  protected void onUnbind() {
    // TODO Auto-generated method stub

  }

  @Override
  public Place getPlace() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
    // TODO Auto-generated method stub
  }

  public void setView(TableDto view) {
    this.view = view;
  }

  public void setEvaluationMode(Mode evaluationMode) {
    this.evaluationMode = evaluationMode;
  }

  public class TestScriptClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {

      String viewResource = "/datasource/" + view.getDatasourceName() + "/table/" + view.getName();
      String variablesResource = viewResource + "/variables";
      String transientVariableResource = viewResource + "/variable/_transient/values";
      String entitiesResource = viewResource + "/entities";

      String selectedScript = getDisplay().getSelectedScript();
      if(selectedScript.isEmpty()) {
        String script = getDisplay().getScript();
        if(evaluationMode == Mode.ENTITY_VALUE) {
          ResourceRequestBuilderFactory.<JsArray<ValueDto>> newBuilder().forResource(transientVariableResource + "?script=" + script).get().withCallback(new EntityValueResourceCallback()).withCallback(400, new InvalidScriptResourceCallBack()).send();
        } else if(evaluationMode == Mode.VARIABLE) {
          ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource(variablesResource + "?script=" + script).get().withCallback(new VariablesResourceCallback(false)).withCallback(400, new InvalidScriptResourceCallBack()).send();
        } else if(evaluationMode == Mode.ENTITY) {
          ResourceRequestBuilderFactory.<JsArray<VariableEntityDto>> newBuilder().forResource(entitiesResource + "?script=" + script).get().withCallback(new EntityResourceCallback()).withCallback(400, new InvalidScriptResourceCallBack()).send();
        }
      } else {
        if(evaluationMode == Mode.ENTITY_VALUE || evaluationMode == Mode.ENTITY) {
          ResourceRequestBuilderFactory.<JsArray<ValueDto>> newBuilder().forResource(transientVariableResource + "?script=" + selectedScript).get().withCallback(new EntityValueResourceCallback()).withCallback(400, new InvalidScriptResourceCallBack()).send();
        } else if(evaluationMode == Mode.VARIABLE) {
          ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource(variablesResource).get().withCallback(new VariablesResourceCallback(true)).send();
        }
      }

      getDisplay().showResults(true);

    }
  }

  public class ShowResultsOpenHandler implements OpenHandler {

    @Override
    public void onOpen(OpenEvent event) {
      // getDisplay().showTestCount(false);
    }

  }

  public class HideResultsCloseHandler implements CloseHandler {

    @Override
    public void onClose(CloseEvent event) {
      // getDisplay().showTestCount(true);
    }

  }

  public class VariablesResourceCallback implements ResourceCallback<JsArray<VariableDto>> {

    private boolean withValues;

    public VariablesResourceCallback(boolean withValues) {
      this.withValues = withValues;
    }

    @Override
    public void onResource(Response response, JsArray<VariableDto> variables) {
      getDisplay().clearResults();
      getDisplay().setTestVariableCount(variables.length());
      getDisplay().initializeResultTable();
      getDisplay().addVariableColumn();

      List<Result> results = buildResultList(variables);

      String selectedScript = getDisplay().getSelectedScript();

      if(withValues) {
        ResourceRequestBuilderFactory.<JsArray<ValueDto>> newBuilder().forResource("/datasource/" + view.getDatasourceName() + "/table/" + view.getName() + "/variables/query?script=" + selectedScript).get().withCallback(new VariableValueResourceCallback(results)).withCallback(400, new InvalidScriptResourceCallBack()).send();
      } else {
        getDisplay().addResults(results);
      }

      getDisplay().showResults(true);

    }

    private List<Result> buildResultList(JsArray<VariableDto> results) {
      List<Result> resultsList = new ArrayList<EvaluateScriptPresenter.Result>();
      for(int i = 0; i < results.length(); i++) {
        resultsList.add(new Result(results.get(i)));
      }
      return resultsList;
    }

  }

  public class VariableValueResourceCallback implements ResourceCallback<JsArray<ValueDto>> {

    private List<Result> results;

    public VariableValueResourceCallback(List<Result> results) {
      super();
      this.results = results;
    }

    @Override
    public void onResource(Response response, JsArray<ValueDto> values) {
      getDisplay().clearResults();
      getDisplay().addValueColumn();
      getDisplay().addResults(addValueToResults(results, values));
    }

  }

  private List<Result> addValueToResults(List<Result> results, JsArray<ValueDto> values) {
    if(results == null) {
      results = new ArrayList<EvaluateScriptPresenter.Result>();
      for(int i = 0; i < values.length(); i++) {
        results.add(new Result(values.get(i)));
      }
    } else {
      int i = 0;
      for(Result result : results) {
        result.setValue(values.get(i++));
      }
    }
    return results;
  }

  private class EntityResourceCallback implements ResourceCallback<JsArray<VariableEntityDto>> {

    @Override
    public void onResource(Response response, JsArray<VariableEntityDto> resource) {
      getDisplay().clearResults();
      getDisplay().setTestEntityCount(resource.length());
    }

  }

  private class EntityValueResourceCallback implements ResourceCallback<JsArray<ValueDto>> {

    @Override
    public void onResource(Response response, JsArray<ValueDto> values) {
      getDisplay().clearResults();
      getDisplay().initializeResultTable();
      getDisplay().addValueColumn();
      getDisplay().addResults(addValueToResults(null, values));
    }

  }

  public class InvalidScriptResourceCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      getDisplay().clearResults();
      getDisplay().showErrorMessage(null);
      // getDisplay().showErrorMessage((ClientErrorDto) JsonUtils.unsafeEval(response.getText()));
    }

  }

  public class Result {
    private ValueDto value;

    private VariableDto variable;

    public Result(VariableDto variable) {
      this.variable = variable;
    }

    public Result(ValueDto value) {
      this.value = value;
    }

    public ValueDto getValue() {
      return value;
    }

    public void setValue(ValueDto value) {
      this.value = value;
    }

    public VariableDto getVariable() {
      return variable;
    }

    public void setVariable(VariableDto variable) {
      this.variable = variable;
    }

  }

}
