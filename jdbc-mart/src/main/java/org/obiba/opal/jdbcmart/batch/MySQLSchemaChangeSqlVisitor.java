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

  private static final String ENTITY_KEY_COLUMN_REGEX = "`" + SchemaChangeConstants.ENTITY_KEY_NAME + "` ";

  private static final String OCCURRENCE_COLUMN_REGEX = "`" + SchemaChangeConstants.OCCURRENCE_COLUMN_NAME + "` ";

  //
  // Instance Variables
  //

  private String studyPrefix;

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

  public void setStudyPrefix(String studyPrefix) {
    this.studyPrefix = (studyPrefix != null) ? studyPrefix : "";
  }

  /**
   * Returns the number of columns in the specified table.
   * 
   * @param createTableSql table creation SQL
   * @return columns in the table
   */
  public int getColumnCount(String createTableSql) {
    String opalColumnRegex = "`" + studyPrefix + "\\S+` ";

    String columnRegex = ENTITY_KEY_COLUMN_REGEX + "|" + OCCURRENCE_COLUMN_REGEX + "|" + opalColumnRegex;

    Pattern p = Pattern.compile(columnRegex);
    Matcher m = p.matcher(createTableSql);

    int columnCount = 0;

    while(m.find()) {
      columnCount++;
    }

    return columnCount;
  }
}