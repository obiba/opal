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
import org.obiba.opal.web.gwt.app.client.support.JSErrorNotificationEventBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class SummaryTabPresenter extends PresenterWidget<SummaryTabPresenter.Display> {

  private final static int DEFAULT_LIMIT = 500;

  private final static int MIN_LIMIT = 10;

  private SummaryStatisticsDto summary;

  private Request summaryRequest;

  private ResourceRequestBuilder<SummaryStatisticsDto> resourceRequestBuilder;

  private int limit = DEFAULT_LIMIT;

  private int entitiesCount;

  @Inject
  public SummaryTabPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    registerHandler(getEventBus().addHandler(SummaryRequiredEvent.getType(), new DeferredSummaryRequestHandler()));

    // Summary: Get full summary
    registerHandler(getView().getFullSummary().addClickHandler(new FullSummaryHandler()));

    // Summary: Cancel summary
    registerHandler(getView().getCancelSummary().addClickHandler(new CancelSummaryHandler()));

    // Summary: Refresh summary
    registerHandler(getView().getRefreshSummary().addClickHandler(new RefreshSummaryHandler()));
  }

  @Override
  public void onReset() {
    if(!hasSummaryOrPendingRequest()) {
      requestSummary();
    }
  }

  public void forgetSummary() {
    cancelPendingSummaryRequest();
    summary = null;
  }

  public void hideSummaryPreview() {
    getView().hideSummaryPreview();
  }

  public void setResourceUri(String resourceUri, int entitiesCount) {
    cancelPendingSummaryRequest();

    this.entitiesCount = entitiesCount;
    UriBuilder uriBuilder = UriBuilder.create().fromPath(resourceUri);
    if(limit < entitiesCount) {
      uriBuilder.query("limit", String.valueOf(limit));
    }
    resourceRequestBuilder = ResourceRequestBuilderFactory.<SummaryStatisticsDto>newBuilder()
        .forResource(uriBuilder.build()).get();

    limit = Math.min(entitiesCount, limit);
  }

  public void setRequestBuilder(ResourceRequestBuilder<SummaryStatisticsDto> resourceRequestBuilder) {
    this.resourceRequestBuilder = resourceRequestBuilder;
  }

  private void requestSummary() {
    getView().requestingSummary(limit, entitiesCount);
    summaryRequest = resourceRequestBuilder //
        .withCallback(new ResourceCallback<SummaryStatisticsDto>() {
          @Override
          public void onResource(Response response, SummaryStatisticsDto resource) {
            summary = resource;
            getView().renderSummary(resource);
            getView().renderSummaryLimit(limit, entitiesCount);
            getEventBus().fireEvent(new SummaryReceivedEvent(resourceRequestBuilder.getResource(), resource));
          }
        }) //
        .withCallback(Response.SC_NOT_FOUND, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().renderNoSummary();
          }
        }) //
        .withCallback(Response.SC_BAD_REQUEST, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
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

  @SuppressWarnings("ParameterHidesMemberVariable")
  public interface Display extends View {

    void requestingSummary(int limit, int entitiesCount);

    void renderSummary(SummaryStatisticsDto summary);

    void renderNoSummary();

    void renderSummaryLimit(int limit, int entitiesCount);

    void renderCancelSummaryLimit(int limit, int entitiesCount);

    HasClickHandlers getFullSummary();

    HasClickHandlers getCancelSummary();

    HasClickHandlers getRefreshSummary();

    Number getLimit();

    void hideSummaryPreview();
  }

  class DeferredSummaryRequestHandler implements SummaryRequiredEvent.Handler {

    @Override
    public void onSummaryRequest(SummaryRequiredEvent event) {
      cancelPendingSummaryRequest();
      setResourceUri(event.getResourceUri(), event.getMax() == null ? DEFAULT_LIMIT : event.getMax());
    }

  }

  private final class FullSummaryHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      cancelPendingSummaryRequest();
      // Remove queries from the url
      String uri = resourceRequestBuilder.getResource();
      if(uri.indexOf("?") > 0) {
        uri = uri.substring(0, uri.indexOf("?"));
      }
      resourceRequestBuilder.forResource(uri).get();
      limit = entitiesCount;
      onReset();
    }
  }

  private final class CancelSummaryHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      cancelPendingSummaryRequest();
      getView().renderCancelSummaryLimit(limit < entitiesCount ? limit : Math.min(DEFAULT_LIMIT, entitiesCount),
          entitiesCount);

      // If canceling from a full summary, automatically fetch for limit, else, do nothing
      if(!resourceRequestBuilder.getResource().contains("limit")) {
        refreshSummary();
      }
    }
  }

  private final class RefreshSummaryHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      cancelPendingSummaryRequest();
      refreshSummary();
    }
  }

  private void refreshSummary() {
    limit = getView().getLimit().intValue();
    if(limit < Math.min(MIN_LIMIT, entitiesCount)) {
      limit = Math.min(MIN_LIMIT, entitiesCount);
    }
    String uri = resourceRequestBuilder.getResource();
    uri = uri.substring(0, uri.indexOf("?") > 0 ? uri.indexOf("?") : uri.length());
    resourceRequestBuilder.forResource(limit >= entitiesCount ? uri : uri + "?limit=" + limit).get();

    onReset();
  }
}
