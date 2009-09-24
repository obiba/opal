package org.obiba.opal.jdbcmart.batch;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import liquibase.change.Change;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.CreateIndexChange;
import liquibase.change.CreateTableChange;
import liquibase.change.EmptyChange;
import liquibase.change.InsertDataChange;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.Table;
import liquibase.exception.JDBCException;

import org.obiba.opal.elmo.concepts.CategoricalItem;
import org.obiba.opal.elmo.concepts.Category;
import org.obiba.opal.elmo.concepts.DataItem;
import org.obiba.opal.elmo.concepts.MissingCategory;
import org.obiba.opal.jdbcmart.batch.naming.DefaultColumnNamingStrategy;
import org.obiba.opal.sesame.report.DataItemSet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;

public class DataItemSetToSchemaChangeProcessor implements ItemStream, ItemProcessor<DataItemSet, Change> {
  //
  // Constants
  //

  private static final String ENTITY_KEY_TYPE = "varchar(200)";

  private static final String OCCURRENCE_COLUMN_TYPE = "BIGINT";

  private static final String VARIABLES_METADATA_TABLE_NAME = "variables";

  private static final String CATEGORIES_METADATA_TABLE_NAME = "categories";

  //
  // Instance Variables
  //

  private DataSource dataSource;

  private IColumnNamingStrategy columnNamingStrategy = new DefaultColumnNamingStrategy();

  /**
   * Mapping from <code>DataItem</code> types to LiquiBase vendor-neutral column types.
   */
  private Map<String, String> typeMap;

  private Connection connection;

  private DatabaseSnapshot databaseSnapshot;

  //
  // Constructors
  //

  public DataItemSetToSchemaChangeProcessor() {
    typeMap = new HashMap<String, String>();
  }

  public void setColumnNamingStrategy(IColumnNamingStrategy columnNamingStrategy) {
    this.columnNamingStrategy = columnNamingStrategy;
  }

  //
  // ItemStream Methods
  //

  public void open(ExecutionContext executionContext) throws ItemStreamException {
    // Get a connection to the dataSource.
    try {
      connection = dataSource.getConnection();
    } catch(SQLException ex) {
      throw new ItemStreamException("Could not acquire a connection to the dataSource", ex);
    }
  }

  public void update(ExecutionContext executionContext) throws ItemStreamException {

  }

  public void close() throws ItemStreamException {
    // Close the connection to the dataSource.
    if(connection != null) {
      try {
        connection.close();
      } catch(SQLException ex) {
        throw new ItemStreamException("Could not close connection to the dataSource", ex);
      }
    }
  }

  //
  // ItemProcessor Methods
  //

  public Change process(DataItemSet dataItemSet) throws Exception {

    columnNamingStrategy.prepare(dataItemSet);

    CompositeChange composite = new CompositeChange();

    // Create the metadata table if necessary
    if(doesTableExist(VARIABLES_METADATA_TABLE_NAME) == false) {
      composite.addChange(doCreateVariablesMetaDataTableChange(dataItemSet));
    }
    if(doesTableExist(CATEGORIES_METADATA_TABLE_NAME) == false) {
      composite.addChange(doCreateCategoriesMetaDataTableChange(dataItemSet));
    }

    if(doesTableExist(dataItemSet.getName()) == false) {
      // Insert metadata
      composite.addChange(doInsertVariablesMetaDataTableChange(dataItemSet));
      composite.addChange(doInsertCategoriesMetaDataTableChange(dataItemSet));
      composite.addChange(doCreateTableChange(dataItemSet));
      composite.addChange(doCreateEntityIndex(dataItemSet));
    }

    // Delete the snapshot since we're about to change the schema
    databaseSnapshot = null;

    return composite;
  }

  //
  // Methods
  //

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setTypeMap(Map<String, String> typeMap) {
    this.typeMap.clear();

    if(typeMap != null) {
      this.typeMap.putAll(typeMap);
    }
  }

  protected Change doCreateVariablesMetaDataTableChange(DataItemSet dataItemSet) {
    CreateTableChange schemaChange = new CreateTableChange();
    schemaChange.setTableName(VARIABLES_METADATA_TABLE_NAME);

    ColumnConfig column = new ColumnConfig();
    column.setName("report");
    column.setType("varchar(255)");
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("variable");
    column.setType("varchar(255)");
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("name");
    column.setType("varchar(255)");
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("unit");
    column.setType("varchar(255)");
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("label");
    column.setType("varchar(2000)");
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("dataType");
    column.setType("varchar(255)");
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("multiple");
    column.setType("BOOLEAN");
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("repeatable");
    column.setType("BOOLEAN");
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("categorical");
    column.setType("BOOLEAN");
    schemaChange.addColumn(column);

    return schemaChange;
  }

