package org.obiba.opal.r.magma;

import org.fest.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.obiba.opal.r.magma.util.DoubleRange;
import org.obiba.opal.r.magma.util.IntegerRange;
import org.obiba.opal.r.magma.util.NumberRange;

public class VectorTypeTest {

  @Test
  public void findDoubleRangeTest() {
    NumberRange res = new DoubleRange(Lists.newArrayList("1", "2", "3", "1000", "2000"), Lists.newArrayList("1", "2", "3", "2000"));
    Assert.assertEquals("na_range=[1.0, 2.0, 3.0] na_values=[2000.0]", res.toString());

    res = new DoubleRange(Lists.newArrayList("1", "2", "3", "1000", "2000", "3000"), Lists.newArrayList("1", "2", "1000", "2000", "3000"));
    Assert.assertEquals("na_range=[1000.0, 2000.0, 3000.0] na_values=[1.0, 2.0]", res.toString());

    res = new DoubleRange(Lists.newArrayList("1", "2", "3", "1000.123456789", "2000", "3000.4534", "4000"), Lists.newArrayList( "1000.123456789", "2000", "3000.4534", "4000"));
    Assert.assertEquals("na_range=[1000.123456789, 2000.0, 3000.4534, 4000.0] na_values=[]", res.toString());

    res = new DoubleRange(Lists.newArrayList("1", "2", "3", "a", "b", "c", "999"), Lists.newArrayList( "a", "b", "c", "999"));
    Assert.assertEquals("na_range=[999.0] na_values=[]", res.toString());
  }

  @Test
  public void findIntegerRangeTest() {
    NumberRange res = new IntegerRange(Lists.newArrayList("1", "2", "3", "1000", "2000"), Lists.newArrayList("1", "2", "3", "2000"));
    Assert.assertEquals("na_range=[1, 2, 3] na_values=[2000]", res.toString());

    res = new IntegerRange(Lists.newArrayList("1", "2", "3", "1000", "2000", "3000"), Lists.newArrayList("1", "2", "1000", "2000", "3000"));
    Assert.assertEquals("na_range=[1000, 2000, 3000] na_values=[1, 2]", res.toString());

    res = new IntegerRange(Lists.newArrayList("1", "2", "3", "1000", "2000", "3000", "4000"), Lists.newArrayList("1000", "2000", "3000", "4000"));
    Assert.assertEquals("na_range=[1000, 2000, 3000, 4000] na_values=[]", res.toString());

    res = new IntegerRange(Lists.newArrayList("1", "2", "3", "1000.123456789", "2000"), Lists.newArrayList("1000.123456789", "2000"));
    Assert.assertEquals("na_range=[2000] na_values=[]", res.toString());
  }

}
