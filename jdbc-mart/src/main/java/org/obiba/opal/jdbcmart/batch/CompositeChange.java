package org.obiba.opal.jdbcmart.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.UnsupportedChangeException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class CompositeChange extends AbstractChange {

  private List<Change> changes = new LinkedList<Change>();

  public CompositeChange() {
    super("", "");
  }

  public void addChange(Change change) {
    changes.add(change);
  }

  public Node createNode(Document doc) {
    return null;
  }

  public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
    List<SqlStatement> statements = new ArrayList<SqlStatement>();
    for(Change change : changes) {
      statements.addAll(Arrays.asList(change.generateStatements(database)));
    }
    return statements.toArray(new SqlStatement[statements.size()]);
  }

  public Set<DatabaseObject> getAffectedDatabaseObjects() {
    Set<DatabaseObject> dbo = new LinkedHashSet<DatabaseObject>();
    for(Change change : changes) {
      dbo.addAll(change.getAffectedDatabaseObjects());
    }
    return dbo;
  }

  public String getConfirmationMessage() {
    return "";
  }

  public void validate(Database database) throws InvalidChangeDefinitionException {
    for(Change change : changes) {
      change.validate(database);
    }
  }

}
