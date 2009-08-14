package org.obiba.opal.jdbcmart.batch;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.CreateTableChange;

import org.junit.Test;
import org.obiba.opal.core.domain.metadata.DataItem;
import org.obiba.opal.core.domain.metadata.DataItemSet;

public class DataItemSetToSchemaChangeProcessorTest {

  //
  // Test Methods
  //
  
  @Test
  public void testProcess() throws Exception {
    DataItemSetToSchemaChangeProcessor processor = new DataItemSetToSchemaChangeProcessor();
    
    DataItemSet dataItemSet = new DataItemSet("test");
    dataItemSet.setDataItems(createDataItems());
    
    // Basic verification.
    Change schemaChange = processor.process(dataItemSet);
    assertNotNull(schemaChange);
    assertTrue(schemaChange instanceof CreateTableChange);
        
    // Verify the table name.
    CreateTableChange createTableChange = (CreateTableChange)schemaChange;
    assertEquals(dataItemSet.getName(), createTableChange.getTableName());
    
    // Verify table columns.
    for (ColumnConfig column : createTableChange.getColumns()) {
      String columnName = column.getName();
      
      if (column.getConstraints() == null || !column.getConstraints().isPrimaryKey()) {
        DataItem dataItem = getDataItemByCode(dataItemSet.getDataItems(), Long.valueOf(columnName.substring(DataItemSetToSchemaChangeProcessor.COLUMN_NAME_PREFIX.length())));
        if (dataItem == null) {
          fail("Unexpected column '"+columnName+"'");
        }
        
        assertEquals(column.getType(), getColumnTypeFor(dataItem));
      }
    }
  }
  
  //
  // Helper Methods
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
  
  private DataItem getDataItemByCode(Set<DataItem> dataItems, Long code) {
    for (DataItem dataItem : dataItems) {
      if (dataItem.getCode().equals(code)) {
        return dataItem;
      }
    }
    
    return null;
  }
  
  private String getColumnTypeFor(DataItem dataItem) {
    return "INTEGER";
  }
}
