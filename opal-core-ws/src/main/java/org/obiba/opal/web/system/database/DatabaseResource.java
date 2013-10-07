package org.obiba.opal.web.system.database;

import java.sql.Connection;
import java.sql.SQLException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.runtime.database.DatabaseAlreadyExistsException;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.opal.core.runtime.database.MultipleIdentifiersDatabaseException;
import org.obiba.opal.web.database.Dtos;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Ws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

@Component
@Scope("request")
@Path("/system/database/{name}")
public class DatabaseResource {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @PathParam("name")
  private String name;

  @GET
  public Opal.DatabaseDto get() {
    return Dtos.asDto(getDatabase());
  }

  private Database getDatabase() {
    return databaseRegistry.getDatabase(name);
  }

  @DELETE
  public Response delete() {
    Database database = getDatabase();
    if(!database.isEditable()) {
      return Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "DatabaseIsNotEditable").build()).build();
    }
    databaseRegistry.deleteDatabase(database);
    return Response.ok().build();
  }

  @PUT
  public Response update(Opal.DatabaseDto dto)
      throws MultipleIdentifiersDatabaseException, DatabaseAlreadyExistsException {

    Database database = databaseRegistry.getDatabase(dto.getName());
    if(database == null) {
      return Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "CannotFindDatabase", dto.getName()).build()).build();
    }

    Dtos.fromDto(dto, database);
    if(!database.isEditable()) {
      return Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "DatabaseIsNotEditable").build()).build();
    }

    databaseRegistry.addOrReplaceDatabase(database);

    return Response.ok().build();
  }

  @POST
  @Path("/connections")
  public Response testConnection() {
    Database database = getDatabase();
    if(database instanceof SqlDatabase) {
      return testSqlConnection();
    }
    throw new RuntimeException("Connection test not yet implemented for database " + database.getClass());
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
      if(result != null && result) return Response.ok().build();
    } catch(DataAccessException e) {
      error = ClientErrorDtos.getErrorMessage(SERVICE_UNAVAILABLE, "DatabaseConnectionFailed", e);
    } catch(RuntimeException e) {
      error = ClientErrorDtos.getErrorMessage(SERVICE_UNAVAILABLE, "DatabaseConnectionFailed", e);
    }
    return Response.status(SERVICE_UNAVAILABLE).entity(error).build();
  }

}
