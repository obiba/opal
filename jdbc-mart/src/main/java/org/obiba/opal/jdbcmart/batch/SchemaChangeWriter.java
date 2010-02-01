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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;

public class SchemaChangeWriter extends AbstractItemStreamItemWriter<Change> {

  private static final Logger log = LoggerFactory.getLogger(SchemaChangeWriter.class);

  //
  // Instance Variables
  //

  private DataSource dataSource;

  private Connection connection;

  private Database database;

  private List<SqlVisitor> sqlVisitors;

  //
  // Constructors
  //

  public SchemaChangeWriter() {
    sqlVisitors = new ArrayList<SqlVisitor>();
  }

  //
  // AbstractItemStreamItemWriter Methods
  //

  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    // Get a connection to the dataSource.
    try {
      connection = dataSource.getConnection();
    } catch(SQLException ex) {
      throw new ItemStreamException("Could not acquire a connection to the dataSource", ex);
    }

    // Find an appropriate database instance for the connection.
    try {
      DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
      database = databaseFactory.findCorrectDatabaseImplementation(connection);
    } catch(JDBCException ex) {
      throw new ItemStreamException("Could not locate a database implementation for the connection", ex);
    }
  }

  @Override
  public void close() throws ItemStreamException {
    // Close the connection to the dataSource.
    if(connection != null) {
      try {
        if(!connection.isClosed()) {
          connection.close();
        }
      } catch(SQLException ex) {
        throw new ItemStreamException("Could not close connection to the dataSource", ex);
      }
    }
  }

  public void write(List<? extends Change> items) throws Exception {
    for(Change schemaChange : items) {
      schemaChange.executeStatements(database, sqlVisitors);
    }
  }

  //
  // Methods
  //

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setSqlVisitors(List<SqlVisitor> sqlVisitors) {
    this.sqlVisitors.clear();

    if(sqlVisitors != null) {
      this.sqlVisitors.addAll(sqlVisitors);
    }
  }
}