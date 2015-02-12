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

  private final int termsFacetSize;

  /**
   * @param indexManagerHelper - IndexManagerHelper provides certain variable information required for conversion
   * @param termsFacetSize - used to limit the 'terms' facet results
   */
  public QueryTermConverter(IndexManagerHelper indexManagerHelper, int termsFacetSize) {
    Assert.notNull(indexManagerHelper, "Index Manager Helper is null!");

    this.indexManagerHelper = indexManagerHelper;
    this.termsFacetSize = termsFacetSize;
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
    JSONObject jsonAggregations = new JSONObject();

    for(Search.QueryTermDto dtoQuery : dtoQueries.getQueriesList()) {
      JSONObject jsonAggregation = new JSONObject();

      if(dtoQuery.hasExtension(Search.LogicalTermDto.filter)) {
        convertLogicalFilter("filter", dtoQuery.getExtension(Search.LogicalTermDto.filter), jsonAggregation);
      } else if(dtoQuery.hasExtension(Search.LogicalTermDto.facetFilter)) {
        convertFilter(dtoQuery, jsonAggregation);
      } else if(dtoQuery.hasExtension(Search.VariableTermDto.field)) {
        convertField(dtoQuery.getExtension(Search.VariableTermDto.field), jsonAggregation);
      } else if(dtoQuery.hasGlobal()) {
        convertGlobal(dtoQuery, jsonAggregation);
      }

      jsonAggregations.put(dtoQuery.getFacet(), jsonAggregation);
    }

    jsonQuery.put("aggregations", jsonAggregations);

    return jsonQuery;
  }

  private void convertLogicalFilter(String filterName, Search.LogicalTermDto dtoLogicalFilter,
      JSONObject jsonAggregation) throws JSONException {
    Search.TermOperator operator = dtoLogicalFilter.getOperator();
    String operatorName = operator == Search.TermOperator.AND_OP ? "and" : "or";
    JSONObject jsonOperator = new JSONObject();

    List<Search.FilterDto> filters = dtoLogicalFilter.getExtension(Search.FilterDto.filters);

    if(filters.size() > 1) {
      for(Search.FilterDto filter : filters) {
        jsonOperator.accumulate(operatorName, convertFilterType(filter));
      }

      jsonAggregation.put(filterName, jsonOperator);
    } else if(filters.size() == 1) {
      jsonAggregation.put(filterName, convertFilterType(filters.get(0)));
    }
  }

  private void convertFilter(Search.QueryTermDto dtoQuery, JSONObject jsonAggregation)
      throws JSONException, UnsupportedOperationException {
    convertLogicalFilter("filter", dtoQuery.getExtension(Search.LogicalTermDto.facetFilter), jsonAggregation);
    if(dtoQuery.hasExtension(Search.VariableTermDto.field)) {
      convertNestedField(dtoQuery.getExtension(Search.VariableTermDto.field), jsonAggregation);
    }
  }

  private void convertGlobal(Search.QueryTermDto dtoQuery, JSONObject jsonAggregation)
      throws JSONException, UnsupportedOperationException {
    jsonAggregation.put("global", new JSONObject());
    if(dtoQuery.hasExtension(Search.VariableTermDto.field)) {
      convertNestedField(dtoQuery.getExtension(Search.VariableTermDto.field), jsonAggregation);
    }
  }

  private void convertNestedField(Search.VariableTermDto dtoVariable, JSONObject jsonAggregation)
      throws JSONException, UnsupportedOperationException {
    JSONObject jsonAgg = new JSONObject();
    convertField(dtoVariable, jsonAgg);
    JSONObject jsonAggregation2 = new JSONObject();
    jsonAggregation2.put("0", jsonAgg);
    jsonAggregation.put("aggregations", jsonAggregation2);
  }

  private void convertField(Search.VariableTermDto dtoVariable, JSONObject jsonAggregation)
      throws JSONException, UnsupportedOperationException {
    if(dtoVariable.hasType()) {
      convertFieldByType(dtoVariable, jsonAggregation);
    } else {
      convertFieldByNature(dtoVariable, jsonAggregation);
    }
  }

  /**
   * Convert variable query to field aggregation of the specified type (if applicable).
   *
   * @param dtoVariable
   * @param jsonAggregation
   * @throws JSONException
   * @throws UnsupportedOperationException
   */
  private void convertFieldByType(Search.VariableTermDto dtoVariable, JSONObject jsonAggregation)
      throws JSONException, UnsupportedOperationException {
    String variable = dtoVariable.getVariable();
    JSONObject jsonField = new JSONObject();
    jsonField.put("field", variableFieldName(variable));

    switch(dtoVariable.getType()) {
      case MISSING:
        jsonAggregation.put("missing", jsonField);
        break;
      case CARDINALITY:
        jsonAggregation.put("cardinality", jsonField);
        break;
      case TERMS:
        jsonField.put("size", termsFacetSize);
        jsonAggregation.put("terms", jsonField);
        break;
      case STATS:
        if(indexManagerHelper.getVariableNature(variable) != VariableNature.CONTINUOUS)
          throw new IllegalArgumentException(
              "Statistics aggregation is only applicable to numeric continuous variables");
        jsonAggregation.put("extended_stats", jsonField);
        break;
      case PERCENTILES:
        if(indexManagerHelper.getVariableNature(variable) != VariableNature.CONTINUOUS)
          throw new IllegalArgumentException(
              "Percentiles aggregation is only applicable to numeric continuous variables");
        jsonAggregation.put("percentiles", jsonField);
        break;
    }
  }

  /**
   * Convert field query to default field aggregation according to variable nature.
   *
   * @param dtoVariable
   * @param jsonAggregation
   * @throws JSONException
   * @throws UnsupportedOperationException
   */
  private void convertFieldByNature(Search.VariableTermDto dtoVariable, JSONObject jsonAggregation)
      throws JSONException, UnsupportedOperationException {
    String variable = dtoVariable.getVariable();
    JSONObject jsonField = new JSONObject();
    jsonField.put("field", variableFieldName(variable));

    switch(indexManagerHelper.getVariableNature(variable)) {

      case CONTINUOUS:
        jsonAggregation.put("extended_stats", jsonField);
        break;

      case CATEGORICAL:
        // we want all categories frequencies: as we do not know the variable description at this point,
        // set a maximum size to term facets request (0 means maximum)
        // http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations-bucket-terms-aggregation.html
        jsonField.put("size", 0);
        jsonAggregation.put("terms", jsonField);
        break;

      default:
        jsonField.put("size", termsFacetSize);
        jsonAggregation.put("terms", jsonField);
        break;
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

    if(dtoFilter.hasNot() && dtoFilter.getNot()) {
      jsonFilter = new JSONObject().put("not", jsonFilter);
    }

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

  private String variableFieldName(String variable) {
    return indexManagerHelper.getIndexFieldName(variable);
  }

}