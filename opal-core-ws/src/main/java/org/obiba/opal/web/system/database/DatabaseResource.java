/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.database;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.obiba.magma.SocketFactoryProvider;
import org.obiba.magma.datasource.mongodb.MongoDBDatasourceFactory;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.database.MultipleIdentifiersDatabaseException;
import org.obiba.opal.core.service.database.NoSuchDatabaseException;
import org.obiba.opal.web.database.Dtos;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.obiba.opal.web.model.Database.DatabaseDto;

/**
 * Class is not transactional because of delete method
 */
@Component
@Scope("request")
@Path("/system/database/{name}")
@Tag(name = "Databases", description = "Operations on databases")
public class DatabaseResource {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private SocketFactoryProvider socketFactoryProvider;

  @PathParam("name")
  private String name;

  @GET
  @Operation(
    summary = "Get database configuration",
    description = "Retrieves detailed information about a specific database including its configuration and datasource status."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved database information"),
    @ApiResponse(responseCode = "404", description = "Database not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public DatabaseDto get() {
    Database database = getDatabase();
    return Dtos.asDto(database, databaseRegistry.hasDatasource(database));
  }

  @DELETE
  @Operation(
    summary = "Delete database",
    description = "Removes a database from the system configuration."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Database successfully deleted"),
    @ApiResponse(responseCode = "404", description = "Database not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response delete() {
    Database database = getDatabase();
    databaseRegistry.delete(database);
    return Response.ok().build();
  }

  @PUT
  @Operation(
    summary = "Update database configuration",
    description = "Updates database configuration. Database name cannot be changed, but other settings can be modified."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Database successfully updated"),
    @ApiResponse(responseCode = "400", description = "Invalid database configuration"),
    @ApiResponse(responseCode = "404", description = "Database not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response update(DatabaseDto dto) throws MultipleIdentifiersDatabaseException {
    Database database = Dtos.fromDto(dto);
    try {
      // Allow edition of all fields except database name
      database.setName(databaseRegistry.getDatabase(name).getName());
    } catch(NoSuchDatabaseException ignored) {
      // do nothing if it's a new database
    }
    databaseRegistry.update(database);
    return Response.ok().build();
  }

  @POST
  @Path("/connections")
  @Transactional(readOnly = true)
  @Operation(
    summary = "Test database connection",
    description = "Tests the connectivity to a database by attempting to establish a connection. Supports both SQL and MongoDB databases."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Database connection test successful"),
    @ApiResponse(responseCode = "404", description = "Database not found"),
    @ApiResponse(responseCode = "503", description = "Database connection failed"),
    @ApiResponse(responseCode = "500", description = "Internal server error or unsupported database type")
  })
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

  @GET
  @Path("/hasEntities")
  @Operation(
    summary = "Check if database has entities",
    description = "Returns whether the database contains any entity records, indicating if it has been used for data storage."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved entity status"),
    @ApiResponse(responseCode = "404", description = "Database not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response getHasEntities() {
    Database database = databaseRegistry.getDatabase(name);
    return Response.ok().entity(String.valueOf(databaseRegistry.hasEntities(database))).build();
  }

  private Database getDatabase() {
    return databaseRegistry.getDatabase(name);
  }

  private Response testSqlConnection() {
    try {
      JdbcOperations jdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource(name, null));
      return jdbcTemplate.execute(new ConnectionCallback<Response>() {
        @Override
        public Response doInConnection(Connection con) throws SQLException, DataAccessException {
          return con.isValid(1) ? Response.ok().build() : databaseConnectionFailed();
        }
      });
    } catch(RuntimeException e) {
      return databaseConnectionFailed();
    }
  }

  private Response testMongoConnection(MongoDbSettings mongoDbSettings) {
    try {
      MongoDBDatasourceFactory datasourceFactory = mongoDbSettings.createMongoDBDatasourceFactory("_test", socketFactoryProvider);
      List<String> dbs = StreamSupport.stream(datasourceFactory.getMongoDBFactory().getMongoClient().listDatabaseNames().spliterator(), false)
          .toList();
      if(dbs.contains(datasourceFactory.getMongoDbDatabaseName())) {
        return Response.ok().build();
      }
      return datasourceFactory.getMongoDBFactory().execute(db -> {
        db.getCollection("_coll_test").drop();
        return Response.ok().build();
      });
    } catch(RuntimeException e) {
      return databaseConnectionFailed();
    }
  }

  private Response databaseConnectionFailed() {
    return Response.status(Response.Status.SERVICE_UNAVAILABLE)
        .entity(ClientErrorDtos.getErrorMessage(Response.Status.SERVICE_UNAVAILABLE, "DatabaseConnectionFailed").build()).build();
  }

}
