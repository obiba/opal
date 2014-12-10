/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search.support;

import java.util.Iterator;

import javax.validation.constraints.NotNull;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.search.ValueTableValuesIndex;
import org.obiba.opal.search.ValuesIndexManager;
import org.obiba.opal.web.model.Search;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.fest.assertions.api.Assertions.assertThat;

public class QueryTermConverterTest {

  @Before
  public void setUp() throws Exception {
    new MagmaEngine();
  }

  @After
  public void tearDown() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_convert_ValidCategoricalQueryJson() throws Exception {

    String variableName = "LAST_MEAL_WHEN";
    IndexManagerHelper indexManagerHelper = createIndexManagerHelper("opal-data", "CIPreliminaryQuestionnaire",
        "opal-data-cipreliminaryquestionnaire", variableName, createCategoricalVariable(variableName));

    QueryTermConverter converter = new QueryTermConverter(indexManagerHelper, 10);
    Search.QueryTermsDto dtoQuery = createSimpleQueryDto(variableName);

    JSONObject jsonExpected = new JSONObject("{\"query\":{\"match_all\":{} }, \"size\":0, " + //
        "\"aggregations\":{\"0\":{\"terms\":{\"field\":\"opal-data-cipreliminaryquestionnaire-LAST_MEAL_WHEN\", \"size\": 0 } } } }");

    JSONObject jsonResult = converter.convert(dtoQuery);
    assertThat(jsonResult).isNotNull();
    JsonAssert.assertEquals(jsonExpected, jsonResult);
  }

  @Test
  public void test_convert_ValidStatisticalQueryJson() throws Exception {
    String variableName = "RES_FIRST_HEIGHT";
    IndexManagerHelper indexManagerHelper = createIndexManagerHelper("opal-data", "StandingHeight",
        "opal-data-standingheight", variableName, createContinuousVariable(variableName));

    QueryTermConverter converter = new QueryTermConverter(indexManagerHelper, 10);
    Search.QueryTermsDto dtoQuery = createSimpleQueryDto(variableName);

    JSONObject jsonExpected = new JSONObject("{\"query\":{\"match_all\":{} }, \"size\":0, " + //
        "\"aggregations\":{\"0\":{\"extended_stats\":{\"field\":\"opal-data" + //
        "-standingheight-RES_FIRST_HEIGHT\"} } } }");

    JSONObject jsonResult = converter.convert(dtoQuery);
    assertThat(jsonResult).isNotNull();
    JsonAssert.assertEquals(jsonExpected, jsonResult);
  }

  @Test
  public void test_non_categorical_continuous_conversion_limit_one_thousand() throws JSONException {
    String variableName = "NON_CATEGORICAL_CONTINUOUS";
    IndexManagerHelper indexManagerHelper = createIndexManagerHelper("opal-data", "StandingHeight",
        "opal-data-StandingHeight", variableName, createNonCategoricalContinuousVariable(variableName));

    QueryTermConverter converter = new QueryTermConverter(indexManagerHelper, 1000);
    Search.QueryTermsDto dtoQuery = createSimpleQueryDto(variableName);

    JSONObject jsonExpected = new JSONObject("{\"query\":{\"match_all\":{} }, \"size\":0, " + //
        "\"aggregations\":{\"0\":{\"terms\":{\"field\":\"opal-data" + //
        "-StandingHeight-NON_CATEGORICAL_CONTINUOUS\", \"size\": 1000} } } }");

    JSONObject jsonResult = converter.convert(dtoQuery);
    assertThat(jsonResult).isNotNull();
    JsonAssert.assertEquals(jsonExpected, jsonResult);
  }

  private Search.QueryTermsDto createSimpleQueryDto(String variable) {
    Search.QueryTermDto.Builder dtoBuilder = Search.QueryTermDto.newBuilder().setFacet("0");

    Search.VariableTermDto.Builder variableDto = Search.VariableTermDto.newBuilder();
    variableDto.setVariable(variable);
    dtoBuilder.setExtension(Search.VariableTermDto.field, variableDto.build());

    return Search.QueryTermsDto.newBuilder().addQueries(dtoBuilder.build()).build();
  }

