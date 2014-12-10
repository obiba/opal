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
import org.junit.Test;
import org.obiba.opal.web.model.Search;

import static org.fest.assertions.api.Assertions.assertThat;

public class EsResultConverterTest {

  @Test
  public void test_convert_categorical_result() throws Exception {
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
  public void test_convert_statistical_result() throws Exception {
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

  @Test
  public void test_convert_missing_result() throws Exception {
    JSONObject jsonResult = new JSONObject("{\n" +
        "    \"took\": 4,\n" +
        "    \"timed_out\": false,\n" +
        "    \"_shards\": {\n" +
        "        \"total\": 5,\n" +
        "        \"successful\": 5,\n" +
        "        \"failed\": 0\n" +
        "    },\n" +
        "    \"hits\": {\n" +
        "        \"total\": 6,\n" +
        "        \"max_score\": 0,\n" +
        "        \"hits\": []\n" +
        "    },\n" +
        "    \"aggregations\": {\n" +
        "        \"1\": {\n" +
        "            \"0\": {\n" +
        "                \"doc_count\": 0\n" +
        "            },\n" +
        "            \"doc_count\": 4\n" +
        "        },\n" +
        "        \"2\": {\n" +
        "            \"0\": {\n" +
        "                \"doc_count\": 0\n" +
        "            },\n" +
        "            \"doc_count\": 5\n" +
        "        },\n" +
        "        \"total\": {\n" +
        "            \"0\": {\n" +
        "                \"doc_count\": 0\n" +
        "            },\n" +
        "            \"doc_count\": 3\n" +
        "        }\n" +
        "    }\n" +
        "}");

    EsResultConverter converter = new EsResultConverter();
    Search.QueryResultDto dtoResult = converter.convert(jsonResult);

    validateMissingQueryResultDto(dtoResult);
  }

  @Test
  public void test_convert_cardinality_result() throws Exception {
    JSONObject jsonResult = new JSONObject("{\n" +
        "    \"took\": 11,\n" +
        "    \"timed_out\": false,\n" +
        "    \"_shards\": {\n" +
        "        \"total\": 5,\n" +
        "        \"successful\": 5,\n" +
        "        \"failed\": 0\n" +
        "    },\n" +
        "    \"hits\": {\n" +
        "        \"total\": 6,\n" +
        "        \"max_score\": 0,\n" +
        "        \"hits\": []\n" +
        "    },\n" +
        "    \"aggregations\": {\n" +
        "        \"1\": {\n" +
        "            \"0\": {\n" +
        "                \"value\": 3\n" +
        "            },\n" +
        "            \"doc_count\": 4\n" +
        "        },\n" +
        "        \"2\": {\n" +
        "            \"0\": {\n" +
        "                \"value\": 3\n" +
        "            },\n" +
        "            \"doc_count\": 5\n" +
        "        },\n" +
        "        \"total\": {\n" +
        "            \"0\": {\n" +
        "                \"value\": 2\n" +
        "            },\n" +
        "            \"doc_count\": 3\n" +
        "        }\n" +
        "    }\n" +
        "}");

    EsResultConverter converter = new EsResultConverter();
    Search.QueryResultDto dtoResult = converter.convert(jsonResult);

    validateCardinalityQueryResultDto(dtoResult);
  }

  @Test
  public void test_convert_percentiles_result() throws Exception {
    JSONObject jsonResult = new JSONObject("{\n" +
        "    \"took\": 12,\n" +
        "    \"timed_out\": false,\n" +
        "    \"_shards\": {\n" +
        "        \"total\": 5,\n" +
        "        \"successful\": 5,\n" +
        "        \"failed\": 0\n" +
        "    },\n" +
        "    \"hits\": {\n" +
        "        \"total\": 6,\n" +
        "        \"max_score\": 0,\n" +
        "        \"hits\": []\n" +
        "    },\n" +
        "    \"aggregations\": {\n" +
        "        \"1\": {\n" +
        "            \"0\": {\n" +
        "                \"values\": {\n" +
        "                    \"1.0\": 16,\n" +
        "                    \"5.0\": 16,\n" +
        "                    \"25.0\": 16,\n" +
        "                    \"50.0\": 16,\n" +
        "                    \"75.0\": 17.5,\n" +
        "                    \"95.0\": 21.099999999999998,\n" +
        "                    \"99.0\": 21.82\n" +
        "                }\n" +
        "            },\n" +
        "            \"doc_count\": 4\n" +
        "        },\n" +
        "        \"2\": {\n" +
        "            \"0\": {\n" +
        "                \"values\": {\n" +
        "                    \"1.0\": 16,\n" +
        "                    \"5.0\": 16,\n" +
        "                    \"25.0\": 16,\n" +
        "                    \"50.0\": 16,\n" +
        "                    \"75.0\": 16,\n" +
        "                    \"95.0\": 16,\n" +
        "                    \"99.0\": 16\n" +
        "                }\n" +
        "            },\n" +
        "            \"doc_count\": 5\n" +
        "        },\n" +
        "        \"total\": {\n" +
        "            \"0\": {\n" +
        "                \"values\": {\n" +
        "                    \"1.0\": 16,\n" +
        "                    \"5.0\": 16,\n" +
        "                    \"25.0\": 16,\n" +
        "                    \"50.0\": 16,\n" +
        "                    \"75.0\": 16,\n" +
        "                    \"95.0\": 16,\n" +
        "                    \"99.0\": 16\n" +
        "                }\n" +
        "            },\n" +
        "            \"doc_count\": 3\n" +
        "        }\n" +
        "    }\n" +
        "}");

    EsResultConverter converter = new EsResultConverter();
    Search.QueryResultDto dtoResult = converter.convert(jsonResult);

    validatePercentilesQueryResultDto(dtoResult);
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
    assertThat(dtoFacetResult.getFacet()).isEqualTo("0");

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
    assertThat(dtoFacetResult.getFacet()).isEqualTo("0");

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

  private void validateCardinalityQueryResultDto(Search.QueryResultDto dtoResult) {
    assertThat(dtoResult).isNotNull();

    Search.FacetResultDto dtoFacetResult = dtoResult.getFacets(0);
    assertThat(dtoFacetResult).isNotNull();
    assertThat(dtoFacetResult.getFacet()).isEqualTo("1");

    List<Search.FacetResultDto.TermFrequencyResultDto> listTermDto = dtoFacetResult.getFrequenciesList();
    assertThat(listTermDto).isNotNull();
    assertThat(listTermDto).hasSize(0);

    List<Search.FacetResultDto.ValueResultDto> valuesDto = dtoFacetResult.getValuesList();
    assertThat(valuesDto).isNotNull();
    assertThat(valuesDto).hasSize(1);

    Search.FacetResultDto.StatisticalResultDto statistics = dtoFacetResult.getStatistics();
    assertThat(statistics.hasCount()).isFalse();
  }

  private void validateMissingQueryResultDto(Search.QueryResultDto dtoResult) {
    assertThat(dtoResult).isNotNull();

    Search.FacetResultDto dtoFacetResult = dtoResult.getFacets(0);
    assertThat(dtoFacetResult).isNotNull();
    assertThat(dtoFacetResult.getFacet()).isEqualTo("1");

    List<Search.FacetResultDto.TermFrequencyResultDto> listTermDto = dtoFacetResult.getFrequenciesList();
    assertThat(listTermDto).isNotNull();
    assertThat(listTermDto).hasSize(0);

    List<Search.FacetResultDto.ValueResultDto> valuesDto = dtoFacetResult.getValuesList();
    assertThat(valuesDto).isNotNull();
    assertThat(valuesDto).hasSize(1);

    Search.FacetResultDto.StatisticalResultDto statistics = dtoFacetResult.getStatistics();
    assertThat(statistics.hasCount()).isFalse();
  }

  private void validatePercentilesQueryResultDto(Search.QueryResultDto dtoResult) {
    assertThat(dtoResult).isNotNull();

    Search.FacetResultDto dtoFacetResult = dtoResult.getFacets(0);
    assertThat(dtoFacetResult).isNotNull();
    assertThat(dtoFacetResult.getFacet()).isEqualTo("1");

    List<Search.FacetResultDto.TermFrequencyResultDto> listTermDto = dtoFacetResult.getFrequenciesList();
    assertThat(listTermDto).isNotNull();
    assertThat(listTermDto).hasSize(0);

    List<Search.FacetResultDto.ValueResultDto> valuesDto = dtoFacetResult.getValuesList();
    assertThat(valuesDto).isNotNull();
    assertThat(valuesDto).hasSize(7);

    Search.FacetResultDto.StatisticalResultDto statistics = dtoFacetResult.getStatistics();
    assertThat(statistics.hasCount()).isFalse();
  }
}
