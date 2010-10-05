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
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class EvaluateScriptPresenter extends WidgetPresenter<EvaluateScriptPresenter.Display> {

  private TableDto view;

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

    void showTestCount(boolean show);

    void clearResults();

    void setTestCount(int count);

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
    super.registerHandler(getDisplay().addHideResultsPanelHandler(new HideResultsCloseHandler()));
    super.registerHandler(getDisplay().addShowResultsPanelHandler(new ShowResultsOpenHandler()));
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

  public class TestScriptClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      String selectedScript = getDisplay().getSelectedScript();

      if(selectedScript.isEmpty()) {
        ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource("/datasource/" + view.getDatasourceName() + "/table/" + view.getName() + "/variables?script=" + getDisplay().getScript()).get().withCallback(new VariablesResourceCallback(false)).send();
      } else {
        ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource("/datasource/" + view.getDatasourceName() + "/table/" + view.getName() + "/variables").get().withCallback(new VariablesResourceCallback(true)).send();
      }

    }
  }

  public class ShowResultsOpenHandler implements OpenHandler {

    @Override
    public void onOpen(OpenEvent event) {
      getDisplay().showTestCount(false);
    }

  }

  public class HideResultsCloseHandler implements CloseHandler {

    @Override
    public void onClose(CloseEvent event) {
      getDisplay().showTestCount(true);
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
      List<Result> results = buildResultList(variables);

      String selectedScript = getDisplay().getSelectedScript();

      if(withValues) {
        ResourceRequestBuilderFactory.<JsArray<ValueDto>> newBuilder().forResource("/datasource/" + view.getDatasourceName() + "/table/" + view.getName() + "/variables/query?script=" + selectedScript).get().withCallback(new QueryVariablesResourceCallback(results, selectedScript)).send();
      } else {
        getDisplay().addResults(results);
      }

      getDisplay().setTestCount(variables.length());
    }

    private List<Result> buildResultList(JsArray<VariableDto> results) {
      List<Result> resultsList = new ArrayList<EvaluateScriptPresenter.Result>();
      for(int i = 0; i < results.length(); i++) {
        resultsList.add(new Result(results.get(i)));
      }
      return resultsList;
    }

  }

  public class QueryVariablesResourceCallback implements ResourceCallback<JsArray<ValueDto>> {

    private List<Result> results;

    private String selectedScript;

    public QueryVariablesResourceCallback(List<Result> results, String selectedScript) {
      super();
      this.selectedScript = selectedScript;
      this.results = results;
    }

    @Override
    public void onResource(Response response, JsArray<ValueDto> values) {
      getDisplay().clearResults();
      getDisplay().addResults(addValueToResults(results, values));
    }

    private List<Result> addValueToResults(List<Result> results, JsArray<ValueDto> values) {
      int i = 0;
      for(Result result : results) {
        result.setValue(values.get(i++));
      }
      return results;
    }
  }

  public class Result {
    private ValueDto value;

    private VariableDto variable;

    public Result(VariableDto variable) {
      this.variable = variable;
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
