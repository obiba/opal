package org.obiba.opal.jdbcmart.batch;

import java.util.HashMap;
import java.util.Map;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.CreateTableChange;

import org.obiba.opal.core.domain.metadata.DataItem;
import org.obiba.opal.core.domain.metadata.DataItemSet;
import org.springframework.batch.item.ItemProcessor;

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

    return schemaChange;
  }

  //
  // Methods
  //

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

    schemaChange.addColumn(column);
  }

  private String getTypeForDataItem(DataItem dataItem) {
    // TODO: DataItem does not have a type. The actual item class to be used
    // (SemanticDataItem?) *will* have a type. Specify INTEGER now for testing.
    String dataItemType = "INTEGER";

    String type = typeMap.get(dataItemType);

    return (type != null) ? type : dataItemType;
  }
}
