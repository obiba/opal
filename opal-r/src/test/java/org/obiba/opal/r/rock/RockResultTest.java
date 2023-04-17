/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.rock;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.obiba.opal.spi.r.RNamedList;
import org.obiba.opal.spi.r.RServerResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class RockResultTest {

  @Test
  public void byteArrayTest() {
    RockResult result = new RockResult("abc".getBytes(StandardCharsets.UTF_8));
    Assert.assertTrue(result.isRaw());
    byte[] values = result.asBytes();
    Assert.assertEquals(3, values.length);
    Assert.assertEquals("abc", new String(values));
  }

  @Test
  public void logicalTest() {
    RockResult result = new RockResult("true");
    boolean value = result.asLogical();
    Assert.assertTrue(value);

    result = new RockResult("[true, false]");
    value = result.asLogical();
    Assert.assertTrue(value);

    result = new RockResult("{ value: true }");
    value = result.asLogical();
    Assert.assertTrue(value);

    result = new RockResult(true);
    value = result.asLogical();
    Assert.assertTrue(value);
  }

  @Test
  public void nativeStringsTest() {
    RockResult result = new RockResult("a");
    Assert.assertTrue(result.isString());
    String[] values = result.asStrings();
    Assert.assertEquals(1, values.length);
    Assert.assertEquals("a", values[0]);

    result = new RockResult(1);
    Assert.assertTrue(result.isString());
    values = result.asStrings();
    Assert.assertEquals(1, values.length);
    Assert.assertEquals("1", values[0]);
  }

  @Test
  public void jsonArrayStringsTest() {
    RockResult result = new RockResult("[\"a\", \"b\", \"c\"]");
    Assert.assertTrue(result.isString());
    String[] values = result.asStrings();
    Assert.assertEquals(3, values.length);
    Assert.assertEquals("a", values[0]);
    Assert.assertEquals("b", values[1]);
    Assert.assertEquals("c", values[2]);
  }

  @Test
  public void jsonArrayIntegersTest() {
    RockResult result = new RockResult("[1, 2, 3]");
    Assert.assertTrue(result.isInteger());
    int[] values = result.asIntegers();
    Assert.assertEquals(3, values.length);
    Assert.assertEquals(1, values[0]);
    Assert.assertEquals(2, values[1]);
    Assert.assertEquals(3, values[2]);
  }

  @Test
  public void jsonArrayNumericTest() {
    RockResult result = new RockResult("[1.1, 2.2, 3.3]");
    Assert.assertTrue(result.isNumeric());
    double[] values = result.asDoubles();
    Assert.assertEquals(3, values.length);
    Assert.assertEquals(1.1, values[0], 0.01);
    Assert.assertEquals(2.2, values[1], 0.01);
    Assert.assertEquals(3.3, values[2], 0.01);
  }

  @Test
  public void jsonArrayStringTest() {
    RockResult result = new RockResult("[\"01\", \"02\", \"03\"]");
    Assert.assertFalse(result.isNumeric());
    Assert.assertTrue(result.isString());
    String[] values = result.asStrings();
    Assert.assertEquals(3, values.length);
    Assert.assertEquals("01", values[0]);
    Assert.assertEquals("02", values[1]);
    Assert.assertEquals("03", values[2]);
  }

  @Test
  public void jsonObjectNamedListTest() {
    RockResult result = new RockResult("{ string: \"a\", integer: 1, numeric: 2.2, list: [\"b\", \"c\"], object: { data: \"d\" } }");
    Assert.assertTrue(result.isNamedList());
    String[] names = result.getNames();
    Assert.assertEquals(5, names.length);
    List<String> nList = Lists.newArrayList(names);
    Assert.assertTrue(nList.contains("string"));
    Assert.assertTrue(nList.contains("integer"));
    Assert.assertTrue(nList.contains("numeric"));
    Assert.assertTrue(nList.contains("list"));
    Assert.assertTrue(nList.contains("object"));
    RNamedList<RServerResult> list = result.asNamedList();
    Assert.assertEquals("a", list.get("string").asStrings()[0]);
    Assert.assertEquals(1, list.get("integer").asIntegers()[0]);
    Assert.assertEquals(2.2, list.get("numeric").asDoubles()[0], 0.01);
    Assert.assertTrue(list.get("list").isList());
    Assert.assertEquals(2, list.get("list").length());
    Assert.assertEquals("b", list.get("list").asStrings()[0]);
    Assert.assertEquals("c", list.get("list").asStrings()[1]);
    Assert.assertEquals(1, list.get("object").length());
    Assert.assertTrue(list.get("object").isNamedList());
    Assert.assertEquals("d", list.get("object").asNamedList().get("data").asStrings()[0]);
  }

  @Test
  public void jsonObjectNativeObjectTest() {
    RockResult result = new RockResult("{ string: \"a\", integer: 1, numeric: 2.2, list: [\"b\", \"c\"], object: { data: \"d\" } }");
    Object obj = result.asNativeJavaObject();
    Assert.assertTrue(obj instanceof Map);
    Map<String, Object> map = (Map<String, Object>) obj;
    Assert.assertTrue(map.get("string") instanceof String);
    Assert.assertTrue(map.get("integer") instanceof Integer);
    Assert.assertTrue(map.get("numeric") instanceof BigDecimal);
    Assert.assertTrue(map.get("list") instanceof List);
    Assert.assertTrue(map.get("object") instanceof Map);
  }
}
