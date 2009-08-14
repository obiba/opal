package org.obiba.opal.jdbcmart.batch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obiba.opal.core.domain.metadata.DataItem;
import org.obiba.opal.core.domain.metadata.DataItemSet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class DataItemSetReader implements ItemReader<DataItemSet> {
  //
  // Instance Variables
  //
  
  private List<DataItemSet> dataItemSets;
  
  private int index;
  
  //
  // Constructors
  //
  
  public DataItemSetReader() {
    dataItemSets = new ArrayList<DataItemSet>();
    
    // Add a single DataItemSet.
    DataItemSet dataItemSet = new DataItemSet("test");
    dataItemSet.setDataItems(createDataItems());
    dataItemSets.add(dataItemSet);
  }
  
  //
  // ItemReader Methods
  //
  
  public DataItemSet read() throws Exception, UnexpectedInputException, ParseException {   
    return (index < dataItemSets.size()) ? dataItemSets.get(index++) : null;
  }
  
  //
  // Methods
  //
  
  private Set<DataItem> createDataItems() {
    Set<DataItem> dataItems = new HashSet<DataItem>();
    
    for (int i=0; i<10; i++) {
      final long code = i;
      
      DataItem dataItem = new DataItem() {
        private static final long serialVersionUID = 1L;

        public Long getCode() {
          return code;
        }
      };
      
      dataItems.add(dataItem);
    }
    
    return dataItems;
  }

}
