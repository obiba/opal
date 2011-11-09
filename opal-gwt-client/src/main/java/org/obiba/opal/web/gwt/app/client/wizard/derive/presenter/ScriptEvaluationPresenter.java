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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.inject.Inject;

/**
 *
 */
public class ScriptEvaluationPresenter extends WidgetPresenter<ScriptEvaluationPresenter.Display> {

  public static final int PAGE_SIZE = 20;

  @Inject
  private SummaryTabPresenter summaryTabPresenter;

  private VariableDto variable;

  private String script;

  private String valueType;

  private TableDto table;

  private int currentOffset;

  private boolean repeatable;

  //
  // Constructors
  //

  @Inject
  public ScriptEvaluationPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  public void setTable(TableDto table) {
    this.table = table;
  }

  /**
   * Set the variable to be evaluated. Value type and script are extracted from the variable dto.
   * @param variable
   */
  public void setVariable(VariableDto variable) {
    this.variable = variable;
    this.valueType = variable.getValueType();
    this.repeatable = variable.getIsRepeatable();
    this.script = getScript(variable);
  }

  /**
   * Set script and value type manually.
   * @param valueType
   * @param script
   */
  public void setScript(String valueType, String script, boolean repeatable) {
    this.variable = null;
    this.valueType = valueType;
    this.repeatable = repeatable;
    this.script = script;
  }

  private String getScript(VariableDto derived) {
    AttributeDto scriptAttr = null;
    for(AttributeDto attr : JsArrays.toIterable(JsArrays.toSafeArray(derived.getAttributesArray()))) {
      if(attr.getName().equals("script")) {
        scriptAttr = attr;
        break;
      }
    }
    return scriptAttr != null ? scriptAttr.getValue() : "null";
  }

  private void populateValues(final int offset) {
    getDisplay().setScript(script);

    currentOffset = offset;
    StringBuilder link = new StringBuilder(table.getLink())//
    .append("/variable/_transient/values?limit=").append(PAGE_SIZE)//
    .append("&offset=").append(offset).append("&");
    appendVariableArguments(link);
    ResourceRequestBuilderFactory.<JsArray<ValueDto>> newBuilder() //
    .forResource(link.toString()).get() //
    .withCallback(new ResourceCallback<JsArray<ValueDto>>() {

      @Override
      public void onResource(Response response, JsArray<ValueDto> resource) {
        int high = offset + PAGE_SIZE;
        if(resource != null && resource.length() < high) {
          high = offset + resource.length();
        }
        getDisplay().setPageLimits(offset + 1, high, table.getValueSetCount());
        getDisplay().populateValues(resource);
      }

    })//
    .send();
  }

  private void requestSummary() {
    StringBuilder link = new StringBuilder(table.getLink()).append("/variable/_transient/summary?");
    appendVariableArguments(link);

    if(variable != null) {
      JsArray<CategoryDto> cats = variable.getCategoriesArray();
      if(cats != null) {
        for(int i = 0; i < cats.length(); i++) {
          link.append("&category=" + URL.encodeQueryString(cats.get(i).getName()));
        }
      }
    }
    summaryTabPresenter.setResourceUri(link.toString());
    summaryTabPresenter.refreshDisplay();
  }

  private void appendVariableArguments(StringBuilder link) {
    link.append("valueType=" + valueType) //
    .append("&repeatable=" + repeatable) //
    .append("&script=" + URL.encodeQueryString(script));
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

  public interface Display extends WidgetDisplay {

    void setSummaryTabWidget(WidgetDisplay widget);

    void populateValues(JsArray<ValueDto> values);

    void setScript(String text);

    HandlerRegistration addNextPageClickHandler(ClickHandler handler);

    HandlerRegistration addPreviousPageClickHandler(ClickHandler handler);

    void setPageLimits(int low, int high, int count);
  }

}
