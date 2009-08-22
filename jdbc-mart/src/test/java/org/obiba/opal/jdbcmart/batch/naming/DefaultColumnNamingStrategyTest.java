package org.obiba.opal.jdbcmart.batch.naming;

import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.obiba.opal.elmo.concepts.DataItem;
import org.obiba.opal.sesame.report.DataItemSet;

public class DefaultColumnNamingStrategyTest {

  @Test
  public void testFirstLevelConflict() {
    DefaultColumnNamingStrategy s = new DefaultColumnNamingStrategy();

    DataItemSet set = EasyMock.createMock(DataItemSet.class);
    DataItem item1 = EasyMock.createMock(DataItem.class);
    DataItem item2 = EasyMock.createMock(DataItem.class);
    
    EasyMock.expect(set.getDataItems()).andReturn(makeSet(item1, item2)).anyTimes();
    EasyMock.expect(item1.getIdentifier()).andReturn("1").anyTimes();
    EasyMock.expect(item1.getName()).andReturn("Item1.name").anyTimes();
    EasyMock.expect(item2.getIdentifier()).andReturn("2").anyTimes();
    EasyMock.expect(item2.getName()).andReturn("Item2.name").anyTimes();
    EasyMock.replay(set, item1, item2);
    s.prepare(set);
    Assert.assertEquals("Item1.name", s.getColumnName(item1));
    Assert.assertEquals("Item2.name", s.getColumnName(item2));
  }
  

//  @Test
  public void testSecondLevel() {
    DefaultColumnNamingStrategy s = new DefaultColumnNamingStrategy();

    DataItemSet set = EasyMock.createMock(DataItemSet.class);
    DataItem item1 = EasyMock.createMock(DataItem.class);
    DataItem item2 = EasyMock.createMock(DataItem.class);
    DataItem item3 = EasyMock.createMock(DataItem.class);
    
    EasyMock.expect(set.getDataItems()).andReturn(makeSet(item1, item2)).anyTimes();
    EasyMock.expect(item1.getIdentifier()).andReturn("1").anyTimes();
    EasyMock.expect(item1.getName()).andReturn("Item1.name.name").anyTimes();
    EasyMock.expect(item2.getIdentifier()).andReturn("2").anyTimes();
    EasyMock.expect(item2.getName()).andReturn("Item2.name.name").anyTimes();
    EasyMock.expect(item3.getIdentifier()).andReturn("3").anyTimes();
    EasyMock.expect(item3.getName()).andReturn("Item3.name.name").anyTimes();
    EasyMock.replay(set, item1, item2, item3);
    s.prepare(set);
    Assert.assertEquals("Item1.name.name", s.getColumnName(item1));
    Assert.assertEquals("Item2.name.name", s.getColumnName(item2));
    Assert.assertEquals("Item3.name.name", s.getColumnName(item3));
  }
  protected Set<DataItem> makeSet(DataItem ... items ) {
    Set<DataItem> itemSet = new HashSet<DataItem>();
    for(DataItem item : items) {
      itemSet.add(item);
    }
    return itemSet;
  }

}