  private IndexManagerHelper createIndexManagerHelper(String datasource, String table, String indexName,
      String variableName, Variable variable) {

    ValuesIndexManager indexManager = setupMockObjects(datasource, table, indexName, variableName, variable);

    return new IndexManagerHelper(indexManager).setDatasource(datasource).setTable(table);
  }

  private ValuesIndexManager setupMockObjects(String dsName, String table, String indexName, String variableName,
      Variable variable) {
    ValuesIndexManager indexManager = createMock(ValuesIndexManager.class);
    Datasource datasource = createMockDatasource(dsName, table);

    ValueTable mockTable = datasource.getValueTable(table);
    reset(mockTable);
    ValueTableValuesIndex mockTableIndex = createMock(ValueTableValuesIndex.class);
    expect(mockTableIndex.getIndexName()).andReturn(indexName).anyTimes();
    expect(mockTableIndex.getFieldName("LAST_MEAL_WHEN")).andReturn(indexName + "-LAST_MEAL_WHEN").anyTimes();
    expect(mockTableIndex.getFieldName("RES_FIRST_HEIGHT")).andReturn(indexName + "-RES_FIRST_HEIGHT").anyTimes();
    expect(mockTableIndex.getFieldName("NON_CATEGORICAL_CONTINUOUS")).andReturn(indexName + "-NON_CATEGORICAL_CONTINUOUS").anyTimes();
    replay(mockTableIndex);

    expect(mockTable.getVariable(variableName)).andReturn(variable).anyTimes();
    expect(indexManager.getIndex(mockTable)).andReturn(mockTableIndex).anyTimes();
    replay(indexManager);
    replay(mockTable);

    MagmaEngine.get().addDatasource(datasource);

    return indexManager;
  }

  private Variable createNonCategoricalContinuousVariable(String variableName) {
    Variable.Builder builder = Variable.Builder.newVariable(variableName, PointType.get(), "dummy");

    return builder.build();
  }

  private Variable createCategoricalVariable(String variableName) {
    Variable.Builder builder = Variable.Builder.newVariable(variableName, TextType.get(), "dummy")
        .addCategories("dummy");

    return builder.build();
  }

  private Variable createContinuousVariable(String variableName) {
    Variable.Builder builder = Variable.Builder.newVariable(variableName, DecimalType.get(), "dummy");

    return builder.build();
  }

  private Datasource createMockDatasource(String dsName, String... tables) {
    Datasource mockDatasource = createMock(Datasource.class);
    mockDatasource.initialise();
    EasyMock.expectLastCall().once();
    mockDatasource.dispose();
    EasyMock.expectLastCall().once();

    expect(mockDatasource.getName()).andReturn(dsName).anyTimes();
    for(String table : tables) {
      ValueTable mockTable = createMock(ValueTable.class);
      expect(mockTable.getName()).andReturn(table).anyTimes();
      expect(mockDatasource.getValueTable(table)).andReturn(mockTable).anyTimes();
      replay(mockTable);
    }
    replay(mockDatasource);
    return mockDatasource;
  }

  /**
   * Utility class that asserts the equality of two JSON objects.
   * TODO extract the class and add the JSONArray recursion as well. For now it asserts two simple JSON objects
   */
  private static class JsonAssert {

    private JsonAssert() {
    }

    @SuppressWarnings("unchecked")
    public static void assertEquals(@NotNull JSONObject expected, @NotNull JSONObject target) throws JSONException {

      for(Iterator<String> iterator = expected.keys(); iterator.hasNext(); ) {
        String key = iterator.next();

        // match the key names
        assertThat(target.get(key)).isNotNull();

        Object expectedValue = expected.get(key);
        Object targetValue = target.get(key);

        // match the value class types
        assertThat(expectedValue).isInstanceOf(targetValue.getClass());

        if(expectedValue instanceof JSONObject) {
          // For now, recurse only the JSON object
          assertThat(expected.getJSONObject(key)).isEqualTo(target.getJSONObject(key));
        } else if(expectedValue instanceof JSONArray) {
          // TODO handle JSONArray in the future
          Assert.fail();
        } else {
          // compare values
          assertThat(expectedValue).isEqualTo(targetValue);
        }
      }

    }

  }

}
