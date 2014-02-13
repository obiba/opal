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

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.VariableDtoNature;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.FilterDto;
import org.obiba.opal.web.model.client.search.InTermDto;
import org.obiba.opal.web.model.client.search.LogicalTermDto;
import org.obiba.opal.web.model.client.search.MissingDto;
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

  public static final String TOTAL_FACET = "total";

  public static final String MISSING_FACET = "missings";

  public static final String MISSING_BY_CATEGORY_FACET = "missings_by_category";

  @Inject
  public ContingencyTablePresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  public void initialize(TableDto table, final VariableDto variableDto, final VariableDto crossWithVariable) {
    QueryTermsDto queries = QueryTermsDto.create();

    JsArray<QueryTermDto> terms = JsArrays.create().cast();

    final List<String> variableCategories = getCategories(variableDto);
    final List<String> crossWithCategories = getCategories(crossWithVariable);

    addFacetTerms(variableDto.getName(), variableCategories, crossWithVariable.getName(), terms);
    addTotalTerm(variableDto.getName(), variableCategories, crossWithVariable.getName(), terms);

    boolean isContinuous = VariableDtoNature.getNature(crossWithVariable) == VariableDtoNature.CONTINUOUS;
    addMissingTerm(MISSING_FACET, variableDto.getName(), crossWithVariable.getName(), terms, isContinuous);

    // add extra facet if continuous variable
    if(isContinuous) {
      addMissingTerm(MISSING_BY_CATEGORY_FACET, crossWithVariable.getName(), variableDto.getName(), terms, false);
    }

    queries.setQueriesArray(terms);

    ResourceRequestBuilderFactory.<QueryResultDto>newBuilder() //
        .forResource(
            UriBuilders.DATASOURCE_TABLE_FACETS_SEARCH.create().build(table.getDatasourceName(), table.getName())) //
        .post() //
        .withResourceBody(QueryTermsDto.stringify(queries)) //
        .withCallback(new ResourceCallback<QueryResultDto>() {
          @Override
          public void onResource(Response response, QueryResultDto resource) {
            getView().init(resource, variableDto, variableCategories, crossWithVariable, crossWithCategories);
            getView().draw();

          }
        }).send();
  }

  private void addTotalTerm(String variableName, List<String> variableCategories, String crossWithVariableName,
      JsArray<QueryTermDto> terms) {
    QueryTermDto query = QueryTermDto.create();
    query.setFacet(TOTAL_FACET);

    VariableTermDto variableTerm = VariableTermDto.create();
    variableTerm.setVariable(crossWithVariableName);
    query.setExtension("Search.VariableTermDto.field", variableTerm);

    LogicalTermDto logicalTerm = getLogicalTermDto(variableName, variableCategories);
    query.setExtension("Search.LogicalTermDto.facetFilter", logicalTerm);

    terms.push(query);
  }

  private void addMissingTerm(String facetName, String variableName, String crossWithVariableName,
      JsArray<QueryTermDto> terms, boolean limitSize) {

    QueryTermDto query = QueryTermDto.create();
    query.setFacet(facetName);

    if(limitSize) {
      query.setSize(0);
    }

    VariableTermDto variableTerm = VariableTermDto.create();
    variableTerm.setVariable(crossWithVariableName);
    query.setExtension("Search.VariableTermDto.field", variableTerm);

    LogicalTermDto missingTerm = getLogicalTermDto(variableName);
    query.setExtension("Search.LogicalTermDto.facetFilter", missingTerm);

    terms.push(query);
  }

  private LogicalTermDto getLogicalTermDto(String variableName, List<String> variableCategories) {
    LogicalTermDto logicalTerm = LogicalTermDto.create();
    logicalTerm.setOperator(TermOperator.AND_OP);
    FilterDto filter = FilterDto.create();
    filter.setVariable(variableName);
    InTermDto inTerm = InTermDto.create();
    JsArrayString values = JavaScriptObject.createArray().cast();

    for(String variableCategory : variableCategories) {
      values.push(variableCategory);
    }

    inTerm.setValuesArray(values);
    inTerm.setMinimumMatch(1);

    filter.setExtension("Search.InTermDto.terms", inTerm);
    logicalTerm.setExtension("Search.FilterDto.filters", filter);
    return logicalTerm;
  }

  private LogicalTermDto getLogicalTermDto(String variableName) {
    LogicalTermDto logicalTerm = LogicalTermDto.create();
    logicalTerm.setOperator(TermOperator.AND_OP);
    MissingDto missing = MissingDto.create();
    missing.setVariable(variableName);

    logicalTerm.setExtension("Search.MissingDto.missing", missing);
    return logicalTerm;
  }

  private void addFacetTerms(String variableName, List<String> variableCategories, String crossWithVariableName,
      JsArray<QueryTermDto> terms) {
    for(String variableCategory : variableCategories) {
      terms.push(getQueryTermDto(variableName, crossWithVariableName, variableCategory));
    }
  }

  private QueryTermDto getQueryTermDto(String variableName, String crossWithVariableName, String facetName) {
    QueryTermDto query = QueryTermDto.create();
    query.setFacet(facetName);

    VariableTermDto variableTerm = VariableTermDto.create();
    variableTerm.setVariable(crossWithVariableName);
    query.setExtension("Search.VariableTermDto.field", variableTerm);

    LogicalTermDto logicalTerm = getLogicalTermDto(variableName, facetName);

    query.setExtension("Search.LogicalTermDto.facetFilter", logicalTerm);
    return query;
  }

  private LogicalTermDto getLogicalTermDto(String variableName, String facetName) {
    LogicalTermDto logicalTerm = LogicalTermDto.create();
    logicalTerm.setOperator(TermOperator.AND_OP);
    FilterDto filter = FilterDto.create();
    filter.setVariable(variableName);
    InTermDto inTerm = InTermDto.create();
    JsArrayString values = JavaScriptObject.createArray().cast();
    values.push(facetName);
    inTerm.setValuesArray(values);
    inTerm.setMinimumMatch(1);

    filter.setExtension("Search.InTermDto.terms", inTerm);
    logicalTerm.setExtension("Search.FilterDto.filters", filter);
    return logicalTerm;
  }

  private List<String> getCategories(VariableDto variable) {
    List<String> categories = new ArrayList<String>();

    if("boolean".equals(variable.getValueType())) {
      categories.add("true");
      categories.add("false");
    } else {
      for(CategoryDto categoryDto : JsArrays.toIterable(variable.getCategoriesArray())) {
        categories.add(categoryDto.getName());
      }
    }

    return categories;
  }

  public interface Display extends View {

    void init(QueryResultDto resource, VariableDto variableDto, List<String> variableCategories,
        VariableDto crossWithVariable, List<String> crossWithCategories);

    void draw();
  }

}