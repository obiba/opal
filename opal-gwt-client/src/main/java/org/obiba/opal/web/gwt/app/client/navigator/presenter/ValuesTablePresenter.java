/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.presenter;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ValueSequencePopupPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
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

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;

public class ValuesTablePresenter extends PresenterWidget<ValuesTablePresenter.Display> {

  private TableDto table;

  private DataFetcher fetcher;

  private final ValueSequencePopupPresenter valueSequencePopupPresenter;

  private final EntityDialogPresenter entityDialogPresenter;

  @Inject
  public ValuesTablePresenter(Display display, EventBus eventBus,
      ValueSequencePopupPresenter valueSequencePopupPresenter, EntityDialogPresenter entityDialogPresenter) {
    super(eventBus, display);
    this.valueSequencePopupPresenter = valueSequencePopupPresenter;
    this.entityDialogPresenter = entityDialogPresenter;
  }

  public void setTable(TableDto table) {
    setTable(table, "");
  }

  public void setTable(TableDto table, VariableDto variable) {
    hidePopups(table);
    this.table = table;

    getView().setTable(table);
    JsArray<VariableDto> variables = JsArray.createArray().cast();
    variables.push(variable);
    getView().setVariables(variables);
  }

  public void setTable(TableDto table, String select) {
    hidePopups(table);
    this.table = table;

    getView().setTable(table);
    fetcher.updateVariables(select);
  }

  @Override
  protected void onBind() {
    super.onBind();
    getView().setValueSetsFetcher(fetcher = new DataFetcherImpl());
    getView().addEntitySearchHandler(new EntitySearchHandlerImpl());
  }

  /**
   * Hide entity details & value sequence popup if table is about to be changed.
   */
  private void hidePopups(TableDto newTable) {
    if(table != null && !table.getName().equals(newTable.getName())) {
      valueSequencePopupPresenter.getView().hide();
      entityDialogPresenter.getView().hide();
    }
  }

  public void setViewMode(ViewMode mode) {
    getView().setViewMode(mode);
  }

  private class VariablesResourceCallback implements ResourceCallback<JsArray<VariableDto>> {

    private final TableDto table;

    private VariablesResourceCallback(TableDto table) {
      this.table = table;
    }

    @Override
    public void onResource(Response response, JsArray<VariableDto> resource) {
      if(table.getLink().equals(ValuesTablePresenter.this.table.getLink())) {
        JsArray<VariableDto> variables = resource == null
            ? JsArray.createArray().<JsArray<VariableDto>>cast()
            : resource;
        getView().setVariables(variables);
      }
    }
  }

  private class ValueSetsResourceCallback implements ResourceCallback<ValueSetsDto> {

    private final int offset;

    private final TableDto table;

    private ValueSetsResourceCallback(int offset, TableDto table) {
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
      JsArray<JavaScriptErrorDto> errors = (JsArray<JavaScriptErrorDto>) error
          .getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors);
      String firstError = errors.get(0).getMessage();
      NotificationEvent notificationEvent = NotificationEvent.Builder.newNotification().error(firstError).build();
      getEventBus().fireEvent(notificationEvent);
    }
  }

  public interface EntitySearchHandler {
    void onSearch(String identifier);
  }

  private class EntitySearchHandlerImpl implements EntitySearchHandler {

    @Override
    public void onSearch(final String identifier) {
      UriBuilder uriBuilder = UriBuilder.create().segment("entity", identifier, "type", table.getEntityType());
      ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource(uriBuilder.build()).get()
          .withCallback(Response.SC_NOT_FOUND, new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getEventBus().fireEvent(NotificationEvent.Builder.newNotification().error("EntityIdentifierNotFound")
                  .args(table.getEntityType(), identifier, table.getName()).build());
            }
          }).withCallback(Response.SC_OK, new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          fetcher.requestEntityDialog(table.getEntityType(), identifier);
        }
      }).send();
    }
  }

  private class DataFetcherImpl implements DataFetcher {

    private Request variablesRequest;

    private Request valuesRequest;

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
      if(filter != null && !filter.isEmpty()) {
        link.append("&select=").append(URL.encodePathSegment("name().matches(/" + cleanFilter(filter) + "/)"));
      }
      doRequest(offset, link.toString());
    }

    private String cleanFilter(String filter) {
      return filter.replaceAll("/", "\\\\/");
    }

    private String escape(String filter) {
      return filter.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");
    }

    private void doRequest(int offset, String link) {
      if(valuesRequest != null) {
        valuesRequest.cancel();
        valuesRequest = null;
      }
      valuesRequest = ResourceRequestBuilderFactory.<ValueSetsDto>newBuilder().forResource(link) //
          .get() //
          .withCallback(new ValueSetsResourceCallback(offset, table)) //
          .withCallback(SC_BAD_REQUEST, new BadRequestCallback()).send();
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
      valueSequencePopupPresenter.initialize(table, variable, entityIdentifier, false);
      addToPopupSlot(valueSequencePopupPresenter);
    }

    @Override
    public void requestEntityDialog(String entityType, String entityId) {
      entityDialogPresenter.initialize(table, entityType, entityId);
      addToPopupSlot(entityDialogPresenter);
    }

    @Override
    public void updateVariables(String select) {
      String link = table.getLink() + "/variables";
      if(select != null && !select.isEmpty()) {
        link += "?script=" + URL.encodePathSegment("name().matches(/" + cleanFilter(select) + "/)");
      }
      if(variablesRequest != null) {
        variablesRequest.cancel();
        variablesRequest = null;
      }
      variablesRequest = ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder().forResource(link).get()//
          .withCallback(new VariablesResourceCallback(table))
          .withCallback(Response.SC_BAD_REQUEST, new BadRequestCallback() {
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

    void addEntitySearchHandler(EntitySearchHandler handler);

    void setViewMode(ViewMode mode);
  }

  public enum ViewMode {
    DETAILED_MODE,
    SIMPLE_MODE;
  };

  public interface DataFetcher {
    void request(List<VariableDto> variables, int offset, int limit);

    void request(String filter, int offset, int limit);

    void requestBinaryValue(VariableDto variable, String entityIdentifier);

    void requestValueSequence(VariableDto variable, String entityIdentifier);

    void requestEntityDialog(String entityType, String entityId);

    void updateVariables(String select);
  }

  public interface ValueSetsProvider {
    void populateValues(int offset, ValueSetsDto valueSets);
  }

  public interface EntitySelectionHandler {

    void onEntitySelection(String entityType, String entityId);

  }

}
