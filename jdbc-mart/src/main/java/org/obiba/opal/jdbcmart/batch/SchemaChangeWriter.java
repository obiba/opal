package org.obiba.opal.jdbcmart.batch;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.sql.visitor.SqlVisitor;
import liquibase.exception.JDBCException;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;

public class SchemaChangeWriter extends AbstractItemStreamItemWriter<Change> {
  //
  // Instance Variables
  //
  
  private DataSource dataSource;
  
  private Database database;
  
  private SqlVisitor sqlVisitor;
    
  //
  // AbstractItemStreamItemWriter Methods
  //
  
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    Connection connection = null;
    
    // Get a connection to the dataSource.
    try {
      connection = dataSource.getConnection();  
    }
    catch(SQLException ex) {
      throw new ItemStreamException("Could not acquire a connection to the dataSource", ex);
    }
    
    // Find an appropriate database instance for the connection.
    try {
      DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
      database = databaseFactory.findCorrectDatabaseImplementation(connection);
    }
    catch(JDBCException ex) {
      throw new ItemStreamException("Could not locate a database implementation for the connection", ex);
    }
  }
  
  public void close() throws ItemStreamException {
    // Close the database connection.
    try {
      database.close();
    }
    catch(JDBCException ex) {
      throw new ItemStreamException("Could not close the database connection", ex);
    }
  }
  
  public void write(List<? extends Change> items) throws Exception {
    List<SqlVisitor> sqlVisitors = new ArrayList<SqlVisitor>();
    if (sqlVisitor != null) {
      sqlVisitors.add(sqlVisitor);
    }
    
    for (Change schemaChange : items) {
      schemaChange.executeStatements(database, sqlVisitors);
    }
  }

  //
  // Methods
  //
  
  public void setDataSource(DataSource dataSource)
  {
    this.dataSource = dataSource;
  }
  
  public void setSqlVisitor(SqlVisitor sqlVisitor) {
    this.sqlVisitor = sqlVisitor;
  }
}