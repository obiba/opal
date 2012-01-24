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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ValueColumn.ValueSelectionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.util.Variables;
import org.obiba.opal.web.gwt.app.client.wizard.derive.util.Variables.ValueType;
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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.inject.Inject;

/**
 *
 */
public class ScriptEvaluationPresenter extends WidgetPresenter<ScriptEvaluationPresenter.Display> {

  private static Translations translations = GWT.create(Translations.class);

  public static final int PAGE_SIZE = 20;

  @Inject
  private SummaryTabPresenter summaryTabPresenter;

  private VariableDto variable;

  private TableDto table;

  private int currentOffset;

  private ScriptEvaluationCallback scriptEvaluationCallback;

  //
  // Constructors
  //

  @Inject
  public ScriptEvaluationPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);

    getDisplay().setValueSelectionHandler(new ValueSelectionHandler() {

      @Override
      public void onValueSelection(int row, int column, ValueSetDto valueSet) {
        StringBuilder link = new StringBuilder(valueSet.getValuesArray().get(column).getLink());
        link.append("?");
        appendVariableLimitArguments(link);
        // TODO won't work with long script
        // OPAL-1346 encode script
        link.append("&script=" + URL.encodePathSegment(Variables.getScript(variable)));
        eventBus.fireEvent(new FileDownloadEvent(link.toString()));
      }
    });

  }

  public void setTable(TableDto table) {
    this.table = table;
    getDisplay().setEntityType(table.getEntityType());
  }

  /**
   * Set the variable to be evaluated. Value type and script are extracted from the variable dto.
   * @param variable
   */
  public void setVariable(VariableDto variable) {
    this.variable = variable;
    getDisplay().setVariable(variable);
  }

  public void setScriptEvaluationCallback(ScriptEvaluationCallback scriptEvaluationCallback) {
    this.scriptEvaluationCallback = scriptEvaluationCallback;
  }

  private void populateValues(final int offset) {
    String script = Variables.getScript(variable);

    getDisplay().populateValues(null);
    currentOffset = offset;

    StringBuilder link = new StringBuilder();
    appendTable(link);
    link.append("/variable/_transient/valueSets?limit=").append(PAGE_SIZE)//
    .append("&offset=").append(offset).append("&");
    appendVariableLimitArguments(link);

    ValuesRequestCallback callback = new ValuesRequestCallback(offset);

    ResourceRequestBuilder<ValueSetsDto> requestBuilder = ResourceRequestBuilderFactory.<ValueSetsDto> newBuilder() //
    .forResource(link.toString()).post().withFormBody("script", script) //
    .withCallback(200, callback).withCallback(400, callback).withCallback(500, callback)//
    .accept("application/x-protobuf+json");
    requestBuilder.send();
  }

  private void requestSummary() {
    String script = Variables.getScript(variable);
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

    if(ValueType.TEXT.is(variable.getValueType()) && Variables.allCategoriesMissing(variable)) {
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
  public void refreshDisplay() {
    requestSummary();
    populateValues(0);
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  protected void onBind() {
    summaryTabPresenter.bind();
    getDisplay().setSummaryTabWidget(summaryTabPresenter.getDisplay());
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    summaryTabPresenter.unbind();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addNextPageClickHandler(new NextPageClickHandler()));
    super.registerHandler(getDisplay().addPreviousPageClickHandler(new PreviousPageClickHandler()));
  }

  //
  // Inner classes and Interfaces
  //

  private final class ValuesRequestCallback implements ResponseCodeCallback {
    private final int offset;

    private ValuesRequestCallback(int offset) {
      this.offset = offset;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onResponseCode(Request request, Response response) {
      boolean success = false;
      switch(response.getStatusCode()) {
      case Response.SC_OK:
        updateValuesDisplay((ValueSetsDto) JsonUtils.unsafeEval(response.getText()));
        success = true;
        break;
      case Response.SC_BAD_REQUEST:
        scriptInterpretationFail(response);
        break;
      default:
        eventBus.fireEvent(NotificationEvent.newBuilder().error(translations.scriptEvaluationFailed()).build());
        break;
      }
      if(scriptEvaluationCallback != null) {
        if(success) scriptEvaluationCallback.onSuccess(variable);
        else
          scriptEvaluationCallback.onFailure(variable);
      }
    }

    private void updateValuesDisplay(ValueSetsDto resource) {
      updateValuesDisplay(resource.getValueSetsArray());
    }

    private void updateValuesDisplay(JsArray<ValueSetsDto.ValueSetDto> resource) {
      int high = offset + PAGE_SIZE;
      if(resource != null && resource.length() < high) {
        high = offset + resource.length();
      }
      getDisplay().setPageLimits(offset + 1, high, table.getValueSetCount());
      getDisplay().populateValues(resource);
    }

    private void scriptInterpretationFail(Response response) {
      ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
      if(errorDto.getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors) != null) {
        List<JavaScriptErrorDto> errors = extractJavaScriptErrors(errorDto);
        for(JavaScriptErrorDto error : errors) {
          // TODO translate
          eventBus.fireEvent(NotificationEvent.newBuilder().error("Error at line " + error.getLineNumber() + ", column " + error.getColumnNumber() + ": " + error.getMessage()).build());
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
      if(currentOffset + PAGE_SIZE < table.getValueSetCount()) {
        populateValues(currentOffset + PAGE_SIZE);
      }
    }

  }

  public interface ScriptEvaluationCallback {
    public void onSuccess(VariableDto variable);

    public void onFailure(VariableDto variable);
  }

  public interface Display extends WidgetDisplay {

    void setSummaryTabWidget(WidgetDisplay widget);

    void setVariable(VariableDto variable);

    void setEntityType(String entityType);

    HandlerRegistration setValueSelectionHandler(ValueSelectionHandler handler);

    void populateValues(JsArray<ValueSetsDto.ValueSetDto> values);

    HandlerRegistration addNextPageClickHandler(ClickHandler handler);

    HandlerRegistration addPreviousPageClickHandler(ClickHandler handler);

    void setPageLimits(int low, int high, int count);
  }

}
