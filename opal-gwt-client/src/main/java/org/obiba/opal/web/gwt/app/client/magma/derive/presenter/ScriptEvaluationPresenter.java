/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.derive.presenter;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.event.ScriptEvaluationFailedEvent;
import org.obiba.opal.web.gwt.app.client.magma.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos.ValueType;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ValueColumn.ValueSelectionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.JavaScriptErrorDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueSetDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_OK;

/**
 *
 */
public class ScriptEvaluationPresenter extends PresenterWidget<ScriptEvaluationPresenter.Display> {

  private final SummaryTabPresenter summaryTabPresenter;

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private VariableDto originalVariable;

  private TableDto originalTable;

  private boolean asTable;

  private ScriptEvaluationCallback scriptEvaluationCallback;

  @Inject
  public ScriptEvaluationPresenter(EventBus eventBus, Display view, SummaryTabPresenter summaryTabPresenter,
      Translations translations, TranslationMessages translationMessages) {
    super(eventBus, view);
    this.summaryTabPresenter = summaryTabPresenter;
    this.translations = translations;
    this.translationMessages = translationMessages;

    getView().setValueSelectionHandler(new ValueSelectionHandler() {

      @Override
      public void onBinaryValueSelection(VariableDto variable, int row, int column, ValueSetDto valueSet) {
        UriBuilder uriBuilder = UriBuilder.create();
        uriBuilder.fromPath(valueSet.getValuesArray().get(column).getLink());
        appendVariableLimitArguments(uriBuilder);
        // TODO won't work with long script
        // OPAL-1346 encode script
        uriBuilder.query("script", URL.encodePathSegment(VariableDtos.getScript(variable)));
        getEventBus().fireEvent(new FileDownloadRequestEvent(uriBuilder.build()));
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
  }

  public void setScriptEvaluationCallback(ScriptEvaluationCallback scriptEvaluationCallback) {
    this.scriptEvaluationCallback = scriptEvaluationCallback;
  }

  private void appendVariableLimitArguments(UriBuilder uriBuilder) {
    uriBuilder.query("valueType", originalVariable.getValueType(), //
        "repeatable", String.valueOf(originalVariable.getIsRepeatable()));
  }

  private void appendTable(UriBuilder uriBuilder) {
    if(!Strings.isNullOrEmpty(originalTable.getViewLink()) && !asTable) {
      uriBuilder.fromPath(originalTable.getViewLink());
    } else if(!Strings.isNullOrEmpty(originalTable.getLink())) {
      uriBuilder.fromPath(originalTable.getLink());
    } else {
      uriBuilder.segment("datasource", originalTable.getDatasourceName(), "table", originalTable.getName());
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

      UriBuilder uriBuilder = UriBuilder.create();
      appendTable(uriBuilder);
      uriBuilder.segment("valueSets", "variable", "_transient");
      uriBuilder.query("limit", String.valueOf(limit), //
          "name", originalVariable.getName(), //
          "offset", String.valueOf(offset));
      appendVariableLimitArguments(uriBuilder);

      ResourceRequestBuilderFactory.<ValueSetsDto>newBuilder() //
          .forResource(uriBuilder.build()) //
          .withFormBody("script", VariableDtos.getScript(originalVariable)) //
          .withCallback(new ValuesRequestCallback(offset), SC_OK, SC_BAD_REQUEST, SC_FORBIDDEN,
              SC_INTERNAL_SERVER_ERROR) //
          .accept("application/x-protobuf+json") //
          .post().send();
    }
  }

  private final class ValuesRequestCallback implements ResponseCodeCallback {

    private final int offset;

    private ValuesRequestCallback(int offset) {
      this.offset = offset;
    }

    @Override
    @SuppressWarnings("PMD.NcssMethodCount")
    public void onResponseCode(Request request, Response response) {
      switch(response.getStatusCode()) {
        case SC_OK:
          if(response.getText() != null) {
            getView().setValuesVisible(true);
            getView().getValueSetsProvider()
                .populateValues(offset, (ValueSetsDto) JsonUtils.unsafeEval(response.getText()));
            requestSummary();
          }
          break;
        case SC_FORBIDDEN:
          getView().setValuesVisible(false);
          break;
        case SC_BAD_REQUEST:
          getView().setValuesVisible(true);
          scriptInterpretationFail(response);
          break;
        default:
          getView().setValuesVisible(true);
          fireEvent(new ScriptEvaluationFailedEvent(translations.scriptEvaluationFailed()));
          summaryTabPresenter.hideSummaryPreview();
          break;
      }
    }

    private void requestSummary() {
      UriBuilder uriBuilder = UriBuilder.create();
      appendTable(uriBuilder);
      uriBuilder.segment("variable", "_transient", "summary");

      appendVariableSummaryArguments(uriBuilder);

      ResourceRequestBuilder<SummaryStatisticsDto> requestBuilder = requestSummaryBuilder(uriBuilder.build());

      summaryTabPresenter.setRequestBuilder(requestBuilder, originalTable.getValueSetCount());
      summaryTabPresenter.forgetSummary();
      summaryTabPresenter.onReset();
    }

    private ResourceRequestBuilder<SummaryStatisticsDto> requestSummaryBuilder(String link) {
      String script = VariableDtos.getScript(originalVariable);
      ResourceRequestBuilder<SummaryStatisticsDto> requestBuilder
          = ResourceRequestBuilderFactory.<SummaryStatisticsDto>newBuilder() //
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
          if(response.getStatusCode() == SC_OK) {
            scriptEvaluationCallback.onSuccess(originalVariable);
          } else {
            scriptEvaluationCallback.onFailure(originalVariable);
          }
        }
      };

      requestBuilder.withCallback(callback, SC_OK, SC_BAD_REQUEST, SC_FORBIDDEN, SC_INTERNAL_SERVER_ERROR);
      return requestBuilder;
    }

    private void appendVariableSummaryArguments(UriBuilder uriBuilder) {
      uriBuilder.query("name", originalVariable.getName());
      appendVariableLimitArguments(uriBuilder);

      if(ValueType.TEXT.is(originalVariable.getValueType()) && VariableDtos.allCategoriesMissing(originalVariable)) {
        uriBuilder.query("nature", "categorical", "distinct", "true");
      }
      uriBuilder.query("limit", "500");
    }

    private void scriptInterpretationFail(Response response) {
      summaryTabPresenter.hideSummaryPreview();
      ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
      if(errorDto.getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors) != null) {
        List<JavaScriptErrorDto> errors = extractJavaScriptErrors(errorDto);
        StringBuilder messageBuilder = new StringBuilder();
        for(JavaScriptErrorDto error : errors) {
          messageBuilder
              .append(translationMessages.errorAt(error.getLineNumber(), error.getColumnNumber(), error.getMessage()));
        }
        fireEvent(new ScriptEvaluationFailedEvent(messageBuilder.toString()));
      }
    }

    @SuppressWarnings("unchecked")
    private List<JavaScriptErrorDto> extractJavaScriptErrors(ClientErrorDto errorDto) {
      List<JavaScriptErrorDto> javaScriptErrors = new ArrayList<>();

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
