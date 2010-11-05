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
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.inject.Inject;

public class EvaluateScriptPresenter extends WidgetPresenter<EvaluateScriptPresenter.Display> {

  public enum Mode {
    VARIABLE, ENTITY, ENTITY_VALUE;
  }

  private static final int pageSize = 10;

  private int currentPage;

  private boolean lastPage;

  private TableDto table;

  private Mode evaluationMode;

  @Inject
  public EvaluateScriptPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  public interface Display extends WidgetDisplay {
    String getScript();

    void setScript(String script);

    String getSelectedScript();

    void setSelectedScript(String script);

    HandlerRegistration addTestScriptClickHandler(ClickHandler handler);

    HandlerRegistration addNextPageClickHandler(ClickHandler handler);

    HandlerRegistration addPreviousPageClickHandler(ClickHandler handler);

    HandlerRegistration addScriptChangeHandler(ChangeHandler handler);

    void addResults(List<Result> variables);

    void clearResults();

    void initializeResultTable();

    void addVariableColumn();

    void addValueColumn();

    void setTestEntityCount(int count);

    void setTestVariableCount(int count);

    void showResults(boolean visible);

    void setReadOnly(boolean readOnly);

    void showErrorMessage(ClientErrorDto errorDto);

    void setPaging(int i, int j);

    void showPaging(boolean b);

    void setItemTypeVariables();

    void setItemTypeValues();

  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().addTestScriptClickHandler(new TestScriptClickHandler()));
    super.registerHandler(getDisplay().addNextPageClickHandler(new NextPageClickHandler()));
    super.registerHandler(getDisplay().addPreviousPageClickHandler(new PreviousPageClickHandler()));
  }

  public void setReadyOnly(boolean readyOnly) {
    getDisplay().setReadOnly(readyOnly);
  }

  public void setTable(TableDto table) {
    this.table = table;
  }

  public void setEvaluationMode(Mode evaluationMode) {
    this.evaluationMode = evaluationMode;
  }

  public void setScript(String script) {
    getDisplay().setScript(script);
  }

  public String getScript() {
    return getDisplay().getScript();
  }

  private List<Result> addValueToResults(List<Result> results, JsArray<ValueDto> values) {
    List<Result> resultsWithValues = null;
    if(results == null) {
      resultsWithValues = new ArrayList<EvaluateScriptPresenter.Result>();
      for(int i = 0; i < values.length(); i++) {
        resultsWithValues.add(new Result(values.get(i)));
      }
    } else {
      resultsWithValues = new ArrayList(results);
      int i = 0;
      for(Result result : resultsWithValues) {
        result.setValue(values.get(i++));
      }
    }
    return resultsWithValues;
  }

  public class TestScriptClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      currentPage = 1;
      lastPage = false;
      displayCurrentPageResults();
    }

  }

  private void displayCurrentPageResults() {

    int offset = (currentPage - 1) * pageSize;
    String viewResource = "/datasource/" + table.getDatasourceName() + "/table/" + table.getName();
    String variablesResource = viewResource + "/variables?limit=" + pageSize + "&offset=" + offset + "&script=";
    String transientVariableResource = viewResource + "/variable/_transient/values?limit=" + pageSize + "&offset=" + offset + "&script=";
    String entitiesResource = viewResource + "/entities?script=";

    evaluateScript(variablesResource, transientVariableResource, entitiesResource);

    getDisplay().showResults(true);
  }

  private void evaluateScript(String variablesResource, String transientVariableResource, String entitiesResource) {
    String selectedScript = URL.encodeQueryString(getDisplay().getSelectedScript());
    getDisplay().showPaging(true);
    setPagedItemType(evaluationMode);
    if(selectedScript.isEmpty()) {
      evaluateCompleteScript(variablesResource, transientVariableResource, entitiesResource);
    } else {
      evaluateSelectedScript(variablesResource, transientVariableResource, selectedScript);
    }
  }

  private void evaluateSelectedScript(String variablesResource, String transientVariableResource, String selectedScript) {
    if(evaluationMode == Mode.ENTITY_VALUE || evaluationMode == Mode.ENTITY) {
      ResourceRequestBuilderFactory.<JsArray<ValueDto>> newBuilder().forResource(transientVariableResource + selectedScript).get().withCallback(new EntityValueResourceCallback()).withCallback(400, new InvalidScriptResourceCallBack()).withCallback(500, new InvalidScriptResourceCallBack()).send();
    } else if(evaluationMode == Mode.VARIABLE) {
      ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource(variablesResource + selectedScript).get().withCallback(new VariablesResourceCallback(true)).withCallback(500, new InvalidScriptResourceCallBack()).send();
    }
  }

  private void evaluateCompleteScript(String variablesResource, String transientVariableResource, String entitiesResource) {
    String script = URL.encodeQueryString(getScript());
    if(evaluationMode == Mode.ENTITY_VALUE) {
      ResourceRequestBuilderFactory.<JsArray<ValueDto>> newBuilder().forResource(transientVariableResource + script).get().withCallback(new EntityValueResourceCallback()).withCallback(400, new InvalidScriptResourceCallBack()).withCallback(500, new InvalidScriptResourceCallBack()).send();
    } else if(evaluationMode == Mode.VARIABLE) {
      ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource(variablesResource + script).get().withCallback(new VariablesResourceCallback(false)).withCallback(400, new InvalidScriptResourceCallBack()).withCallback(500, new InvalidScriptResourceCallBack()).send();
    } else if(evaluationMode == Mode.ENTITY) {
      getDisplay().showPaging(false);
      ResourceRequestBuilderFactory.<JsArray<VariableEntityDto>> newBuilder().forResource(entitiesResource + script).get().withCallback(new EntityResourceCallback()).withCallback(400, new InvalidScriptResourceCallBack()).withCallback(500, new InvalidScriptResourceCallBack()).send();
    }
  }

  private void setPagedItemType(Mode evaluationMode) {
    if(evaluationMode == Mode.ENTITY || evaluationMode == Mode.ENTITY_VALUE) {
      getDisplay().setItemTypeValues();
    } else if(evaluationMode == Mode.VARIABLE) {
      getDisplay().setItemTypeVariables();
    }
  }

  private int getCurrentPageEnd(int currentPageSize) {
    return getCurrentPageStart() + currentPageSize - 1;
  }

  private int getCurrentPageStart() {
    return currentPage * pageSize - pageSize + 1;
  }

  public class PreviousPageClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(currentPage > 1) {
        lastPage = false;
        currentPage--;
      }
      displayCurrentPageResults();
    }

  }

  public class NextPageClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(!lastPage) {
        currentPage++;
        displayCurrentPageResults();
      }
    }

  }

  public class VariablesResourceCallback implements ResourceCallback<JsArray<VariableDto>> {

    private boolean withValues;

    public VariablesResourceCallback(boolean withValues) {
      this.withValues = withValues;
    }

    @Override
    public void onResource(Response response, JsArray<VariableDto> variables) {

      if(variables.length() < pageSize) {
        lastPage = true;
      }

      getDisplay().setPaging(getCurrentPageStart(), getCurrentPageEnd(variables.length()));
      getDisplay().clearResults();
      getDisplay().initializeResultTable();
      getDisplay().addVariableColumn();

      String selectedScript = getDisplay().getSelectedScript();
      List<Result> results = buildResultList(variables);

      if(withValues) {
        ResourceRequestBuilderFactory.<JsArray<ValueDto>> newBuilder().forResource("/datasource/" + table.getDatasourceName() + "/table/" + table.getName() + "/variables/query?script=" + selectedScript).get().withCallback(new VariableValueResourceCallback(results)).withCallback(400, new InvalidScriptResourceCallBack()).send();
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
      if(values.length() < pageSize) {
        lastPage = true;
      }
      getDisplay().setPaging(getCurrentPageStart(), getCurrentPageEnd(values.length()));
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
      getDisplay().showResults(true);
      getDisplay().showErrorMessage((ClientErrorDto) JsonUtils.unsafeEval(response.getText()));
    }
  }

  public static class Result {
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
