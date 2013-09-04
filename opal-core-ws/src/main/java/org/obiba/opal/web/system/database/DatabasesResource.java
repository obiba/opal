package org.obiba.opal.web.system.database;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbDatabase;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/system/databases")
public class DatabasesResource {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @GET
  public List<Opal.DatabaseDto> getDatabases(@QueryParam("type") String type) {
    return asDto(databaseRegistry.list(type));
  }

  @GET
  @Path("/sql")
  public List<Opal.DatabaseDto> getSqlDatabases() {
    return asDto(databaseRegistry.list(SqlDatabase.class));
  }

  @GET
  @Path("/mongodb")
  public List<Opal.DatabaseDto> getMongoDbDatabases() {
    return asDto(databaseRegistry.list(MongoDbDatabase.class));
  }

  @GET
  @Path("/identifiers")
  public Opal.DatabaseDto getIdentifiersDatabase() {
    return Dtos.asDto(databaseRegistry.getIdentifiersDatabase());
  }

  private List<Opal.DatabaseDto> asDto(Iterable<? extends Database> databases) {
    List<Opal.DatabaseDto> dtos = new ArrayList<Opal.DatabaseDto>();
    for(Database database : databases) {
      dtos.add(Dtos.asDto(database));
    }
    return dtos;
  }

  @POST
  public Response addDatabase(Opal.DatabaseDto database) {
    databaseRegistry.addOrReplaceDatabase(Dtos.fromDto(database));
    return Response.ok().build();
  }

}
