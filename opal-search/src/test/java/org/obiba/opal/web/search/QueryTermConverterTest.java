/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import java.util.Iterator;

import javax.annotation.Nonnull;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.web.model.Search;

import junit.framework.Assert;

/**
 * Created with IntelliJ IDEA.
 * User: rhaeri
 * Date: 26/11/12
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryTermConverterTest {
  private Search.QueryTermDto dtoCategoricalQuery;

  private Search.QueryTermDto dtoStatisticalQuery;

  @Before
  public void setUp() throws Exception {
    dtoCategoricalQuery = createCategoricalQueryDto();
    dtoStatisticalQuery = createStatisticalQueryDto();
  }

  @Test
  public void testConvert_ValidCategoricalResultJson() throws Exception {
    QueryTermConverter converter = new QueryTermConverter("opal-data.cipreliminaryquestionnaire");

    JSONObject jsonExpected = new JSONObject(
        "{\"query\":{\"match_all\":{} }, \"size\":0, " + "\"facets\":{\"0\":{\"terms\":{\"field\":\"opal-data.cipreliminaryquestionnaire:LAST_MEAL_WHEN\" } } } }");

    JSONObject jsonResult = converter.convert(dtoCategoricalQuery);
    Assert.assertNotNull(jsonResult);
    new JsonAssert().assertEquals(jsonExpected, jsonResult);
    new JsonAssert().assertEquals(jsonExpected, jsonResult);

  }

  @Test
  public void testConvert_ValidStatisticalResultJson() throws Exception {
    QueryTermConverter converter = new QueryTermConverter("opal-data.standingheight");

    JSONObject jsonExpected = new JSONObject(
        "{\"query\":{\"match_all\":{} }, \"size\":0, \"facets\":{\"0\":{\"statistical\":{\"field\":\"opal-data.standingheight:RES_FIRST_HEIGHT\"} } } }");

    JSONObject jsonResult = converter.convert(dtoStatisticalQuery);
    Assert.assertNotNull(jsonResult);
    new JsonAssert().assertEquals(jsonExpected, jsonResult);
  }

  private Search.QueryTermDto createCategoricalQueryDto() {
    Search.QueryTermDto.Builder dtoBuilder = Search.QueryTermDto.newBuilder().setFacet("0");

    Search.VariableTermDto.Builder variableDto = Search.VariableTermDto.newBuilder();
    variableDto.setVariable("LAST_MEAL_WHEN");
    variableDto.setExtension(Search.InTermDto.params, Search.InTermDto.newBuilder().build());

    dtoBuilder.setExtension(Search.VariableTermDto.params, variableDto.build());

    return dtoBuilder.build();
  }

  private Search.QueryTermDto createStatisticalQueryDto() {
    Search.QueryTermDto.Builder dtoBuilder = Search.QueryTermDto.newBuilder().setFacet("0");

    Search.VariableTermDto.Builder variableDto = Search.VariableTermDto.newBuilder();
    variableDto.setVariable("RES_FIRST_HEIGHT");
    variableDto.setExtension(Search.RangeTermDto.params, Search.RangeTermDto.newBuilder().build());

    dtoBuilder.setExtension(Search.VariableTermDto.params, variableDto.build());

    return dtoBuilder.build();
  }

  private static class JsonAssert {

    JsonAssert() {
    }

    @SuppressWarnings("unchecked")
    public void assertEquals(@Nonnull JSONObject expected, @Nonnull JSONObject target) {

      try {

        for(Iterator<String> iterator = expected.keys(); iterator.hasNext(); ) {
          String key = iterator.next();

          // match the key names
          Assert.assertNotNull(target.get(key));

          Object expectedValue = expected.get(key);
          Object targetValue = target.get(key);

          // match the value class types
          Assert.assertEquals(expectedValue.getClass(), targetValue.getClass());

          if(expectedValue instanceof JSONObject) {
            assertEquals(expected.getJSONObject(key), target.getJSONObject(key));
          } else if(expectedValue instanceof JSONArray) {
            // TODO handle JSONArray in the future
            Assert.assertFalse(true);
          } else {
            Assert.assertEquals(expectedValue, targetValue);
          }
        }

      } catch(JSONException e) {
        Assert.assertFalse(true);
      }
    }

  }

}
