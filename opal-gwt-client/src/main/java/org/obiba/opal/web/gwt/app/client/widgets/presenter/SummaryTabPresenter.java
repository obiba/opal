/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.widgets.event.SummaryReceivedEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.SummaryRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

/**
 *
 */
public class SummaryTabPresenter extends WidgetPresenter<SummaryTabPresenter.Display> {

  public interface Display extends WidgetDisplay {

    void requestingSummary();

    void renderSummary(SummaryStatisticsDto summary);

  }

  private SummaryStatisticsDto summary;

  private Request summaryRequest;

  private ResourceRequestBuilder<SummaryStatisticsDto> resourceRequestBuilder;

  @Inject
  public SummaryTabPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    registerHandler(eventBus.addHandler(SummaryRequiredEvent.getType(), new DeferredSummaryRequestHandler()));
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
    if(hasSummaryOrPendingRequest() == false) {
      requestSummary(resourceRequestBuilder);
    }
  }

  @Override
  public void revealDisplay() {
  }

  public void forgetSummary() {
    cancelPendingSummaryRequest();
    summary = null;
  }

  public void setResourceUri(String resourceUri) {
    cancelPendingSummaryRequest();
    resourceRequestBuilder = ResourceRequestBuilderFactory.<SummaryStatisticsDto> newBuilder().forResource(resourceUri).get();
  }

  public void setRequestBuilder(ResourceRequestBuilder<SummaryStatisticsDto> resourceRequestBuilder) {
    this.resourceRequestBuilder = resourceRequestBuilder;
  }

  private void requestSummary(final ResourceRequestBuilder<SummaryStatisticsDto> resourceRequestBuilder) {
    getDisplay().requestingSummary();
    summaryRequest = resourceRequestBuilder.withCallback(new ResourceCallback<SummaryStatisticsDto>() {
      @Override
      public void onResource(Response response, SummaryStatisticsDto resource) {
        if(SummaryTabPresenter.this.resourceRequestBuilder == resourceRequestBuilder) {
          summary = resource;
          getDisplay().renderSummary(resource);
          eventBus.fireEvent(new SummaryReceivedEvent(resourceRequestBuilder.getResource(), resource));
        }
      }
    }).send();
  }

  private void cancelPendingSummaryRequest() {
    summary = null;
    if(summaryRequest != null && summaryRequest.isPending()) {
      summaryRequest.cancel();
      summaryRequest = null;
    }
  }

  private boolean hasSummaryOrPendingRequest() {
    return summary != null || (summaryRequest != null && summaryRequest.isPending());
  }

  class DeferredSummaryRequestHandler implements SummaryRequiredEvent.Handler {

    @Override
    public void onSummaryRequest(SummaryRequiredEvent event) {
      cancelPendingSummaryRequest();
      setResourceUri(event.getResourceUri());
    }

  }

}
