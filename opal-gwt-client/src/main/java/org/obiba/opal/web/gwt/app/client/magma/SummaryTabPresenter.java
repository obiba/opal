/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.event.SummaryReceivedEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.SummaryRequiredEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableRefreshEvent;
import org.obiba.opal.web.gwt.app.client.support.JSErrorNotificationEventBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import static org.obiba.opal.web.gwt.rest.client.UriUtils.removeQueryParam;
import static org.obiba.opal.web.gwt.rest.client.UriUtils.updateQueryParams;

/**
 *
 */
public class SummaryTabPresenter extends PresenterWidget<SummaryTabPresenter.Display> implements SummaryTabUiHandlers {

  public final static int DEFAULT_LIMIT = 50;

  private final static int MIN_LIMIT = 10;

  private SummaryStatisticsDto summary;

  private Request summaryRequest;

  private ResourceRequestBuilder<SummaryStatisticsDto> resourceRequestBuilder;

  private int limit = DEFAULT_LIMIT;

  private int entitiesCount;

  private HandlerRegistration handlerRegistration;

  private VariableDto variableDto;

  private String variable;

  private String datasource;

  private String table;

  private final Translations translations;

  private ClientErrorDto latestClientError; //latest client error received

  private int currentErrorCount; //number of consecutive client errors that were equal to each other

