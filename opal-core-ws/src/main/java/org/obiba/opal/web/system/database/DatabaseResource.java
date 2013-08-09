package org.obiba.opal.web.system.database;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/system/database/{name}")
public class DatabaseResource {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @PathParam("name")
  private String name;

//  @GET
//  public Opal.JdbcDataSourceDto get() {
//    return Dtos.JdbcDataSourceDtos.asDto.apply(databaseRegistry.getDatabase(name));
//  }
//
//  @DELETE
//  public Response delete() {
//    if(!jdbcDataSource.isEditable()) {
//      return Response.status(Response.Status.BAD_REQUEST).build();
//    }
//    jdbcDataSourceRegistry.remove(jdbcDataSource);
//    return Response.ok().build();
//  }
//
//  @PUT
//  public Response update(Opal.JdbcDataSourceDto dto) {
//    if(!jdbcDataSource.isEditable()) {
//      return Response.status(Response.Status.BAD_REQUEST).build();
//    }
//    if(!dto.getName().equals(jdbcDataSource.getName())) {
//      return Response.status(Response.Status.BAD_REQUEST).entity(
//          Ws.ClientErrorDto.newBuilder().setCode(Response.Status.BAD_REQUEST.getStatusCode()).setStatus("InvalidName").build())
//          .build();
//    }
//    jdbcDataSourceRegistry.update(Dtos.JdbcDataSourceDtos.fromDto.apply(dto));
//    return Response.ok().build();
//  }
//
//  @POST
//  @Path("/connections")
//  public Response testConnection() {
//    Ws.ClientErrorDto error = ClientErrorDtos
//        .getErrorMessage(Response.Status.SERVICE_UNAVAILABLE, "DatabaseConnectionFailed", "")
//        .build();
//    try {
//      JdbcTemplate t = new JdbcTemplate(jdbcDataSourceRegistry.getDataSource(jdbcDataSource.getName(), null));
//      Boolean result = t.execute(new ConnectionCallback<Boolean>() {
//
//        @Override
//        public Boolean doInConnection(Connection con) throws SQLException, DataAccessException {
//          return con.isValid(1);
//        }
//      });
//      if(result != null && result != null && result) {
//        return Response.ok().build();
//      }
//    } catch(DataAccessException dae) {
//      error = ClientErrorDtos.getErrorMessage(Response.Status.SERVICE_UNAVAILABLE, "DatabaseConnectionFailed", dae).build();
//    } catch(RuntimeException e) {
//      error = ClientErrorDtos.getErrorMessage(Response.Status.SERVICE_UNAVAILABLE, "DatabaseConnectionFailed", e).build();
//    }
//    return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(error).build();
//  }

}
