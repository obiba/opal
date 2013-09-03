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
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Ws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

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
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    databaseRegistry.deleteDatabase(database);
    return Response.ok().build();
  }

  @PUT
  public Response update(Opal.DatabaseDto dto) {
    Database database = Dtos.fromDto(dto);
    if(!database.isEditable()) {
      return Response.status(Response.Status.BAD_REQUEST).build();
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
    Ws.ClientErrorDto error = ClientErrorDtos
        .getErrorMessage(Response.Status.SERVICE_UNAVAILABLE, "DatabaseConnectionFailed", "").build();
    try {
      JdbcTemplate t = new JdbcTemplate(databaseRegistry.getDataSource(name, null));
      Boolean result = t.execute(new ConnectionCallback<Boolean>() {

        @Override
        public Boolean doInConnection(Connection con) throws SQLException, DataAccessException {
          return con.isValid(1);
        }
      });
      if(result != null && result) return Response.ok().build();
    } catch(DataAccessException dae) {
      error = ClientErrorDtos.getErrorMessage(Response.Status.SERVICE_UNAVAILABLE, "DatabaseConnectionFailed", dae)
          .build();
    } catch(RuntimeException e) {
      error = ClientErrorDtos.getErrorMessage(Response.Status.SERVICE_UNAVAILABLE, "DatabaseConnectionFailed", e)
          .build();
    }
    return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(error).build();
  }

}
