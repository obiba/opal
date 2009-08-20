package org.obiba.opal.jdbcmart.batch;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liquibase.database.Database;
import liquibase.database.sql.visitor.SqlVisitor;

public class MySQLSchemaChangeSqlVisitor implements SqlVisitor {
  //
  // Constants
  //

  public static final String CREATE_INNODB_TABLE_SUFFIX = " ENGINE=InnoDB DEFAULT CHARSET=latin1";

  public static final String CREATE_MYISAM_TABLE_SUFFIX = " ENGINE=MyISAM DEFAULT CHARSET=latin1";

  public static final int MAX_INNODB_COLUMNS = 1000;

  private static final String MART_COLUMN_PREFIX = "OPAL_";

  //
  // SqlVisitor Methods
  //

  public String getTagName() {
    return "mysqlSchemaChangeSqlVisitor";
  }

  @SuppressWarnings("unchecked")
  public void setApplicableDbms(Collection applicableDbms) {
    // no-op
  }

  public boolean isApplicable(Database database) {
    return database.getTypeName().equals("mysql");
  }

  public String modifySql(String sql, Database database) {
    if(sql.toUpperCase().startsWith("CREATE TABLE")) {
      int columnCount = getColumnCount(sql);

      if(columnCount > MAX_INNODB_COLUMNS) {
        sql += CREATE_MYISAM_TABLE_SUFFIX;
      } else {
        sql += CREATE_INNODB_TABLE_SUFFIX;
      }
    }

    return sql;
  }

  //
  // Methods
  //

  /**
   * Returns the number of columns in the specified table.
   * 
   * @param createTableSql table creation SQL
   * @return columns in the table (1 entity_id column + N columns named "OPAL_*")
   */
  public int getColumnCount(String createTableSql) {
    Pattern p = Pattern.compile(MART_COLUMN_PREFIX);
    Matcher m = p.matcher(createTableSql);

    // Start columnCount at 1 (there is at least the entity_id column).
    int columnCount = 1;

    while(m.find()) {
      columnCount++;
    }

    return columnCount;
  }
}