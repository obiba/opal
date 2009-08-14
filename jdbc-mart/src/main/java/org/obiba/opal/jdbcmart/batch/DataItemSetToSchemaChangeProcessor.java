package org.obiba.opal.jdbcmart.batch;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.CreateTableChange;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.Table;
import liquibase.exception.JDBCException;

import org.obiba.opal.core.domain.metadata.DataItem;
import org.obiba.opal.core.domain.metadata.DataItemSet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamException;

public class DataItemSetToSchemaChangeProcessor implements ItemProcessor<DataItemSet, Change> {
  //
  // Constants
  //

  public static final String COLUMN_NAME_PREFIX = "OPAL_";

  public static final String PRIMARY_KEY_NAME = "id";

  public static final String PRIMARY_KEY_TYPE = "BIGINT";

  //
  // Instance Variables
  //

  private DataSource dataSource;
  
  /**
   * Mapping from <code>DataItem</code> types to LiquiBase vendor-neutral column types.
   */
  private Map<String, String> typeMap;

  //
  // Constructors
  //

  public DataItemSetToSchemaChangeProcessor() {
    typeMap = new HashMap<String, String>();
  }

  //
  // ItemProcessor Methods
  //

  public Change process(DataItemSet dataItemSet) throws Exception {
    CreateTableChange schemaChange = new CreateTableChange();
    schemaChange.setTableName(dataItemSet.getName());

    addPrimaryKeyColumn(schemaChange);

    for(DataItem dataItem : dataItemSet.getDataItems()) {
      addColumnForDataItem(schemaChange, dataItem);
    }
    
    return validate(schemaChange) ? schemaChange : null;
  }

  //
  // Methods
  //

  public void setDataSource(DataSource dataSource)
  {
    this.dataSource = dataSource;
  }
  
  public void setTypeMap(Map<String, String> typeMap) {
    this.typeMap.clear();

    if(typeMap != null) {
      this.typeMap.putAll(typeMap);
    }
  }

  private void addPrimaryKeyColumn(CreateTableChange schemaChange) {
    ColumnConfig primaryKey = new ColumnConfig();

    primaryKey.setName(PRIMARY_KEY_NAME);
    primaryKey.setType(PRIMARY_KEY_TYPE);
    primaryKey.setAutoIncrement(true);

    ConstraintsConfig constraints = new ConstraintsConfig();
    constraints.setPrimaryKey(true);
    primaryKey.setConstraints(constraints);

    schemaChange.addColumn(primaryKey);
  }

  private void addColumnForDataItem(CreateTableChange schemaChange, DataItem dataItem) {
    ColumnConfig column = new ColumnConfig();

    // Column name: Set to COLUMN_NAME_PREFIX + dataItem.getCode().
    column.setName(COLUMN_NAME_PREFIX + dataItem.getCode().toString());

    // Column type: From the DataItem's metadata.
    column.setType(getTypeForDataItem(dataItem));

    // Column auto-increment: Set to false.
    column.setAutoIncrement(false);
    
    schemaChange.addColumn(column);
  }

  private String getTypeForDataItem(DataItem dataItem) {
    // TODO: DataItem does not have a type. The actual item class to be used
    // (SemanticDataItem?) *will* have a type. Specify INTEGER now for testing.
    String dataItemType = "INTEGER";

    String type = typeMap.get(dataItemType);

    return (type != null) ? type : dataItemType;
  }
  
  /**
   * Validates the "create table" schema change against the current database schema and 
   * indicates whether processing should continue or not.
   * 
   * Processing should <i>not</i> continue if the table described by the change already exists and
   * is different from that table.
   *
   * @param schemaChange the schema change
   * @return <code>true</code> if processing should continue
   */
  private boolean validate(CreateTableChange schemaChange) {
    DatabaseSnapshot databaseSnapshot = createDatabaseSnapshot();
    
    // Does the table exist?
    Table table = databaseSnapshot.getTable(schemaChange.getTableName());
    if (table != null) {
      for (ColumnConfig columnConfig : schemaChange.getColumns()) {
        Column column = databaseSnapshot.getColumn(schemaChange.getTableName(), columnConfig.getName());
        
        // Does the column exist? If not throw an exception.
        if (column == null) {
          throw new ItemStreamException("No such column ("+columnConfig.getName()+")");
        }
        
        // Do the columns match?
        ColumnConfig existingColumnConfig = new ColumnConfig(column);
        
        // Check types.
        String existingColumnType = existingColumnConfig.getType();
        String changeColumnType = columnConfig.getType();
        if (!existingColumnType.equals(changeColumnType)) {
          throw new ItemStreamException("Column type mismatch ("+columnConfig.getName()+")");
        }
        
        // Check auto-increment setting.
        if (!existingColumnConfig.isAutoIncrement().equals(columnConfig.isAutoIncrement())) {
          throw new ItemStreamException("Column auto-increment mismatch ("+columnConfig.getName()+")");
        }
        
        // TODO: Clearly more checking can/should be done here...
      }
      
      return false;
    }
    
    return true;
  }
  
  private DatabaseSnapshot createDatabaseSnapshot() {
    DatabaseSnapshot databaseSnapshot = null;
    
    // Get a connection to the dataSource.
    Connection connection = null;
    try {
      connection = dataSource.getConnection();  
    }
    catch(SQLException ex) {
      throw new ItemStreamException("Could not acquire a connection to the dataSource", ex);
    }
    
    // Find an appropriate database instance for the connection.
    try {
      DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
      Database database = databaseFactory.findCorrectDatabaseImplementation(connection);
      databaseSnapshot = database.createDatabaseSnapshot(null, null);
      
    }
    catch(JDBCException ex) {
      throw new ItemStreamException("Could not locate a database implementation for the connection", ex);
    }
    
    return databaseSnapshot;
  }
}
