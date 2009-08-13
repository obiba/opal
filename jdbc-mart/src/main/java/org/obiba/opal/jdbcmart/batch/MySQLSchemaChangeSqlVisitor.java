package org.obiba.opal.jdbcmart.batch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.database.sql.visitor.AbstractSqlVisitor;
import liquibase.database.sql.visitor.SqlVisitor;

public class MySQLSchemaChangeSqlVisitor implements SqlVisitor {
  //
  // Constants
  //
  
  public static final String CREATE_TABLE_SUFFIX = " ENGINE=InnoDB DEFAULT CHARSET=latin1";
  
  //
  // Instance Variables
  //
  
  private Map<String, String> typeMap;

  //
  // Constructors
  //
  
  public MySQLSchemaChangeSqlVisitor() {
    typeMap = new HashMap<String, String>();
  }
  
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
    sql += CREATE_TABLE_SUFFIX;  
    
    // Replace semantic types with mapped MySQL column types.
    for (Map.Entry<String, String> entry : typeMap.entrySet()) {
      sql = sql.replaceAll(entry.getKey(), entry.getValue());
    }
    
    return sql;
  }
  
  //
  // Methods
  //
  
  public void setTypeMap(Map<String, String> typeMap) {
    this.typeMap.clear();
    
    if (typeMap != null) {
      this.typeMap.putAll(typeMap); 
    }
  }
}