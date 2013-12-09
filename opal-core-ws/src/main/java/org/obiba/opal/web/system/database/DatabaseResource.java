package org.obiba.opal.web.system.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.datasource.mongodb.MongoDBDatasourceFactory;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.database.MultipleIdentifiersDatabaseException;
import org.obiba.opal.core.service.database.NoSuchDatabaseException;
import org.obiba.opal.web.database.Dtos;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Ws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static org.obiba.opal.web.model.Database.DatabaseDto;

/**
 * Class is not transactional because of delete method
 */
@Component
@Scope("request")
@Path("/system/database/{name}")
public class DatabaseResource {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @PathParam("name")
  private String name;

  @GET
  public DatabaseDto get() {
    Database database = getDatabase();
    return Dtos.asDto(database, databaseRegistry.hasDatasource(database), databaseRegistry.hasEntities(database));
  }

  @DELETE
  public Response delete() {
    Database database = getDatabase();
    databaseRegistry.delete(database);
    return Response.ok().build();
  }

  @PUT
  public Response update(DatabaseDto dto) throws MultipleIdentifiersDatabaseException {
    Database database = null;
    try {
      Database existing = databaseRegistry.getDatabase(name);
      if(databaseRegistry.hasDatasource(existing)) {
        // restrict edition to certain fields when database has datasource
        database = existing;
        database.setDefaultStorage(dto.getDefaultStorage());
      } else {
        database = Dtos.fromDto(dto);
      }
    } catch(NoSuchDatabaseException e) {
      database = Dtos.fromDto(dto);
    }
    databaseRegistry.update(database);
    return Response.ok().build();
  }

  @POST
  @Path("/connections")
  @Transactional(readOnly = true)
  public Response testConnection() {
    Database database = getDatabase();
    if(database.hasSqlSettings()) {
      return testSqlConnection();
    }
    if(database.hasMongoDbSettings()) {
      return testMongoConnection(database.getMongoDbSettings());
    }
    throw new RuntimeException("Connection test not yet implemented for database " + database.getClass());
  }

  private Database getDatabase() {
    return databaseRegistry.getDatabase(name);
  }

  private Response testSqlConnection() {
    Ws.ClientErrorDto error = ClientErrorDtos.getErrorMessage(SERVICE_UNAVAILABLE, "DatabaseConnectionFailed").build();
    try {
      JdbcTemplate jdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource(name, null));
      Boolean result = jdbcTemplate.execute(new ConnectionCallback<Boolean>() {

        @Override
        public Boolean doInConnection(Connection con) throws SQLException, DataAccessException {
          return con.isValid(1);
        }
      });
      if(result != null && result) {
        return Response.ok().build();
      }
    } catch(RuntimeException e) {
      error = ClientErrorDtos.getErrorMessage(SERVICE_UNAVAILABLE, "DatabaseConnectionFailed", e);
    }
    return Response.status(SERVICE_UNAVAILABLE).entity(error).build();
  }

  private Response testMongoConnection(MongoDbSettings mongoDbSettings) {
    Ws.ClientErrorDto error = null;
    try {
      MongoDBDatasourceFactory datasourceFactory = mongoDbSettings.createMongoDBDatasourceFactory("_test");
      List<String> dbs = datasourceFactory.getMongoDBFactory().getMongoClient().getDatabaseNames();
      if(dbs.contains(datasourceFactory.getMongoDbDatabaseName())) {
        return Response.ok().build();
      } else {
        DBCollection collTest = datasourceFactory.getMongoDBFactory().getDB().getCollection("_coll_test");
        collTest.drop();
        return Response.ok().build();
      }
    } catch(RuntimeException e) {
      error = ClientErrorDtos.getErrorMessage(SERVICE_UNAVAILABLE, "DatabaseConnectionFailed", e);
    }
    return Response.status(SERVICE_UNAVAILABLE).entity(error).build();
  }

}
