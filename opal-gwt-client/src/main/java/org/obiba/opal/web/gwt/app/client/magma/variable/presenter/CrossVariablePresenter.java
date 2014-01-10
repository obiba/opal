/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.variable.presenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.FilterDto;
import org.obiba.opal.web.model.client.search.InTermDto;
import org.obiba.opal.web.model.client.search.LogicalTermDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;
import org.obiba.opal.web.model.client.search.QueryTermDto;
import org.obiba.opal.web.model.client.search.QueryTermsDto;
import org.obiba.opal.web.model.client.search.TermOperator;
import org.obiba.opal.web.model.client.search.VariableTermDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class CrossVariablePresenter extends PresenterWidget<CrossVariablePresenter.Display>
    implements CrossVariableUiHandlers {

  @Inject
  public CrossVariablePresenter(Display display, EventBus eventBus) {
    super(eventBus, display);

    getView().setUiHandlers(this);
  }

  public void initialize(TableDto table, final VariableDto variableDto, final VariableDto crossWithVariable) {
    QueryTermsDto queries = QueryTermsDto.create();

    JsArray<QueryTermDto> terms = JsArrays.create().cast();

    addFacetTerms(variableDto, crossWithVariable, terms);
    addTotalTerm(variableDto, crossWithVariable, terms);

    queries.setQueriesArray(terms);

    ResourceRequestBuilderFactory.<QueryResultDto>newBuilder() //
        .forResource(
            UriBuilders.DATASOURCE_TABLE_FACETS_SEARCH.create().build(table.getDatasourceName(), table.getName())) //
        .post() //
        .withResourceBody(QueryTermsDto.stringify(queries)) //
        .withCallback(new ResourceCallback<QueryResultDto>() {
          @Override
          public void onResource(Response response, QueryResultDto resource) {
            getView().init(resource, variableDto, crossWithVariable);
            getView().draw();

          }
        }).send();
  }

  private void addTotalTerm(VariableDto variableDto, VariableDto crossWithVariable, JsArray<QueryTermDto> terms) {
    QueryTermDto q = QueryTermDto.create();
    q.setFacet("total");

    VariableTermDto v = VariableTermDto.create();
    v.setVariable(crossWithVariable.getName());
    q.setExtension("Search.VariableTermDto.field", v);

    LogicalTermDto l = getLogicalTermDto(variableDto);
    q.setExtension("Search.LogicalTermDto.facetFilter", l);

    terms.push(q);
  }

  private LogicalTermDto getLogicalTermDto(VariableDto variableDto) {
    LogicalTermDto l = LogicalTermDto.create();
    l.setOperator(TermOperator.AND_OP);
    FilterDto f = FilterDto.create();
    f.setVariable(variableDto.getName());
    InTermDto t = InTermDto.create();
    JsArrayString values = JavaScriptObject.createArray().cast();

    for(int i = 0; i < variableDto.getCategoriesArray().length(); i++) {
      values.push(variableDto.getCategoriesArray().get(i).getName());
    }

    t.setValuesArray(values);
    t.setMinimumMatch(1);

    f.setExtension("Search.InTermDto.terms", t);
    l.setExtension("Search.FilterDto.filters", f);
    return l;
  }

  private void addFacetTerms(VariableDto variableDto, VariableDto crossWithVariable, JsArray<QueryTermDto> terms) {
    for(int i = 0; i < variableDto.getCategoriesArray().length(); i++) {
      QueryTermDto q = QueryTermDto.create();
      q.setFacet(variableDto.getCategoriesArray().get(i).getName());

      VariableTermDto v = VariableTermDto.create();
      v.setVariable(crossWithVariable.getName());
      q.setExtension("Search.VariableTermDto.field", v);

      LogicalTermDto l = getLogicalTermDto(variableDto, i);

      q.setExtension("Search.LogicalTermDto.facetFilter", l);

      terms.push(q);
    }
  }

  private LogicalTermDto getLogicalTermDto(VariableDto variableDto, int i) {
    LogicalTermDto logicalTerm = LogicalTermDto.create();
    logicalTerm.setOperator(TermOperator.AND_OP);
    FilterDto filter = FilterDto.create();
    filter.setVariable(variableDto.getName());
    InTermDto t = InTermDto.create();
    JsArrayString values = JavaScriptObject.createArray().cast();
    values.push(variableDto.getCategoriesArray().get(i).getName());
    t.setValuesArray(values);
    t.setMinimumMatch(1);

    filter.setExtension("Search.InTermDto.terms", t);
    logicalTerm.setExtension("Search.FilterDto.filters", filter);
    return logicalTerm;
  }

  public interface Display extends View, HasUiHandlers<CrossVariableUiHandlers> {

    void init(QueryResultDto resource, VariableDto variableDto, VariableDto crossWithVariable);

    void draw();
  }

}