  protected Change doCreateCategoriesMetaDataTableChange(DataItemSet dataItemSet) {
    CreateTableChange schemaChange = new CreateTableChange();
    schemaChange.setTableName(CATEGORIES_METADATA_TABLE_NAME);

    ColumnConfig column = new ColumnConfig();
    column.setName("report");
    column.setType("varchar(255)");
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("variable");
    column.setType("varchar(255)");
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("name");
    column.setType("varchar(255)");
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("label");
    column.setType("varchar(2000)");
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("code");
    column.setType("INT");
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("missing");
    column.setType("BOOLEAN");
    schemaChange.addColumn(column);

    return schemaChange;
  }

  protected Change doInsertVariablesMetaDataTableChange(DataItemSet dataItemSet) {
    CompositeChange composite = new CompositeChange();

    for(DataItem dataItem : dataItemSet.getDataItems()) {
      InsertDataChange schemaChange = new InsertDataChange();
      schemaChange.setTableName(VARIABLES_METADATA_TABLE_NAME);
      addRowForDataItem(schemaChange, dataItemSet, dataItem);
      composite.addChange(schemaChange);
    }

    return composite;
  }

  protected Change doInsertCategoriesMetaDataTableChange(DataItemSet dataItemSet) {
    CompositeChange composite = new CompositeChange();

    for(DataItem dataItem : dataItemSet.getDataItems()) {
      if(dataItem instanceof CategoricalItem) {
        for(Category category : ((CategoricalItem) dataItem).getCategories()) {
          InsertDataChange schemaChange = new InsertDataChange();
          schemaChange.setTableName(CATEGORIES_METADATA_TABLE_NAME);
          addRowForCategory(schemaChange, dataItemSet, (CategoricalItem) dataItem, category);
          composite.addChange(schemaChange);
        }
      }
    }

    return composite;
  }

  protected void addRowForCategory(InsertDataChange schemaChange, DataItemSet set, CategoricalItem categoricalItem, Category category) {
    ColumnConfig column = new ColumnConfig();
    column.setName("report");
    column.setValue(set.getName());
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("variable");
    column.setValue(columnNamingStrategy.getColumnName(categoricalItem));
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("name");
    column.setValue(category.getShortName());
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("label");
    column.setValue(category.getRdfsLabel());
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("code");
    column.setValueNumeric(category.getCode());
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("missing");
    column.setValueBoolean(category instanceof MissingCategory);
    schemaChange.addColumn(column);
  }

  protected void addRowForDataItem(InsertDataChange schemaChange, DataItemSet set, DataItem dataItem) {
    ColumnConfig column = new ColumnConfig();
    column.setName("report");
    column.setValue(set.getName());
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("variable");
    column.setValue(columnNamingStrategy.getColumnName(dataItem));
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("name");
    column.setValue(dataItem.getName());
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("unit");
    column.setValue(dataItem.getUnit());
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("label");
    column.setValue(dataItem.getRdfsLabel());
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("dataType");
    column.setValue(dataItem.getDataType());
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("multiple");
    column.setValueBoolean(dataItem.isMultiple());
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("repeatable");
    column.setValueBoolean(dataItem.isRepeatable());
    schemaChange.addColumn(column);

    column = new ColumnConfig();
    column.setName("categorical");
    column.setValueBoolean(dataItem instanceof CategoricalItem);
    schemaChange.addColumn(column);
  }

  protected Change doCreateTableChange(DataItemSet dataItemSet) {
    CreateTableChange schemaChange = new CreateTableChange();
    schemaChange.setTableName(dataItemSet.getName());

    addEntityKeyColumn(schemaChange);
    if(dataItemSet.hasOccurrence()) {
      addOccurrenceColumn(schemaChange);
    }

    for(DataItem dataItem : dataItemSet.getDataItems()) {
      addColumnForDataItem(schemaChange, dataItem);
    }

    return validate(schemaChange) ? schemaChange : new EmptyChange();
  }

