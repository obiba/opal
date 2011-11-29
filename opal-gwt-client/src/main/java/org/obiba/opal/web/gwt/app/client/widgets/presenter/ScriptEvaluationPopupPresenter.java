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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.widgets.event.ScriptEvaluationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.ValueDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class ScriptEvaluationPopupPresenter extends WidgetPresenter<ScriptEvaluationPopupPresenter.Display> {

  public static final int PAGE_SIZE = 20;

  private String script;

  private int currentOffset;

  @Inject
  public ScriptEvaluationPopupPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  private void populateValues(final int offset) {
    getDisplay().setScript(script);

    currentOffset = offset;
    // TODO adjust table request
    StringBuilder link = new StringBuilder("/datasource/opal-data/table/questionnaire")//
    .append("/variable/_transient/values?limit=").append(PAGE_SIZE)//
    .append("&offset=").append(offset);
    // appendVariableLimitArguments(link);
    ResourceRequestBuilder<JsArray<ValueDto>> requestBuilder = ResourceRequestBuilderFactory.<JsArray<ValueDto>> newBuilder() //
    .forResource(link.toString()).post() //
    .withCallback(new ResourceCallback<JsArray<ValueDto>>() {

      @Override
      public void onResource(Response response, JsArray<ValueDto> resource) {
        int high = offset + PAGE_SIZE;
        if(resource != null && resource.length() < high) {
          high = offset + resource.length();
        }
        // TODO not 50000 but table size
        getDisplay().setPageLimits(offset + 1, high, 50000);
        getDisplay().populateValues(resource);
      }

    });
    requestBuilder.withFormBody("script", script);
    requestBuilder.send();
  }

  @Override
  public void refreshDisplay() {

  }

  @Override
  public void revealDisplay() {
    display.getButton().addClickHandler(new CloseHandler());
    display.setScript(script);
    populateValues(0);
    display.showDialog();
  }

  class CloseHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      display.closeDialog();
    }
  }

  @Override
  protected void onBind() {
    addHandler();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  private void addHandler() {
    super.registerHandler(eventBus.addHandler(ScriptEvaluationEvent.getType(), new EvaluationHandler()));
    super.registerHandler(getDisplay().addNextPageClickHandler(new NextPageClickHandler()));
    super.registerHandler(getDisplay().addPreviousPageClickHandler(new PreviousPageClickHandler()));
  }

  class EvaluationHandler implements ScriptEvaluationEvent.Handler {

    @Override
    public void onScriptEvaluation(ScriptEvaluationEvent scriptEvaluationEvent) {
      script = scriptEvaluationEvent.getScript();
      revealDisplay();
    }
  }

  public class PreviousPageClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(currentOffset > 0) {
        populateValues(currentOffset - PAGE_SIZE);
      }
    }

  }

  public class NextPageClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      // if(currentOffset + PAGE_SIZE < table.getValueSetCount()) {
      populateValues(currentOffset + PAGE_SIZE);
      // }
    }

  }

  public interface Display extends WidgetDisplay {

    void showDialog();

    void setScript(String script);

    HasClickHandlers getButton();

    void closeDialog();

    void populateValues(JsArray<ValueDto> values);

    HandlerRegistration addNextPageClickHandler(ClickHandler handler);

    HandlerRegistration addPreviousPageClickHandler(ClickHandler handler);

    void setPageLimits(int low, int high, int count);

  }
}
