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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.TableDto;
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

    HandlerRegistration addTestScriptClickHandler(ClickHandler handler);

    void addResults(JsArray<VariableDto> variables);

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
      ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource("/datasource/" + view.getDatasourceName() + "/table/" + view.getName() + "/variables").get().withCallback(new VariablesResourceCallback()).send();
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

    @Override
    public void onResource(Response response, JsArray<VariableDto> variables) {
      getDisplay().clearResults();
      getDisplay().addResults(variables);
      getDisplay().setTestCount(variables.length());
    }

  }

}
