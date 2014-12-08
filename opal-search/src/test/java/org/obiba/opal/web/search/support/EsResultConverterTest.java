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

import static org.fest.assertions.api.Assertions.assertThat;

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
    JSONObject jsonResult = new JSONObject("{\n" +
        "    \"took\": 4,\n" +
        "    \"timed_out\": false,\n" +
        "    \"_shards\": {\n" +
        "        \"total\": 5,\n" +
        "        \"successful\": 5,\n" +
        "        \"failed\": 0\n" +
        "    },\n" +
        "    \"hits\": {\n" +
        "        \"total\": 8053,\n" +
        "        \"max_score\": 0,\n" +
        "        \"hits\": []\n" +
        "    },\n" +
        "    \"aggregations\": {\n" +
        "        \"0\": {\n" +
        "            \"buckets\": [\n" +
        "                {\n" +
        "                    \"key\": 2,\n" +
        "                    \"doc_count\": 4916\n" +
        "                },\n" +
        "                {\n" +
        "                    \"key\": 1,\n" +
        "                    \"doc_count\": 3137\n" +
        "                }\n" +
        "            ]\n" +
        "        }\n" +
        "    }\n" +
        "}");

    EsResultConverter converter = new EsResultConverter();
    Search.QueryResultDto dtoResult = converter.convert(jsonResult);

    validateCategoricalQueryResultDto(dtoResult);
  }

  @Test
  public void testConvert_ValidStatisticalResultDto() throws Exception {
    JSONObject jsonResult = new JSONObject("{\n" +
        "    \"took\": 1,\n" +
        "    \"timed_out\": false,\n" +
        "    \"_shards\": {\n" +
        "        \"total\": 5,\n" +
        "        \"successful\": 5,\n" +
        "        \"failed\": 0\n" +
        "    },\n" +
        "    \"hits\": {\n" +
        "        \"total\": 8053,\n" +
        "        \"max_score\": 0,\n" +
        "        \"hits\": []\n" +
        "    },\n" +
        "    \"aggregations\": {\n" +
        "        \"0\": {\n" +
        "            \"count\": 8053,\n" +
        "            \"min\": 35,\n" +
        "            \"max\": 69,\n" +
        "            \"avg\": 51.141810505401715,\n" +
        "            \"sum\": 411845,\n" +
        "            \"sum_of_squares\": 21786719,\n" +
        "            \"variance\": 89.93170897837224,\n" +
        "            \"std_deviation\": 9.48323304460943\n" +
        "        }\n" +
        "    }\n" +
        "}");

    EsResultConverter converter = new EsResultConverter();
    Search.QueryResultDto dtoResult = converter.convert(jsonResult);

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
    assertThat(dtoResult).isNotNull();

    Search.FacetResultDto dtoFacetResult = dtoResult.getFacets(0);
    assertThat(dtoFacetResult).isNotNull();
    assertThat(dtoQuery.getFacet()).isEqualTo(dtoFacetResult.getFacet());

    List<Search.FacetResultDto.TermFrequencyResultDto> listTermDto = dtoFacetResult.getFrequenciesList();
    assertThat(listTermDto).isNotNull();
    assertThat(listTermDto).hasSize(2);

    Search.FacetResultDto.StatisticalResultDto statistics = dtoFacetResult.getStatistics();
    assertThat(statistics.hasCount()).isFalse();
  }

  private void validateStatisticalQueryResultDto(Search.QueryResultDto dtoResult) {
    assertThat(dtoResult).isNotNull();

    Search.FacetResultDto dtoFacetResult = dtoResult.getFacets(0);
    assertThat(dtoFacetResult).isNotNull();
    assertThat(dtoQuery.getFacet()).isEqualTo(dtoFacetResult.getFacet());

    Search.FacetResultDto.StatisticalResultDto dtoStatistical = dtoFacetResult.getStatistics();
    assertThat(dtoStatistical).isNotNull();
    assertThat(dtoStatistical.hasCount()).isTrue();
    assertThat(dtoStatistical.hasMin()).isTrue();
    assertThat(dtoStatistical.hasMax()).isTrue();
    assertThat(dtoStatistical.hasMean()).isTrue();
    assertThat(dtoStatistical.hasStdDeviation()).isTrue();
    assertThat(dtoStatistical.hasTotal()).isTrue();
    assertThat(dtoStatistical.hasSumOfSquares()).isTrue();
    assertThat(dtoStatistical.hasVariance()).isTrue();

    List<Search.FacetResultDto.TermFrequencyResultDto> listTermDto = dtoFacetResult.getFrequenciesList();
    assertThat(listTermDto).isEmpty();
  }
}