  private CreateIndexChange doCreateEntityIndex(DataItemSet dataItemSet) {
    CreateIndexChange cic = new CreateIndexChange();
    cic.setIndexName("entity");
    cic.setTableName(dataItemSet.getName());
    cic.setUnique(true);
    addEntityKeyColumn(cic);
    if(dataItemSet.hasOccurrence()) {
      addOccurrenceColumn(cic);
    }
    return cic;
  }

  private void addEntityKeyColumn(ChangeWithColumns schemaChange) {
    ColumnConfig primaryKey = new ColumnConfig();
    primaryKey.setName(SchemaChangeConstants.ENTITY_KEY_NAME);
    primaryKey.setType(ENTITY_KEY_TYPE);

    ConstraintsConfig constraints = new ConstraintsConfig();
    constraints.setNullable(false);
    primaryKey.setConstraints(constraints);

    schemaChange.addColumn(primaryKey);
  }

  private void addOccurrenceColumn(ChangeWithColumns schemaChange) {
    ColumnConfig occurrence = new ColumnConfig();

    occurrence.setName(SchemaChangeConstants.OCCURRENCE_COLUMN_NAME);
    occurrence.setType(OCCURRENCE_COLUMN_TYPE);

    ConstraintsConfig constraints = new ConstraintsConfig();
    constraints.setNullable(false);
    occurrence.setConstraints(constraints);

    schemaChange.addColumn(occurrence);
  }

  private void addColumnForDataItem(CreateTableChange schemaChange, DataItem dataItem) {
    if(dataItem.getDataType() == null) {
      return;
    }
    ColumnConfig column = new ColumnConfig();

    // Column name: Set to studyPrefix + dataItem.getCode().
    column.setName(columnNamingStrategy.getColumnName(dataItem));

    // Column type: From the DataItem's metadata.
    column.setType(getTypeForDataItem(dataItem));

    // Column auto-increment: Set to false.
    column.setAutoIncrement(false);

    schemaChange.addColumn(column);
  }

  private String getTypeForDataItem(DataItem dataItem) {
    String dataItemType = dataItem.getDataType();

    String type = typeMap.get(dataItemType);

    return (type != null) ? type : dataItemType;
  }

  /**
   * Validates the "create table" schema change against the current database schema and indicates whether processing
   * should continue or not.
   * 
   * Processing should <i>not</i> continue if the table described by the change already exists and is different from
   * that table.
   * 
   * @param schemaChange the schema change
   * @return <code>true</code> if processing should continue
   */
  private boolean validate(CreateTableChange schemaChange) {
    // Does the table exist?
    Table table = databaseSnapshot.getTable(schemaChange.getTableName());
    if(table != null) {
      for(ColumnConfig columnConfig : schemaChange.getColumns()) {
        Column column = databaseSnapshot.getColumn(schemaChange.getTableName(), columnConfig.getName());

        // Does the column exist? If not throw an exception.
        if(column == null) {
          throw new ItemStreamException("No such column (" + columnConfig.getName() + ")");
        }

        // Do the columns match?
        ColumnConfig existingColumnConfig = new ColumnConfig(column);

        // Check types.
        String existingColumnType = existingColumnConfig.getType();
        String changeColumnType = columnConfig.getType();
        if(!existingColumnType.equals(changeColumnType)) {
          throw new ItemStreamException("Column type mismatch (" + columnConfig.getName() + ")");
        }

        // Check auto-increment setting.
        if(!existingColumnConfig.isAutoIncrement().equals(columnConfig.isAutoIncrement())) {
          throw new ItemStreamException("Column auto-increment mismatch (" + columnConfig.getName() + ")");
        }

        // TODO: Clearly more checking can/should be done here...
      }

      return false;
    }

    return true;
  }

  private boolean doesTableExist(String name) {
    if(databaseSnapshot == null) {
      databaseSnapshot = createDatabaseSnapshot();
    }
    return databaseSnapshot.getTable(name) != null;
  }

  private DatabaseSnapshot createDatabaseSnapshot() {
    DatabaseSnapshot databaseSnapshot = null;

    // Find an appropriate database instance for the connection.
    try {
      DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
      Database database = databaseFactory.findCorrectDatabaseImplementation(connection);
      databaseSnapshot = database.createDatabaseSnapshot(null, null);
    } catch(JDBCException ex) {
      throw new ItemStreamException("Could not locate a database implementation for the connection", ex);
    }

    return databaseSnapshot;
  }
}
