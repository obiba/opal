package org.obiba.opal.core.runtime.database;

import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbDatabase;
import org.obiba.opal.core.domain.database.SqlDatabase;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class DatabaseConverter {

  private DatabaseConverter() {}

  public static ODocument marshall(Database database) {
    ODocument doc = new ODocument("Database");
    doc.field("className", database.getClass());

    if(database instanceof SqlDatabase) {
      marshall((SqlDatabase) database, doc);
    } else if(database instanceof MongoDbDatabase) {
      marshall((MongoDbDatabase) database, doc);
    } else {
      throw new IllegalArgumentException("Unknown database class " + database.getClass());
    }
    doc.field("name", database.getName());
    doc.field("type", database.getType());
    doc.field("description", database.getDescription());
    return doc;
  }

  private static void marshall(SqlDatabase sqlDatabase, ODocument doc) {
    doc.field("url", sqlDatabase.getUrl());
    doc.field("driverClass", sqlDatabase.getDriverClass());
    doc.field("username", sqlDatabase.getUsername());
    doc.field("password", sqlDatabase.getPassword());
    doc.field("properties", sqlDatabase.getProperties());
    doc.field("magmaDatasourceType", sqlDatabase.getMagmaDatasourceType());
  }

  private static void marshall(MongoDbDatabase mongoDbDatabase, ODocument doc) {
    doc.field("url", mongoDbDatabase.getUrl());
    doc.field("username", mongoDbDatabase.getUsername());
    doc.field("password", mongoDbDatabase.getPassword());
    doc.field("properties", mongoDbDatabase.getProperties());
  }

  public static Database unmarshall(ODocument doc) {
    if(doc == null) return null;
    try {
      Database database = doc.<Class<Database>>field("className").newInstance();
      database.setName(doc.<String>field("name"));
      database.setType(doc.<Database.Type>field("type"));
      database.setDescription(doc.<String>field("description"));

      if(database instanceof SqlDatabase) {
        unmarshall(doc, (SqlDatabase) database);
      } else if(database instanceof MongoDbDatabase) {
        unmarshall(doc, (MongoDbDatabase) database);
      } else {
        throw new IllegalArgumentException("Unknown database class " + database.getClass());
      }

      return database;
    } catch(InstantiationException e) {
      throw new RuntimeException("Cannot unmarshall document to Database", e);
    } catch(IllegalAccessException e) {
      throw new RuntimeException("Cannot unmarshall document to Database", e);
    }
  }

  private static void unmarshall(ODocument doc, SqlDatabase database) {
    database.setDriverClass(doc.<String>field("driverClass"));
    database.setUrl(doc.<String>field("url"));
    database.setUsername(doc.<String>field("username"));
    database.setPassword(doc.<String>field("password"));
    database.setProperties(doc.<String>field("properties"));
    database.setMagmaDatasourceType(doc.<String>field("magmaDatasourceType"));
  }

  private static void unmarshall(ODocument doc, MongoDbDatabase database) {
    database.setUrl(doc.<String>field("url"));
    database.setUsername(doc.<String>field("username"));
    database.setPassword(doc.<String>field("password"));
    database.setProperties(doc.<String>field("properties"));
  }

}
