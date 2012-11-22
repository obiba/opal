/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.search;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.opal.web.model.Search;

public class JsonToDtoConverter {

    private Search.QueryTermDto dtoQuery;

    public JsonToDtoConverter(Search.QueryTermDto dtoQuery)
    {
        this.dtoQuery = dtoQuery;
    }

    public Search.QueryResultDto convert(JSONObject json) throws JSONException {
        Search.QueryResultDto.Builder dtoResultBuilder = Search.QueryResultDto.newBuilder();
        Search.FacetResultDto dtoFacet = convertFacets(json.getJSONObject("facets"));

        return dtoResultBuilder.addFacets(dtoFacet).build();
    }

    private Search.FacetResultDto convertFacets(JSONObject jsonFacets) throws JSONException {
        Search.VariableTermDto dtoQueryVariable = dtoQuery.getExtension(Search.VariableTermDto.params);

        Search.FacetResultDto.Builder dtoFacetResultBuilder = Search.FacetResultDto.newBuilder();

        dtoFacetResultBuilder.setVariable(dtoQueryVariable.getVariable());

        JSONObject jsonFacet = jsonFacets.getJSONObject(dtoQuery.getFacet());

        if ("terms".equals(jsonFacet.get("_type"))) {
            convertTerms(jsonFacet.getJSONArray("terms"), dtoFacetResultBuilder);
        }
        else if ("statistical".equals(jsonFacet.get("_type"))) {
            convertStatistical(jsonFacet, dtoFacetResultBuilder);
        }

        return dtoFacetResultBuilder.build();
    }

    private void convertTerms(JSONArray terms, Search.FacetResultDto.Builder dtoFacetResultBuilder) throws JSONException {

        for (int i=0; i< terms.length(); i++) {
            JSONObject term = terms.getJSONObject(i);

            Search.FacetResultDto.TermFrequencyDto dtoTermFrequency=
                    Search.FacetResultDto.TermFrequencyDto.newBuilder()
                        .setTerm(term.getString("term"))
                        .setCount(term.getInt("count"))
                        .build();

            dtoFacetResultBuilder.addFrequencies(dtoTermFrequency);
        }
    }


    private void convertStatistical(JSONObject jsonStatistical, Search.FacetResultDto.Builder dtoFacetResultBuilder) throws JSONException {

        Search.FacetResultDto.StatisticalResultDto dtoStatistical =
                Search.FacetResultDto.StatisticalResultDto.newBuilder()
                    .setCount(jsonStatistical.getInt("count"))
                    .setTotal(jsonStatistical.getInt("total"))
                    .setMin(jsonStatistical.getInt("min"))
                    .setMax(jsonStatistical.getInt("max"))
                    .setMean(jsonStatistical.getInt("mean"))
                    .setSumOfSquares(jsonStatistical.getInt("sum_of_squares"))
                    .setVariance(jsonStatistical.getInt("variance"))
                    .setStdDeviation(jsonStatistical.getInt("std_deviation"))
                    .build();

        dtoFacetResultBuilder.setStatistics(dtoStatistical);
    }

}
