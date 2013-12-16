/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class IdentifiersTablePresenter extends PresenterWidget<IdentifiersTablePresenter.Display>
    implements IdentifiersTableUiHandlers {

  @Inject
  public IdentifiersTablePresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
    getView().setUiHandlers(this);
  }

  public void showIdentifiersTable(TableDto identifiers) {
    getView().showIdentifiersTable(identifiers);
    String uri = UriBuilders.IDENTIFIERS_TABLE_VARIABLES.create().build(identifiers.getName());
    ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder() //
        .forResource(uri) //
        .withCallback(new ResourceCallback<JsArray<VariableDto>>() {
          @Override
          public void onResource(Response response, JsArray<VariableDto> resource) {
            getView().setVariables(JsArrays.toSafeArray(resource));
          }
        }) //
        .get().send();
  }

  @Override
  public void onIdentifiersRequest(TableDto identifiersTable, String select, final int offset, int limit) {
    String uri = UriBuilders.IDENTIFIERS_TABLE_VALUESETS.create()
        .query("select", select).query("offset", "" + offset).query("limit", "" + limit).build(identifiersTable.getName());
    ResourceRequestBuilderFactory.<ValueSetsDto>newBuilder() //
        .forResource(uri) //
        .withCallback(new ResourceCallback<ValueSetsDto>() {
          @Override
          public void onResource(Response response, ValueSetsDto resource) {
            getView().setValueSets(offset, resource);
          }
        }) //
        .get().send();
  }

  public interface Display extends View, HasUiHandlers<IdentifiersTableUiHandlers> {

    void showIdentifiersTable(TableDto table);

    void setVariables(JsArray<VariableDto> variables);

    void setValueSets(int offset, ValueSetsDto valueSets);

  }

}
