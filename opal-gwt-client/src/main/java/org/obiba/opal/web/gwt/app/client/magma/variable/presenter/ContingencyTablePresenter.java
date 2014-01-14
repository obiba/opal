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
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ContingencyTablePresenter extends PresenterWidget<ContingencyTablePresenter.Display> {

  @Inject
  public ContingencyTablePresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
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
    QueryTermDto query = QueryTermDto.create();
    query.setFacet("total");

    VariableTermDto variableTerm = VariableTermDto.create();
    variableTerm.setVariable(crossWithVariable.getName());
    query.setExtension("Search.VariableTermDto.field", variableTerm);

    LogicalTermDto logicalTerm = getLogicalTermDto(variableDto);
    query.setExtension("Search.LogicalTermDto.facetFilter", logicalTerm);

    terms.push(query);
  }

  private LogicalTermDto getLogicalTermDto(VariableDto variableDto) {
    LogicalTermDto logicalTerm = LogicalTermDto.create();
    logicalTerm.setOperator(TermOperator.AND_OP);
    FilterDto filter = FilterDto.create();
    filter.setVariable(variableDto.getName());
    InTermDto inTerm = InTermDto.create();
    JsArrayString values = JavaScriptObject.createArray().cast();

    for(int i = 0; i < variableDto.getCategoriesArray().length(); i++) {
      values.push(variableDto.getCategoriesArray().get(i).getName());
    }

    inTerm.setValuesArray(values);
    inTerm.setMinimumMatch(1);

    filter.setExtension("Search.InTermDto.terms", inTerm);
    logicalTerm.setExtension("Search.FilterDto.filters", filter);
    return logicalTerm;
  }

  private void addFacetTerms(VariableDto variableDto, VariableDto crossWithVariable, JsArray<QueryTermDto> terms) {
    for(int i = 0; i < variableDto.getCategoriesArray().length(); i++) {
      QueryTermDto query = QueryTermDto.create();
      query.setFacet(variableDto.getCategoriesArray().get(i).getName());

      VariableTermDto variableTerm = VariableTermDto.create();
      variableTerm.setVariable(crossWithVariable.getName());
      query.setExtension("Search.VariableTermDto.field", variableTerm);

      LogicalTermDto logicalTerm = getLogicalTermDto(variableDto, i);

      query.setExtension("Search.LogicalTermDto.facetFilter", logicalTerm);

      terms.push(query);
    }
  }

  private LogicalTermDto getLogicalTermDto(VariableDto variableDto, int i) {
    LogicalTermDto logicalTerm = LogicalTermDto.create();
    logicalTerm.setOperator(TermOperator.AND_OP);
    FilterDto filter = FilterDto.create();
    filter.setVariable(variableDto.getName());
    InTermDto inTerm = InTermDto.create();
    JsArrayString values = JavaScriptObject.createArray().cast();
    values.push(variableDto.getCategoriesArray().get(i).getName());
    inTerm.setValuesArray(values);
    inTerm.setMinimumMatch(1);

    filter.setExtension("Search.InTermDto.terms", inTerm);
    logicalTerm.setExtension("Search.FilterDto.filters", filter);
    return logicalTerm;
  }

  public interface Display extends View {

    void init(QueryResultDto resource, VariableDto variableDto, VariableDto crossWithVariable);

    void draw();
  }

}