package org.obiba.opal.jdbcmart.batch;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.CreateTableChange;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.core.JdbcTemplate;

public class SchemaChangeWriterTest {
  //
  // Instance Variables
  //
  
  private DataSource dataSource;
  
  //
  // Fixture Methods (setUp / tearDown)
  //
  
  @Before
  public void setUp() {
    ApplicationContext context = new ClassPathXmlApplicationContext("SchemaChangeWriterTest/test-context.xml");
    dataSource = (DataSource)context.getBean("dataSource");
  }
  
  //
  // Test Methods
  //
  
  @SuppressWarnings("unchecked")
  @Test
  public void testWrite() throws Exception {
    SchemaChangeWriter writer = new SchemaChangeWriter();
    writer.setDataSource(dataSource);
        
    List<CreateTableChange> items = new ArrayList<CreateTableChange>();
    addTableChange(items, "table1", "col1", "int");
    
    writer.open(null);
    writer.write(items);
    writer.close();
    
    // Execute a query to verify that the table was created. Use a select clause
    // that includes all the table columns, to verify that the necessary columns
    // were created.
    try {
      JdbcTemplate template = new JdbcTemplate(dataSource);
      List results = template.queryForList("select col1 from table1");
      assertEquals(0, results.size());
    }
    catch(InvalidDataAccessResourceUsageException ex) {
      fail("Table not created or columns missing");
    }
  }
  
  //
  // Helper Methods
  //
  
  private void addTableChange(List<CreateTableChange> items, String tableName, String... columns) {
    CreateTableChange c = new CreateTableChange();
    c.setTableName(tableName);
   
    addPrimaryKeyColumn(c);
    
    for (int i=0; i<columns.length-1; i+=2) {
      ColumnConfig column = new ColumnConfig();
      column.setName(columns[i]);
      column.setType(columns[i+1]);
      
      c.addColumn(column);
    }
    
    items.add(c);
  }
  
  private void addPrimaryKeyColumn(CreateTableChange schemaChange) {
    ColumnConfig primaryKey = new ColumnConfig();
    
    primaryKey.setName("id");
    primaryKey.setAutoIncrement(true);
    primaryKey.setType("BIGINT");
    
    ConstraintsConfig constraints = new ConstraintsConfig();
    constraints.setPrimaryKey(true);
    primaryKey.setConstraints(constraints);
    
    schemaChange.addColumn(primaryKey);
  }
}