  @Inject
  public SummaryTabPresenter(EventBus eventBus, Display display, Translations translations) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    getView().setLimit(DEFAULT_LIMIT);
    this.translations = translations;
  }

  @Override
  protected void onReveal() {
    handlerRegistration = getEventBus().addHandler(SummaryRequiredEvent.getType(), new DeferredSummaryRequestHandler());
    registerHandler(handlerRegistration);

    addRegisteredHandler(VariableRefreshEvent.getType(), new VariableRefreshEvent.Handler() {
      @Override
      public void onVariableRefresh(VariableRefreshEvent event) {
        requestSummary();
      }
    });
  }

  @Override
  protected void onHide() {
    super.onHide();
    handlerRegistration.removeHandler();
  }

  @Override
  public void onReset() {
    if(!hasSummaryOrPendingRequest()) {
      requestSummary();
    }
  }

  @Override
  public void onFullSummary() {
    getView().setLimit(entitiesCount);
    cancelPendingSummaryRequest();
    String uri = resourceRequestBuilder.getResource();
    uri = removeQueryParam(uri, "limit");
    Map<String, String> args = Maps.newHashMap();
    args.put("resetCache", "true");
    uri = updateQueryParams(uri, args);

    if(uri.contains("_transient")) {
      //for script evaluation, we must use post so the script/categories are passed in the form, otherwise the summary is incorrect
      resourceRequestBuilder.forResource(uri).post();
    } else {
      resourceRequestBuilder.forResource(uri).get();
    }

    limit = entitiesCount;
    onReset();
  }

  @Override
  public void onCancelSummary() {
    cancelPendingSummaryRequest();
    getView().renderCancelSummaryLimit(limit < entitiesCount ? limit : Math.min(DEFAULT_LIMIT, entitiesCount),
        entitiesCount);
  }

  @Override
  public void onRefreshSummary() {
    cancelPendingSummaryRequest();
    String uri = resourceRequestBuilder.getResource();
    uri = removeQueryParam(uri, "limit");

    Map<String, String> args = Maps.newHashMap();
    args.put("resetCache", "true");
    limit = Math.max(getView().getLimit().intValue(), Math.min(MIN_LIMIT, entitiesCount));

    if(limit < entitiesCount) {
      args.put("limit", String.valueOf(limit));
    }

    String target = updateQueryParams(uri, args);
    resourceRequestBuilder.forResource(target);
    if(target.contains("_transient")) {
      //for script evaluation, we must use post so the script/categories are passed in the form, otherwise the summary is incorrect
      resourceRequestBuilder.post();
    } else {
      resourceRequestBuilder.get();
    }

    onReset();
  }

  public void forgetSummary() {
    cancelPendingSummaryRequest();
    summary = null;
  }

  public void hideSummaryPreview() {
    getView().hideSummaryPreview();
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public void configureSummaryRequest(UriBuilder uriBuilder, int entitiesCount, String datasource, String table,
      String variable) {
    cancelPendingSummaryRequest();

    this.entitiesCount = entitiesCount;
    if(limit < entitiesCount) {
      uriBuilder.query("limit", String.valueOf(limit));
    }

    resourceRequestBuilder = ResourceRequestBuilderFactory.<SummaryStatisticsDto>newBuilder()
        .forResource(uriBuilder.build(datasource, table, variable)).get();

    limit = Math.min(entitiesCount, limit);
  }

  public void setRequestBuilder(ResourceRequestBuilder<SummaryStatisticsDto> resourceRequestBuilder,
      int entitiesCount) {
    this.resourceRequestBuilder = resourceRequestBuilder;
    this.entitiesCount = entitiesCount;

    limit = Math.min(entitiesCount, limit);
  }

  private void requestSummary() {
    if(resourceRequestBuilder == null) return;
    if (blockSummaryRequests()) return;

    getView().requestingSummary(limit, entitiesCount);

    summaryRequest = resourceRequestBuilder //
        .withCallback(new ResourceCallback<SummaryStatisticsDto>() {
          @Override
          public void onResource(Response response, SummaryStatisticsDto dto) {
            summary = dto;
            getView().renderSummary(dto, variableDto);
            getView().renderSummaryLimit(dto.hasLimit() ? dto.getLimit() : entitiesCount, entitiesCount);
            getEventBus().fireEvent(new SummaryReceivedEvent(resourceRequestBuilder.getResource(), dto));
          }
        })//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().renderNoSummary();
            ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
            checkMessageFlooding(error);
            NotificationEvent event;
            if (blockSummaryRequests()) {
              onCancelSummary();
              event = NotificationEvent.newBuilder().error(translations.tooManyRepeatedErrorsLabel()).build();
            } else {
              event = new JSErrorNotificationEventBuilder().build(error);
            }
            getEventBus().fireEvent(event);
          }
        }, Response.SC_BAD_REQUEST, Response.SC_NOT_FOUND, Response.SC_FORBIDDEN, Response.SC_INTERNAL_SERVER_ERROR)//
        .send();
  }

  /**
   * @return true if further summary requests should be blocked
   */
  private boolean blockSummaryRequests() {
    return currentErrorCount >= 3;
  }

  /**
   * Checks the given error for message flooding
   * @param error client error to check
   */
  private void checkMessageFlooding(ClientErrorDto error) {
    boolean match = false;

    if (latestClientError != null && latestClientError.getStatus().equals(error.getStatus())) {
      JsArrayString array1 = latestClientError.getArgumentsArray();
      JsArrayString array2 = error.getArgumentsArray();
      match = array1.toString().equals(array2.toString());
    }

    if (match) {
      currentErrorCount++; //one more occurrence of the same error
    } else {
      latestClientError = error;
      currentErrorCount = 0;
    }
  }

  private void cancelPendingSummaryRequest() {
    summary = null;
    if(summaryRequest != null && summaryRequest.isPending()) {
      summaryRequest.cancel();
      summaryRequest = null;
    }
  }

  private boolean hasSummaryOrPendingRequest() {
    return summary != null || summaryRequest != null && summaryRequest.isPending();
  }

  public void setLimit(int limit) {
    this.limit = limit;
    getView().setLimit(limit);
  }

  public void initialize(VariableDto variableDto) {
    // Reset limit to the default limit only if it was the full summary
    if(limit == entitiesCount) {
      limit = DEFAULT_LIMIT;
    }

    //resetting the message flooding protection fields
    latestClientError = null;
    currentErrorCount = 0;
    this.variableDto = variableDto;
    onReset();
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public interface Display extends View, HasUiHandlers<SummaryTabUiHandlers> {

    void requestingSummary(int limit, int entitiesCount);

    /**
     * Summary statistics and optionally the corresponding variable.
     *
     * @param summary
     * @param variableDto may be null
     */
    void renderSummary(SummaryStatisticsDto summary, VariableDto variableDto);

    void renderNoSummary();

    void renderSummaryLimit(int limit, int entitiesCount);

    void renderCancelSummaryLimit(int limit, int entitiesCount);

    Number getLimit();

    void setLimit(int limit);

    void hideSummaryPreview();
  }

  class DeferredSummaryRequestHandler implements SummaryRequiredEvent.Handler {

    @Override
    public void onSummaryRequest(SummaryRequiredEvent event) {
      datasource = event.getDatasource();
      table = event.getTable();
      variable = event.getVariable();

      getView().setLimit(DEFAULT_LIMIT);
      UriBuilder resourceUri = event.getResourceUri();
      resourceUri.query("fullIfCached", "true");
      configureSummaryRequest(resourceUri, event.getMax(), datasource, table, variable);
      requestSummary();
    }

  }

}