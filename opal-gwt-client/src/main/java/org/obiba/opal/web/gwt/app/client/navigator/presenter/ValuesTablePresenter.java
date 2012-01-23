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

import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ValuesTablePresenter extends PresenterWidget<ValuesTablePresenter.Display> {

  private TableDto table;

  @Inject
  public ValuesTablePresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  public void setTable(TableDto table) {
    setTable(table, "");
  }

  public void setTable(TableDto table, VariableDto variable) {
    this.table = table;
    getView().setTable(table);
    getView().setValueSetsFetcher(new ValueSetsFetcherImpl());
    JsArray<VariableDto> variables = JsArray.createArray().<JsArray<VariableDto>> cast();
    variables.push(variable);
    getView().setVariables(variables);
  }

  public void setTable(TableDto table, String select) {
    this.table = table;
    getView().setTable(table);
    getView().setValueSetsFetcher(new ValueSetsFetcherImpl());
    String link = table.getLink() + "/variables";
    if(select != null && select.isEmpty() == false) {
      link += "?select=" + URL.encodePathSegment(select);
    }
    ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource(link).get().withCallback(new VariablesResourceCallback(table)).send();
  }

  //
  // Inner classes and interfaces
  //

  private class VariablesResourceCallback implements ResourceCallback<JsArray<VariableDto>> {

    private TableDto table;

    public VariablesResourceCallback(TableDto table) {
      super();
      this.table = table;
    }

    @Override
    public void onResource(Response response, JsArray<VariableDto> resource) {
      if(this.table.getLink().equals(ValuesTablePresenter.this.table.getLink())) {
        JsArray<VariableDto> variables = (resource != null) ? resource : JsArray.createArray().<JsArray<VariableDto>> cast();
        getView().setVariables(variables);

      }
    }
  }

  private class ValueSetsResourceCallback implements ResourceCallback<ValueSetsDto> {

    private int offset;

    private TableDto table;

    public ValueSetsResourceCallback(int offset, TableDto table) {
      super();
      this.offset = offset;
      this.table = table;
    }

    @Override
    public void onResource(Response response, ValueSetsDto resource) {
      if(this.table.getLink().equals(ValuesTablePresenter.this.table.getLink())) {
        getView().getValueSetsProvider().populateValues(offset, resource);
      }
    }
  }

  private class ValueSetsFetcherImpl implements ValueSetsFetcher {
    @Override
    public void request(List<VariableDto> variables, int offset, int limit) {
      StringBuilder link = getLinkBuilder(offset, limit);
      if(table.getVariableCount() > variables.size()) {
        link.append("&select=");
        StringBuilder script = new StringBuilder();
        for(int i = 0; i < variables.size(); i++) {
          String eval = "name().eq('" + variables.get(i).getName() + "')";
          if(i > 0) {
            script.append("or('").append(eval).append("')");
          } else {
            script.append(eval);
          }
        }
        link.append(URL.encodePathSegment(script.toString()));
      }
      doRequest(offset, link.toString());
    }

    @Override
    public void request(String filter, int offset, int limit) {
      StringBuilder link = getLinkBuilder(offset, limit);
      if(filter != null && filter.isEmpty() == false) {
        link.append("&select=").append(URL.encodePathSegment("name().matches(/" + filter + "/)"));
      }

      doRequest(offset, link.toString());
    }

    private void doRequest(int offset, String link) {
      ResourceRequestBuilderFactory.<ValueSetsDto> newBuilder().forResource(link).get().withCallback(new ValueSetsResourceCallback(offset, table)).send();
    }

    private StringBuilder getLinkBuilder(int offset, int limit) {
      return new StringBuilder(table.getLink()).append("/valueSets").append("?offset=").append(offset).append("&limit=").append(limit);
    }
  }

  public interface Display extends View {
    void setTable(TableDto table);

    void setVariables(JsArray<VariableDto> variables);

    ValueSetsProvider getValueSetsProvider();

    void setValueSetsFetcher(ValueSetsFetcher fetcher);
  }

  public interface ValueSetsFetcher {
    void request(List<VariableDto> variables, int offset, int limit);

    void request(String filter, int offset, int limit);
  }

  public interface ValueSetsProvider {
    void populateValues(int offset, ValueSetsDto valueSets);
  }

}
