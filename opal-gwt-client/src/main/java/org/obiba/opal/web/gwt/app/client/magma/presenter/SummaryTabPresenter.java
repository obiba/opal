/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.SummaryReceivedEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.SummaryRequiredEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableRefreshEvent;
import org.obiba.opal.web.gwt.app.client.support.JSErrorNotificationEventBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class SummaryTabPresenter extends PresenterWidget<SummaryTabPresenter.Display> implements SummaryTabUiHandlers {

  public final static int DEFAULT_LIMIT = 500;

  private final static int MIN_LIMIT = 10;

  private SummaryStatisticsDto summary;

  private Request summaryRequest;

  private ResourceRequestBuilder<SummaryStatisticsDto> resourceRequestBuilder;

  private int limit = DEFAULT_LIMIT;

  private int entitiesCount;

  private HandlerRegistration handlerRegistration;

  @Inject
  public SummaryTabPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
    getView().setUiHandlers(this);
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
    // Remove queries from the url
    String uri = resourceRequestBuilder.getResource();
    if(uri.contains("?")) {
      uri = uri.substring(0, uri.indexOf("?"));
    }
    resourceRequestBuilder.forResource(uri).get();
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
    // Remove queries from the url
    if(uri.contains("?")) {
      uri = uri.substring(0, uri.indexOf("?"));
    }

    UriBuilder uriBuilder = UriBuilder.create().fromPath(URL.decodeQueryString(uri));
    uriBuilder.query("resetCache", "true");

    limit = Math.max(getView().getLimit().intValue(), Math.min(MIN_LIMIT, entitiesCount));
    if(limit < entitiesCount) {
      uriBuilder.query("limit", String.valueOf(limit));
    }
    resourceRequestBuilder.forResource(uriBuilder.build()).get();

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
  public void configureSummaryRequest(UriBuilder uriBuilder, int entitiesCount, String... args) {
    cancelPendingSummaryRequest();

    this.entitiesCount = entitiesCount;
    if(limit < entitiesCount) {
      uriBuilder.query("limit", String.valueOf(limit));
    }

    resourceRequestBuilder = ResourceRequestBuilderFactory.<SummaryStatisticsDto>newBuilder()
        .forResource(uriBuilder.build(args)).get();

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
    getView().requestingSummary(limit, entitiesCount);
    summaryRequest = resourceRequestBuilder //
        .withCallback(new ResourceCallback<SummaryStatisticsDto>() {
          @Override
          public void onResource(Response response, SummaryStatisticsDto dto) {
            summary = dto;
            getView().renderSummary(dto);
            getView().renderSummaryLimit(dto.hasLimit() ? dto.getLimit() : entitiesCount, entitiesCount);
            getEventBus().fireEvent(new SummaryReceivedEvent(resourceRequestBuilder.getResource(), dto));
          }
        }) //
        .withCallback(Response.SC_BAD_REQUEST, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().renderNoSummary();
            NotificationEvent notificationEvent = new JSErrorNotificationEventBuilder()
                .build((ClientErrorDto) JsonUtils.unsafeEval(response.getText()));
            getEventBus().fireEvent(notificationEvent);
          }
        }) //
        .send();
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

  public void init() {
    // Reset limit to the default limit only if it was the full summary
    if(limit == entitiesCount) {
      limit = DEFAULT_LIMIT;
    }
    onReset();
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public interface Display extends View, HasUiHandlers<SummaryTabUiHandlers> {

    void requestingSummary(int limit, int entitiesCount);

    void renderSummary(SummaryStatisticsDto summary);

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
      getView().setLimit(DEFAULT_LIMIT);
      UriBuilder resourceUri = event.getResourceUri();
      resourceUri.query("fullIfCached", "true");
      configureSummaryRequest(resourceUri, event.getMax(), event.getArgs());
      requestSummary();
    }

  }

}
