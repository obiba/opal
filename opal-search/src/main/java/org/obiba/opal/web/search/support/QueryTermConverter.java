/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search.support;

import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.opal.core.domain.VariableNature;
import org.obiba.opal.web.model.Search;
import org.springframework.util.Assert;

/**
 * Converts a DTO query to an elastic search JSON query
 */
public class QueryTermConverter {

  private final IndexManagerHelper indexManagerHelper;

  /**
   * @param fieldPrefix - the field prefix is in the form of 'datasource.table' and is needed to fully qualify a
   * variable.
   */
  public QueryTermConverter(IndexManagerHelper indexManagerHelper) {
    Assert.notNull(indexManagerHelper, "Index Manager Helper is null!");

    this.indexManagerHelper = indexManagerHelper;
  }

  /**
   * Converts a DTO query to an elastic search JSON query
   *
   * @param dtoQueries
   * @return
   * @throws JSONException
   */
  public JSONObject convert(Search.QueryTermsDto dtoQueries) throws JSONException {
    Assert.notNull(dtoQueries, "Query term DTO is null!");

    JSONObject jsonQuery = new JSONObject("{\"query\":{\"match_all\":{}}, \"size\":0}");
    JSONObject jsonFacets = new JSONObject();

    for(Search.QueryTermDto dtoQuery : dtoQueries.getQueriesList()) {
      JSONObject jsonFacet = new JSONObject();

      if(dtoQuery.hasExtension(Search.LogicalTermDto.filter)) {
        convertLogicalFilter("filter", dtoQuery.getExtension(Search.LogicalTermDto.filter), jsonFacet);
      } else {
        if(dtoQuery.hasExtension(Search.VariableTermDto.field)) {
          convertField(dtoQuery, jsonFacet);
        }

        if(dtoQuery.hasExtension(Search.LogicalTermDto.facetFilter)) {
          convertLogicalFilter("facet_filter", dtoQuery.getExtension(Search.LogicalTermDto.facetFilter), jsonFacet);
        }
      }

      if(dtoQuery.hasGlobal()) {
        jsonFacet.put("global", dtoQuery.getGlobal());
      }

      jsonFacets.put(dtoQuery.getFacet(), jsonFacet);
    }

    jsonQuery.put("facets", jsonFacets);

    return jsonQuery;
  }

  private void convertLogicalFilter(String filterName, Search.LogicalTermDto dtoLogicalFilter, JSONObject jsonFacet)
      throws JSONException {
    Search.TermOperator operator = dtoLogicalFilter.getOperator();
    String operatorName = operator == Search.TermOperator.AND_OP ? "and" : "or";
    JSONObject jsonOperator = new JSONObject();

    List<Search.FilterDto> filters = dtoLogicalFilter.getExtension(Search.FilterDto.filters);

    if(!filters.isEmpty()) {
      if(filters.size() > 1) {
        for(Search.FilterDto filter : filters) {
          jsonOperator.accumulate(operatorName, convertFilterType(filter));
        }

        jsonFacet.put(filterName, jsonOperator);
      } else {
        jsonFacet.put(filterName, convertFilterType(filters.get(0)));
      }
    }

    if(dtoLogicalFilter.hasExtension(Search.MissingDto.missing)) {
      Search.MissingDto missing = dtoLogicalFilter.getExtension(Search.MissingDto.missing);
      jsonFacet.put(filterName, convertFilterType(missing));
    }
  }

  private void convertField(Search.QueryTermDto dtoQuery, JSONObject jsonFacet)
      throws JSONException, UnsupportedOperationException {

    Search.VariableTermDto dtoVariable = dtoQuery.getExtension(Search.VariableTermDto.field);

    String variable = dtoVariable.getVariable();
    JSONObject jsonField = new JSONObject();
    jsonField.put("field", variableFieldName(variable));

    // Missing facetFilter should always be treated as terms so they can return "missings" values
    boolean hasMissing = dtoQuery.hasExtension(Search.LogicalTermDto.facetFilter) &&
        dtoQuery.getExtension(Search.LogicalTermDto.facetFilter).hasExtension(Search.MissingDto.missing);

    if(indexManagerHelper.getVariableNature(variable) == VariableNature.CATEGORICAL || hasMissing) {
      if(dtoQuery.hasSize()) {
        jsonField.put("size", dtoQuery.getSize());
      }

      jsonFacet.put("terms", jsonField);
    } else if(indexManagerHelper.getVariableNature(variable) == VariableNature.CONTINUOUS) {
      jsonFacet.put("statistical", jsonField);
    } else {
      throw new UnsupportedOperationException("Variable nature not supported");
    }
  }

  private JSONObject convertFilterType(Search.FilterDto dtoFilter) throws JSONException {
    JSONObject jsonFilter = new JSONObject();

    String variable = dtoFilter.getVariable();

    if(dtoFilter.hasExtension(Search.InTermDto.terms)) {
      convertTermFilter(dtoFilter.getExtension(Search.InTermDto.terms), jsonFilter, variable);
    } else if(dtoFilter.hasExtension(Search.RangeTermDto.range)) {
      convertRangeFilter(dtoFilter.getExtension(Search.RangeTermDto.range), jsonFilter, variable);
    } else {
      convertExistFilter(jsonFilter, variable);
    }

    if(dtoFilter.hasNot()) {
      jsonFilter = new JSONObject().put("not", jsonFilter);
    }

    return jsonFilter;
  }

  private JSONObject convertFilterType(Search.MissingDto dtoFilter) throws JSONException {
    JSONObject jsonFilter = new JSONObject();
    String variable = dtoFilter.getVariable();
    convertMissingFilter(jsonFilter, variable);

    return jsonFilter;
  }

  private void convertRangeFilter(Search.RangeTermDto dtoRange, JSONObject jsonFilter, String variable)
      throws JSONException {

    JSONObject jsonRange = new JSONObject();

    if(dtoRange.hasFrom()) {
      jsonRange.put("from", dtoRange.getFrom());
    }

    if(dtoRange.hasIncludeLower()) {
      jsonRange.put("include_lower", dtoRange.getIncludeLower());
    }

    if(dtoRange.hasTo()) {
      jsonRange.put("to", dtoRange.getTo());
    }

    if(dtoRange.hasIncludeUpper()) {
      jsonRange.put("include_upper", dtoRange.getIncludeUpper());
    }

    jsonFilter.put("numeric_range", new JSONObject().put(variableFieldName(variable), jsonRange));
  }

  private void convertExistFilter(JSONObject jsonFilter, String variable) throws JSONException {
    jsonFilter.put("exists", new JSONObject().put("field", variableFieldName(variable)));
  }

  private void convertTermFilter(Search.InTermDto dtoTerms, JSONObject jsonFilter, String variable)
      throws JSONException {

    // see if we're dealing a 'term' or 'terms' elastic search facet type
    List<String> values = dtoTerms.getValuesList();

    if(values.size() == 1) {
      jsonFilter.put("term", new JSONObject().put(variableFieldName(variable), values.get(0)));
    } else {
      jsonFilter.put("terms", new JSONObject().put(variableFieldName(variable), values));
    }
  }

  private void convertMissingFilter(JSONObject jsonFilter, String variable) throws JSONException {
    jsonFilter.put("missing", new JSONObject().put("field", variableFieldName(variable)));
  }

  private String variableFieldName(String variable) {
    return indexManagerHelper.getIndexFieldName(variable);
  }

}