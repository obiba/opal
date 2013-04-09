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
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.web.model.Search;

import junit.framework.Assert;

public class EsResultConverterTest {

  private Search.QueryTermDto dtoQuery;

  @Before
  public void setUp() throws Exception {
    Search.QueryTermDto.Builder dtoBuilder = Search.QueryTermDto.newBuilder().setFacet("0");

    Search.VariableTermDto.Builder variableDto = Search.VariableTermDto.newBuilder();
    variableDto.setVariable("LAST_MEAL_WHEN");
    dtoBuilder.setExtension(Search.VariableTermDto.field, variableDto.build());

    dtoQuery = dtoBuilder.build();
  }

  @Test
  public void testConvert_ValidCategoricalResultDto() throws Exception {
    JSONObject jsonQuery = new JSONObject(
        "{\"took\":38, \"timed_out\":false, \"_shards\":{\"total\":5, \"successful\":5, \"failed\":0 }, " +
            "" + "\"hits\":{\"total\":20, \"max_score\":1.0, \"hits\":[] }, \"facets\":{\"0\":{\"_type\":\"terms\", " +
            "\"missing\":0, \"total\":20, \"other\":0, \"terms\":[{\"term\":\"TIME_24\", \"count\":20 } ] } } }");

    EsResultConverter converter = new EsResultConverter();
    Search.QueryResultDto dtoResult = converter.convert(jsonQuery);

    validateCategoricalQueryResultDto(dtoResult);
  }

  @Test
  public void testConvert_ValidStatisticalResultDto() throws Exception {
    JSONObject jsonQuery = new JSONObject(
        "{\"took\": 32, \"timed_out\": false, \"_shards\": {\"total\": 5, \"successful\": 5, \"failed\": 0 }," +
            " \"hits\": {\"total\": 5, \"max_score\": 1.0, \"hits\": [] }, \"facets\": {\"0\": {\"_type\": \"statistical\", " +
            "\"count\": 5, \"total\": 820.8, \"min\": 155.6, \"max\": 179.9, \"mean\": 164.16, \"sum_of_squares\": 135096.62, " +
            "\"variance\": 70.81840000000084, \"std_deviation\": 8.415366896339151 } } }");

    EsResultConverter converter = new EsResultConverter();
    Search.QueryResultDto dtoResult = converter.convert(jsonQuery);

    validateStatisticalQueryResultDto(dtoResult);
  }

  @Test(expected = JSONException.class)
  public void testConvert_InvalidJsonQuery() throws Exception {
    // missing a colon
    JSONObject jsonQuery = new JSONObject(
        "{\"took\" 38, \"timed_out\":false, \"_shards\":{\"total\":5, \"successful\":5, " +
            "\"failed\":0 }, \"hits\":{\"total\":20, \"max_score\":1.0, \"hits\":[] }, \"facets\":{\"0\":{\"_type\":\"terms\", \"missing\":0, \"total\":20, \"other\":0, \"terms\":[{\"term\":\"TIME_24\", \"count\":20 } ] } } }");

    new EsResultConverter().convert(jsonQuery);
  }

  private void validateCategoricalQueryResultDto(Search.QueryResultDto dtoResult) {
    Assert.assertNotNull(dtoResult);

    Search.FacetResultDto dtoFacetResult = dtoResult.getFacets(0);
    Assert.assertNotNull(dtoFacetResult);
    Assert.assertEquals(dtoQuery.getFacet(), dtoFacetResult.getFacet());

    List<Search.FacetResultDto.TermFrequencyResultDto> listTermDto = dtoFacetResult.getFrequenciesList();

    Assert.assertNotNull(listTermDto);

    Search.FacetResultDto.StatisticalResultDto statistics = dtoFacetResult.getStatistics();
    Assert.assertFalse(statistics.hasCount());
  }

  private void validateStatisticalQueryResultDto(Search.QueryResultDto dtoResult) {
    Assert.assertNotNull(dtoResult);

    Search.FacetResultDto dtoFacetResult = dtoResult.getFacets(0);
    Assert.assertNotNull(dtoFacetResult);
    Assert.assertEquals(dtoQuery.getFacet(), dtoFacetResult.getFacet());

    Search.FacetResultDto.StatisticalResultDto dtoStatistical = dtoFacetResult.getStatistics();
    Assert.assertNotNull(dtoStatistical);
    Assert.assertTrue(dtoStatistical.hasCount());

    List<Search.FacetResultDto.TermFrequencyResultDto> listTermDto = dtoFacetResult.getFrequenciesList();
    Assert.assertTrue(listTermDto.isEmpty());
  }
}
