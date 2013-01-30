/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.navigator.presenter;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ValueSequencePopupPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.JavaScriptErrorDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ValuesTablePresenter extends PresenterWidget<ValuesTablePresenter.Display> {

  private TableDto table;

  private DataFetcher fetcher;

  private ValueSequencePopupPresenter valueSequencePopupPresenter;

  @Inject
  public ValuesTablePresenter(Display display, final EventBus eventBus,
      ValueSequencePopupPresenter valueSequencePopupPresenter) {
    super(eventBus, display);
    this.valueSequencePopupPresenter = valueSequencePopupPresenter;
  }

  public void setTable(TableDto table) {
    setTable(table, "");
  }

  public void setTable(TableDto table, VariableDto variable) {
    hideValueSequencePopup(table);
    this.table = table;

    getView().setTable(table);
    JsArray<VariableDto> variables = JsArray.createArray().cast();
    variables.push(variable);
    getView().setVariables(variables);
  }

  public void setTable(TableDto table, String select) {
    hideValueSequencePopup(table);
    this.table = table;

    getView().setTable(table);
    fetcher.updateVariables(select);
  }

  @Override
  protected void onBind() {
    super.onBind();
    getView().setValueSetsFetcher(fetcher = new DataFetcherImpl());
  }

  //
  // Private methods
  //

  /**
   * Hide value sequence popup if table is about to be changed.
   */
  private void hideValueSequencePopup(TableDto newTable) {
    if(table != null && table.getName().equals(newTable.getName()) == false) {
      valueSequencePopupPresenter.getView().hide();
    }
  }

  private String cleanFilter(String filter) {
    return filter.replaceAll("/", "\\\\/");
  }

  //
  // Inner classes and interfaces
  //

  private class VariablesResourceCallback implements ResourceCallback<JsArray<VariableDto>> {

    private TableDto table;

    VariablesResourceCallback(TableDto table) {
      this.table = table;
    }

    @Override
    public void onResource(Response response, JsArray<VariableDto> resource) {
      if(table.getLink().equals(ValuesTablePresenter.this.table.getLink())) {
        JsArray<VariableDto> variables = resource != null ? resource : JsArray.createArray()
            .<JsArray<VariableDto>>cast();
        getView().setVariables(variables);
      }
    }
  }

  private class ValueSetsResourceCallback implements ResourceCallback<ValueSetsDto> {

    private int offset;

    private TableDto table;

    ValueSetsResourceCallback(int offset, TableDto table) {
      this.offset = offset;
      this.table = table;
    }

    @Override
    public void onResource(Response response, ValueSetsDto resource) {
      if(table.getLink().equals(ValuesTablePresenter.this.table.getLink())) {
        if(getView().getValueSetsProvider() != null) {
          getView().getValueSetsProvider().populateValues(offset, resource);
        }
      }
    }
  }

  private class BadRequestCallback implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      notifyError(response);
    }

    @SuppressWarnings("unchecked")
    protected void notifyError(Response response) {
      ClientErrorDto error = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());

      if(error.getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors) != null) {
        JsArray<JavaScriptErrorDto> errors = (JsArray<JavaScriptErrorDto>) error
            .getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors);

        NotificationEvent notificationEvent = NotificationEvent.Builder.newNotification().error("JavascriptError")
            .args(errors.get(0).getSourceName(), //
                errors.get(0).getMessage(), //
                String.valueOf(errors.get(0).getLineNumber()),//
                String.valueOf(errors.get(0).getColumnNumber())).build();

        getEventBus().fireEvent(notificationEvent);
      } else {
        getEventBus().fireEvent(
            NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray())
                .build());
      }
    }
  }

  private class DataFetcherImpl implements DataFetcher {

    private Request variablesRequest = null;

    private Request valuesRequest = null;

    @Override
    public void request(List<VariableDto> variables, int offset, int limit) {
      StringBuilder link = getLinkBuilder(offset, limit);
      if(table.getVariableCount() > variables.size()) {
        link.append("&select=");
        StringBuilder script = new StringBuilder();
        script.append("name().matches(/");
        for(int i = 0; i < variables.size(); i++) {
          if(i > 0) {
            script.append("|");
          }
          script.append("^").append(escape(variables.get(i).getName())).append("$");
        }
        script.append("/)");
        link.append(URL.encodePathSegment(script.toString()));
      }
      doRequest(offset, link.toString());
    }

    @Override
    public void request(String filter, int offset, int limit) {
      StringBuilder link = getLinkBuilder(offset, limit);
      if(filter != null && filter.isEmpty() == false) {
        link.append("&select=").append(URL.encodePathSegment("name().matches(/" + cleanFilter(filter) + "/)"));
      }

      doRequest(offset, link.toString());
    }

    private String escape(String filter) {
      return filter.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");
    }

    private void doRequest(int offset, String link) {
      if(valuesRequest != null) {
        valuesRequest.cancel();
        valuesRequest = null;
      }

      valuesRequest = ResourceRequestBuilderFactory.<ValueSetsDto>newBuilder().forResource(link).get()//
          .withCallback(new ValueSetsResourceCallback(offset, table))
          .withCallback(Response.SC_BAD_REQUEST, new BadRequestCallback()).send();
    }

    private StringBuilder getLinkBuilder(int offset, int limit) {
      return new StringBuilder(table.getLink()).append("/valueSets").append("?offset=").append(offset).append("&limit=")
          .append(limit);
    }

    @Override
    public void requestBinaryValue(VariableDto variable, String entityIdentifier) {
      StringBuilder link = new StringBuilder(table.getLink());
      link.append("/valueSet/").append(entityIdentifier).append("/variable/").append(variable.getName())
          .append("/value");
      getEventBus().fireEvent(new FileDownloadEvent(link.toString()));
    }

    @Override
    public void requestValueSequence(VariableDto variable, String entityIdentifier) {
      valueSequencePopupPresenter.initialize(table, variable, entityIdentifier);
      addToPopupSlot(valueSequencePopupPresenter);
    }

    @Override
    public void updateVariables(String select) {
      String link = table.getLink() + "/variables";
      if(select != null && select.isEmpty() == false) {
        link += "?script=" + URL.encodePathSegment("name().matches(/" + cleanFilter(select) + "/)");
      }
      if(variablesRequest != null) {
        variablesRequest.cancel();
        variablesRequest = null;
      }
      //noinspection MagicNumber
      variablesRequest = ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder().forResource(link).get()//
          .withCallback(new VariablesResourceCallback(table)).withCallback(400, new BadRequestCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              notifyError(response);
              setTable(table);
            }
          }).send();
    }
  }

  public interface Display extends View {
    void setTable(TableDto table);

    void setVariables(JsArray<VariableDto> variables);

    ValueSetsProvider getValueSetsProvider();

    void setValueSetsFetcher(DataFetcher fetcher);
  }

  public interface DataFetcher {
    void request(List<VariableDto> variables, int offset, int limit);

    void request(String filter, int offset, int limit);

    void requestBinaryValue(VariableDto variable, String entityIdentifier);

    void requestValueSequence(VariableDto variable, String entityIdentifier);

    void updateVariables(String select);
  }

  public interface ValueSetsProvider {
    void populateValues(int offset, ValueSetsDto valueSets);
  }

}
