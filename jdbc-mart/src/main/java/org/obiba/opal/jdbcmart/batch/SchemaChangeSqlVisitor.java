package org.obiba.opal.jdbcmart.batch;

import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.database.sql.visitor.AbstractSqlVisitor;

public class SchemaChangeSqlVisitor extends AbstractSqlVisitor {
  //
  // Constants
  //
  
  public static final String MYSQL_MODIFICATION = " ENGINE=InnoDB DEFAULT CHARSET=latin1";
  
  //
  // AbstractSqlVisitor Methods
  //
  
  public String getTagName() {
    return null;
  }

  public String modifySql(String sql, Database database) {
    // For MySQL, modify the SQL to specify InnoDB tables and the latin1 charset.
    if (database instanceof MySQLDatabase) {
      sql += MYSQL_MODIFICATION;  
    }
    
    return sql;
  }
}
