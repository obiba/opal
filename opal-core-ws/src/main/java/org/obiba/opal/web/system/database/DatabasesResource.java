package org.obiba.opal.web.system.database;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.domain.database.Database;
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
    List<Opal.DatabaseDto> databases = new ArrayList<Opal.DatabaseDto>();
    for(Database database : databaseRegistry.list(type)) {
      databases.add(Dtos.asDto(database));
    }
    return databases;
  }

  @POST
  public Response addDatabase(Opal.DatabaseDto database) {
    databaseRegistry.updateDatabase(Dtos.fromDto(database));
    return Response.ok().build();
  }

}
