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

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.util.VariableDtos.ValueType;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ValueColumn.ValueSelectionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.JavaScriptErrorDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueSetDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class ScriptEvaluationPresenter extends PresenterWidget<ScriptEvaluationPresenter.Display> {

  private static Translations translations = GWT.create(Translations.class);

  private SummaryTabPresenter summaryTabPresenter;

  private VariableDto variable;

  private TableDto table;

  private ScriptEvaluationCallback scriptEvaluationCallback;

  //
  // Constructors
  //

  @Inject
  public ScriptEvaluationPresenter(final EventBus eventBus, final Display view, SummaryTabPresenter summaryTabPresenter) {
    super(eventBus, view);
    this.summaryTabPresenter = summaryTabPresenter;

    getView().setValueSelectionHandler(new ValueSelectionHandler() {

      @Override
      public void onBinaryValueSelection(VariableDto variable, int row, int column, ValueSetDto valueSet) {
        StringBuilder link = new StringBuilder(valueSet.getValuesArray().get(column).getLink());
        link.append("?");
        appendVariableLimitArguments(link);
        // TODO won't work with long script
        // OPAL-1346 encode script
        link.append("&script=" + URL.encodePathSegment(VariableDtos.getScript(variable)));
        getEventBus().fireEvent(new FileDownloadEvent(link.toString()));
      }

      @Override
      public void onValueSequenceSelection(VariableDto variable, int row, int column, ValueSetDto valueSet) {
        // TODO Auto-generated method stub

      }

    });

    getView().setValueSetFetcher(new ValueSetFetcherImpl());

  }

  public void setTable(TableDto table) {
    this.table = table;
    getView().setTable(table);
  }

  /**
   * Set the variable to be evaluated. Value type and script are extracted from the variable dto.
   * @param variable
   */
  public void setVariable(VariableDto variable) {
    this.variable = variable;
    getView().setVariable(variable);
    requestSummary();
  }

  public void setScriptEvaluationCallback(ScriptEvaluationCallback scriptEvaluationCallback) {
    this.scriptEvaluationCallback = scriptEvaluationCallback;
  }

  private void requestSummary() {
    String script = VariableDtos.getScript(variable);
    StringBuilder link = new StringBuilder();

    appendTable(link);
    link.append("/variable/_transient/summary?");
    appendVariableSummaryArguments(link);

    ResourceRequestBuilder<SummaryStatisticsDto> requestBuilder = ResourceRequestBuilderFactory.<SummaryStatisticsDto> newBuilder()//
    .forResource(link.toString()).withFormBody("script", script).post()//
    .accept("application/x-protobuf+json");

    if(variable != null) {
      JsArray<CategoryDto> cats = variable.getCategoriesArray();
      if(cats != null) {
        for(int i = 0; i < cats.length(); i++) {
          requestBuilder.withFormBody("category", cats.get(i).getName());
        }
      }
    }

    // No-op because already handled by the values request
    ResponseCodeCallback noOpCallback = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {

      }
    };

    requestBuilder.withCallback(400, noOpCallback).withCallback(500, noOpCallback);

    summaryTabPresenter.setRequestBuilder(requestBuilder);
    summaryTabPresenter.forgetSummary();
    summaryTabPresenter.refreshDisplay();
  }

  private void appendVariableSummaryArguments(StringBuilder link) {
    appendVariableLimitArguments(link);

    if(ValueType.TEXT.is(variable.getValueType()) && VariableDtos.allCategoriesMissing(variable)) {
      link.append("&nature=categorical")//
      .append("&distinct=true");
    }
  }

  private void appendVariableLimitArguments(StringBuilder link) {
    link.append("valueType=" + variable.getValueType()) //
    .append("&repeatable=" + variable.getIsRepeatable()); //
  }

  private void appendTable(StringBuilder link) {
    if(!Strings.isNullOrEmpty(table.getViewLink())) {
      // OPAL-879
      link.append(table.getViewLink()).append("/from");
    } else if(!Strings.isNullOrEmpty(table.getLink())) {
      link.append(table.getLink());
    } else {
      link.append("/datasource/").append(table.getDatasourceName()).append("/table/").append(table.getName());
    }
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    super.onBind();
    summaryTabPresenter.bind();
    getView().setSummaryTabWidget(summaryTabPresenter.getDisplay());
    // TODO
    // setInSlot(Display.Slots.Summary, summaryTabPresenter);
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    summaryTabPresenter.unbind();
  }

  //
  // Inner classes and Interfaces
  //

  private final class ValueSetFetcherImpl implements ValueSetFetcher {
    @Override
    public void request(int offset, int limit) {
      String script = VariableDtos.getScript(variable);

      StringBuilder link = new StringBuilder();
      appendTable(link);
      link.append("/variable/_transient/valueSets?limit=").append(limit)//
      .append("&offset=").append(offset).append("&");
      appendVariableLimitArguments(link);

      ValuesRequestCallback callback = new ValuesRequestCallback(offset);

      ResourceRequestBuilder<ValueSetsDto> requestBuilder = ResourceRequestBuilderFactory.<ValueSetsDto> newBuilder() //
      .forResource(link.toString()).post().withFormBody("script", script) //
      .withCallback(200, callback).withCallback(400, callback).withCallback(500, callback)//
      .accept("application/x-protobuf+json");
      requestBuilder.send();
    }
  }

  private final class ValuesRequestCallback implements ResponseCodeCallback {

    private final int offset;

    private ValuesRequestCallback(int offset) {
      this.offset = offset;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      boolean success = false;
      switch(response.getStatusCode()) {
      case Response.SC_OK:
        getView().getValueSetsProvider().populateValues(offset, (ValueSetsDto) JsonUtils.unsafeEval(response.getText()));
        success = true;
        break;
      case Response.SC_BAD_REQUEST:
        scriptInterpretationFail(response);
        break;
      default:
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(translations.scriptEvaluationFailed()).build());
        break;
      }
      if(scriptEvaluationCallback != null) {
        if(success) scriptEvaluationCallback.onSuccess(variable);
        else
          scriptEvaluationCallback.onFailure(variable);
      }
    }

    private void scriptInterpretationFail(Response response) {
      ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
      if(errorDto.getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors) != null) {
        List<JavaScriptErrorDto> errors = extractJavaScriptErrors(errorDto);
        for(JavaScriptErrorDto error : errors) {
          // TODO translate
          getEventBus().fireEvent(NotificationEvent.newBuilder().error("Error at line " + error.getLineNumber() + ", column " + error.getColumnNumber() + ": " + error.getMessage()).build());
        }
      }
    }

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
  }

  public interface ScriptEvaluationCallback {
    public void onSuccess(VariableDto variable);

    public void onFailure(VariableDto variable);
  }

  public interface Display extends View {

    enum Slots {
      Summary
    }

    void setSummaryTabWidget(WidgetDisplay widget);

    void setVariable(VariableDto variable);

    void setTable(TableDto table);

    HandlerRegistration setValueSelectionHandler(ValueSelectionHandler handler);

    ValueSetsProvider getValueSetsProvider();

    void setValueSetFetcher(ValueSetFetcher fetcher);
  }

  public interface ValueSetFetcher {
    void request(int offset, int limit);
  }

  public interface ValueSetsProvider {
    void populateValues(int offset, ValueSetsDto valueSets);
  }

}
