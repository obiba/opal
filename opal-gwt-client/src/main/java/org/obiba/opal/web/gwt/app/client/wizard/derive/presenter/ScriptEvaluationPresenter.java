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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
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

  private static final Translations translations = GWT.create(Translations.class);

  private static final TranslationMessages translationMessages = GWT.create(TranslationMessages.class);

  private final SummaryTabPresenter summaryTabPresenter;

  private VariableDto originalVariable;

  private TableDto originalTable;

  private boolean asTable;

  private ScriptEvaluationCallback scriptEvaluationCallback;

  //
  // Constructors
  //

  @Inject
  public ScriptEvaluationPresenter(EventBus eventBus, Display view, SummaryTabPresenter summaryTabPresenter) {
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
        link.append("&script=").append(URL.encodePathSegment(VariableDtos.getScript(variable)));
        getEventBus().fireEvent(new FileDownloadEvent(link.toString()));
      }

      @Override
      public void onGeoValueSelection(VariableDto variableDto, int row, int column, ValueSetDto valueSet,
          ValueSetsDto.ValueDto value) {

      }

      @Override
      public void onValueSequenceSelection(VariableDto variable, int row, int column, ValueSetDto valueSet) {
      }

    });

    getView().setValueSetFetcher(new ValueSetFetcherImpl());
  }

  public void setOriginalTable(TableDto originalTable) {
    setOriginalTable(originalTable, false);
  }

  public void setOriginalTable(TableDto originalTable, boolean asTable) {
    this.asTable = asTable;
    this.originalTable = originalTable;
    getView().setOriginalTable(originalTable);
  }

  /**
   * Set the variable to be evaluated. Value type and script are extracted from the variable dto.
   *
   * @param originalVariable
   */
  public void setOriginalVariable(VariableDto originalVariable) {
    this.originalVariable = originalVariable;
    getView().setOriginalVariable(originalVariable);
    requestSummary();
  }

  public void setScriptEvaluationCallback(ScriptEvaluationCallback scriptEvaluationCallback) {
    this.scriptEvaluationCallback = scriptEvaluationCallback;
  }

  private void requestSummary() {
    StringBuilder link = new StringBuilder();
    appendTable(link);
    link.append("/variable/_transient/summary?");
    appendVariableSummaryArguments(link);

    ResourceRequestBuilder<SummaryStatisticsDto> requestBuilder = requestSummaryBuilder(link.toString());

    summaryTabPresenter.setRequestBuilder(requestBuilder);
    summaryTabPresenter.forgetSummary();
    summaryTabPresenter.onReset();
  }

  private ResourceRequestBuilder<SummaryStatisticsDto> requestSummaryBuilder(String link) {
    String script = VariableDtos.getScript(originalVariable);
    ResourceRequestBuilder<SummaryStatisticsDto> requestBuilder = ResourceRequestBuilderFactory
        .<SummaryStatisticsDto>newBuilder() //
        .forResource(link).withFormBody("script", script).post() //
        .accept("application/x-protobuf+json");

    if(originalVariable != null) {
      JsArray<CategoryDto> cats = originalVariable.getCategoriesArray();
      if(cats != null) {
        for(int i = 0; i < cats.length(); i++) {
          requestBuilder.withFormBody("category", cats.get(i).getName());
        }
      }
    }

    ResponseCodeCallback callback = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(scriptEvaluationCallback == null) return;
        if(response.getStatusCode() == Response.SC_OK) {
          scriptEvaluationCallback.onSuccess(originalVariable);
        } else {
          scriptEvaluationCallback.onFailure(originalVariable);
        }
      }
    };

    requestBuilder.withCallback(Response.SC_OK, callback)//
        .withCallback(Response.SC_BAD_REQUEST, callback)//
        .withCallback(Response.SC_FORBIDDEN, callback)//
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callback);

    return requestBuilder;
  }

  private void appendVariableSummaryArguments(StringBuilder link) {
    appendVariableLimitArguments(link);

    if(ValueType.TEXT.is(originalVariable.getValueType()) && VariableDtos.allCategoriesMissing(originalVariable)) {
      link.append("&nature=categorical")//
          .append("&distinct=true");
    }
  }

  private void appendVariableLimitArguments(StringBuilder link) {
    link.append("valueType=").append(originalVariable.getValueType()) //
        .append("&repeatable=").append(originalVariable.getIsRepeatable());
  }

  private void appendTable(StringBuilder link) {
    if(!Strings.isNullOrEmpty(originalTable.getViewLink()) && !asTable) {
      // OPAL-879
      link.append(originalTable.getViewLink()).append("/from");
    } else if(!Strings.isNullOrEmpty(originalTable.getLink())) {
      link.append(originalTable.getLink());
    } else {
      link.append("/datasource/").append(originalTable.getDatasourceName()).append("/table/")
          .append(originalTable.getName());
    }
  }

  @Override
  protected void onBind() {
    super.onBind();
    summaryTabPresenter.bind();
    getView().setSummaryTabWidget(summaryTabPresenter.getView());
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
      String script = VariableDtos.getScript(originalVariable);

      StringBuilder link = new StringBuilder();
      appendTable(link);
      link.append("/valueSets/variable/_transient?limit=").append(limit)//
          .append("&offset=").append(offset).append("&");
      appendVariableLimitArguments(link);

      ResponseCodeCallback callback = new ValuesRequestCallback(offset);

      ResourceRequestBuilder<ValueSetsDto> requestBuilder = ResourceRequestBuilderFactory.<ValueSetsDto>newBuilder() //
          .forResource(link.toString()).post().withFormBody("script", script) //
          .withCallback(Response.SC_OK, callback)//
          .withCallback(Response.SC_BAD_REQUEST, callback)//
          .withCallback(Response.SC_FORBIDDEN, callback)//
          .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callback)//
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
      switch(response.getStatusCode()) {
        case Response.SC_OK:
          if(response.getText() != null) {
            getView().setValuesVisible(true);
            getView().getValueSetsProvider()
                .populateValues(offset, (ValueSetsDto) JsonUtils.unsafeEval(response.getText()));
          }
          break;
        case Response.SC_FORBIDDEN:
          getView().setValuesVisible(false);
          break;
        case Response.SC_BAD_REQUEST:
          getView().setValuesVisible(true);
          scriptInterpretationFail(response);
          break;
        default:
          getView().setValuesVisible(true);
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(translations.scriptEvaluationFailed()).build());
          break;
      }
    }

    private void scriptInterpretationFail(Response response) {
      ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
      if(errorDto.getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors) != null) {
        List<JavaScriptErrorDto> errors = extractJavaScriptErrors(errorDto);
        for(JavaScriptErrorDto error : errors) {
          getEventBus().fireEvent(NotificationEvent.newBuilder()
              .error(translationMessages.errorAt(error.getLineNumber(), error.getColumnNumber(), error.getMessage()))
              .build());
        }
      }
    }

    @SuppressWarnings("unchecked")
    private List<JavaScriptErrorDto> extractJavaScriptErrors(ClientErrorDto errorDto) {
      List<JavaScriptErrorDto> javaScriptErrors = new ArrayList<JavaScriptErrorDto>();

      JsArray<JavaScriptErrorDto> errors = (JsArray<JavaScriptErrorDto>) errorDto
          .getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors);
      if(errors != null) {
        for(int i = 0; i < errors.length(); i++) {
          javaScriptErrors.add(errors.get(i));
        }
      }
      return javaScriptErrors;
    }
  }

  public interface ScriptEvaluationCallback {
    void onSuccess(VariableDto variable);

    void onFailure(VariableDto variable);
  }

  public interface Display extends View {

    enum Slots {
      Summary
    }

    void setSummaryTabWidget(SummaryTabPresenter.Display widget);

    void setValuesVisible(boolean visible);

    void setOriginalVariable(VariableDto variable);

    void setOriginalTable(TableDto table);

    HandlerRegistration setValueSelectionHandler(ValueSelectionHandler handler);

    ValueSetsProvider getValueSetsProvider();

    void setValueSetFetcher(ValueSetFetcher fetcher);

    void setCommentVisible(boolean b);
  }

  public interface ValueSetFetcher {
    void request(int offset, int limit);
  }

  public interface ValueSetsProvider {
    void populateValues(int offset, ValueSetsDto valueSets);
  }

}
