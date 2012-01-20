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
    this.table = table;
    getView().setEntityType(table.getEntityType());
    getView().setValueSetsProvider(new ValueSetsProviderImpl());
    ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource(table.getLink() + "/variables").get().withCallback(new VariablesResourceCallback(table)).send();
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

    private TableDto table;

    public ValueSetsResourceCallback(TableDto table) {
      super();
      this.table = table;
    }

    @Override
    public void onResource(Response response, ValueSetsDto resource) {
      if(this.table.getLink().equals(ValuesTablePresenter.this.table.getLink())) {
        getView().populateValues(resource);
      }
    }
  }

  private class ValueSetsProviderImpl implements ValueSetsProvider {
    @Override
    public void request(List<VariableDto> variables, int offset, int limit) {
      String link = table.getLink() + "/valueSets";
      link += "?offset=" + offset + "&limit=" + limit;
      ResourceRequestBuilderFactory.<ValueSetsDto> newBuilder().forResource(link).get().withCallback(new ValueSetsResourceCallback(table)).send();
    }
  }

  public interface Display extends View {
    void setEntityType(String type);

    void setVariables(JsArray<VariableDto> variables);

    void populateValues(ValueSetsDto valueSets);

    void setValueSetsProvider(ValueSetsProvider provider);
  }

  public interface ValueSetsProvider {
    void request(List<VariableDto> variables, int offset, int limit);
  }

}
