/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.event.ScriptEvaluationPopupEvent;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.ScriptEvaluationPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.util.Variables;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.JavaScriptErrorDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ScriptEvaluationPopupPresenter extends WidgetPresenter<ScriptEvaluationPopupPresenter.Display> {

  private static Translations translations = GWT.create(Translations.class);

  @Inject
  private ScriptEvaluationPresenter scriptEvaluationPresenter;

  @Inject
  public ScriptEvaluationPopupPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public void refreshDisplay() {
    scriptEvaluationPresenter.refreshDisplay();
  }

  @Override
  public void revealDisplay() {
    display.showDialog();
    scriptEvaluationPresenter.refreshDisplay();
  }

  class CloseHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      display.closeDialog();
    }
  }

  @Override
  protected void onBind() {
    scriptEvaluationPresenter.bind();
    display.getButton().addClickHandler(new CloseHandler());
    display.addScriptEvaluationWidget(scriptEvaluationPresenter.getDisplay().asWidget());
    addHandler();
  }

  @Override
  protected void onUnbind() {
    scriptEvaluationPresenter.unbind();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  private void addHandler() {
    super.registerHandler(eventBus.addHandler(ScriptEvaluationPopupEvent.getType(), new EvaluationHandler()));
  }

  class EvaluationHandler implements ScriptEvaluationPopupEvent.Handler {

    @Override
    public void onScriptEvaluation(final ScriptEvaluationPopupEvent event) {
      final TableDto table = event.getTable();
      final VariableDto variable = event.getVariable();

      final String scriptToEvaluate = Variables.getScript(variable);
      StringBuilder link = new StringBuilder(table.getLink()).append("/variable/_transient/values?limit=")//
      .append(20)//
      .append("&offset=")//
      .append(0)//
      .append("&valueType=" + variable.getValueType()) //
      .append("&repeatable=" + variable.getIsNewVariable()); //

      ScriptEvaluationCallback callback = new ScriptEvaluationCallback(table, variable);

      // TODO maybe we can avoid this request (because also done values tab...)
      ResourceRequestBuilder<JsArray<ValueDto>> requestBuilder = ResourceRequestBuilderFactory.<JsArray<ValueDto>> newBuilder() //
      .forResource(link.toString()).post().withFormBody("script", scriptToEvaluate) //

      .withCallback(200, callback).withCallback(400, callback).withCallback(500, callback) //
      .accept("application/x-protobuf+json");
      requestBuilder.send();
    }
  }

  private class ScriptEvaluationCallback implements ResponseCodeCallback {
    private final TableDto table;

    private final VariableDto variable;

    public ScriptEvaluationCallback(TableDto table, VariableDto variable) {
      this.table = table;
      this.variable = variable;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      GWT.log(response.getStatusCode() + "");
      switch(response.getStatusCode()) {
      case Response.SC_OK:
        scriptEvaluationPresenter.setTable(table);
        scriptEvaluationPresenter.setVariable(variable);
        revealDisplay();
        break;
      case Response.SC_BAD_REQUEST:
        scriptInterpretationFail(response);
        break;
      default:
        eventBus.fireEvent(NotificationEvent.newBuilder().error(translations.scriptEvaluationFailed()).build());
        break;
      }
    }
  }

  private void scriptInterpretationFail(Response response) {

    // TODO copy/paste from evaluate script view. need to factorize in common method
    ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
    if(errorDto.getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors) != null) {
      List<JavaScriptErrorDto> errors = extractJavaScriptErrors(errorDto);
      for(JavaScriptErrorDto error : errors) {
        eventBus.fireEvent(NotificationEvent.newBuilder().error("Error at line " + error.getLineNumber() + ", column " + error.getColumnNumber() + ": " + error.getMessage()).build());
      }
    }
  }

  // TODO copy/paste from evaluate script view. need to factorise in commom method
  @SuppressWarnings("unchecked")
  private List<JavaScriptErrorDto> extractJavaScriptErrors(ClientErrorDto errorDto) {
    List<JavaScriptErrorDto> javaScriptErrors = new ArrayList<JavaScriptErrorDto>();

    JsArray<JavaScriptErrorDto> errors = (JsArray<JavaScriptErrorDto>) errorDto.getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors);
    if(errors != null) {
      for(int i = 0; i < errors.length(); i++) {
        javaScriptErrors.add(errors.get(i));
      }
    }

    return javaScriptErrors;
  }

  public interface Display extends WidgetDisplay {

    void showDialog();

    void addScriptEvaluationWidget(Widget display);

    HasClickHandlers getButton();

    void closeDialog();

  }
}
