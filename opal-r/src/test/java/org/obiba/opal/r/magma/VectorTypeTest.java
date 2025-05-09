package org.obiba.opal.r.magma;

import org.fest.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.obiba.opal.r.magma.util.*;

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

  @Test
  public void findDateRangeTest() {
    Range res = new DateRange(Lists.newArrayList("1871-01-29", "1918-11-11", "1945-05-08"));
    Assert.assertEquals("na_values=[1871-01-29, 1918-11-11, 1945-05-08]", res.toString());

    res = new DateRange(Lists.newArrayList("1945-05-08", "1871-01-29", "1918-11-11"));
    Assert.assertEquals("na_values=[1871-01-29, 1918-11-11, 1945-05-08]", res.toString());

    res = new DateRange(Lists.newArrayList("1918 11 11", "1871/01/29", "08.05.1945"));
    Assert.assertEquals("na_values=[08.05.1945, 1871/01/29, 1918 11 11]", res.toString());
  }

  @Test
  public void findDateTimeRangeTest() {
    Range res = new DateTimeRange(Lists.newArrayList("1871-01-29 10:11:12", "1918-11-11 11:11:11", "1945-05-08 22:12:24"));
    Assert.assertEquals("na_values=[1871-01-29 10:11:12, 1918-11-11 11:11:11, 1945-05-08 22:12:24]", res.toString());

    res = new DateTimeRange(Lists.newArrayList("1945-05-08", "1871-01-29 10:11:12", "1918-11-11"));
    Assert.assertEquals("na_values=[1871-01-29 10:11:12, 1918-11-11, 1945-05-08]", res.toString());

    res = new DateTimeRange(Lists.newArrayList("1918 11 11", "1871/01/29 10:11:12", "08.05.1945"));
    Assert.assertEquals("na_values=[1871/01/29 10:11:12, 1918 11 11]", res.toString());
  }
}
