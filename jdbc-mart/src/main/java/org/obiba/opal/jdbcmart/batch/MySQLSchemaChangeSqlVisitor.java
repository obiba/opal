package org.obiba.opal.jdbcmart.batch;

import java.util.Collection;

import liquibase.database.Database;
import liquibase.database.sql.visitor.SqlVisitor;

public class MySQLSchemaChangeSqlVisitor implements SqlVisitor {
  //
  // Constants
  //

  public static final String CREATE_TABLE_SUFFIX = " ENGINE=InnoDB DEFAULT CHARSET=latin1";

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
    // Append suffix indicating InnoDB and latin1 charset.
    if(sql.toUpperCase().startsWith("CREATE TABLE")) {
      sql += CREATE_TABLE_SUFFIX;
    }

    return sql;
  }
}