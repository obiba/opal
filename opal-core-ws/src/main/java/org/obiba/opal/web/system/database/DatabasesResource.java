package org.obiba.opal.web.system.database;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.database.MultipleIdentifiersDatabaseException;
import org.obiba.opal.web.database.Dtos;
import org.obiba.opal.web.support.InvalidRequestException;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.obiba.opal.web.model.Database.DatabaseDto;

@Component
@Path("/system/databases")
public class DatabasesResource {

  private final DatabaseRegistry databaseRegistry;

  @Autowired
  public DatabasesResource(DatabaseRegistry databaseRegistry) {
    this.databaseRegistry = databaseRegistry;
  }

  @GET
  @NoAuthorization
  public List<DatabaseDto> getDatabases(@QueryParam("usage") String usage, @QueryParam("settings") @DefaultValue("false") Boolean settings) {
    try {
      return asDto(databaseRegistry.list(usage == null ? null : Database.Usage.valueOf(usage.toUpperCase())), settings);
    } catch(IllegalArgumentException e) {
      throw new InvalidRequestException("Not a valid database usage: " + usage);
    }
  }

  @GET
  @Path("/sql")
  @NoAuthorization
  public List<DatabaseDto> getSqlDatabases() {
    return asDto(databaseRegistry.listSqlDatabases(), true);
  }

  @GET
  @Path("/mongodb")
  @NoAuthorization
  public List<DatabaseDto> getMongoDbDatabases() {
    return asDto(databaseRegistry.listMongoDatabases(), true);
  }

  @GET
  @Path("/identifiers")
  public DatabaseDto getIdentifiersDatabase() {
    return Dtos.asDto(databaseRegistry.getIdentifiersDatabase());
  }

  private List<DatabaseDto> asDto(Iterable<? extends Database> databases, boolean withSettings) {
    List<DatabaseDto> dtos = new ArrayList<DatabaseDto>();
    for(Database database : databases) {
      dtos.add(Dtos.asDto(database, withSettings));
    }
    return dtos;
  }

  @POST
  public Response addDatabase(DatabaseDto database) throws MultipleIdentifiersDatabaseException {
    databaseRegistry.save(Dtos.fromDto(database));
    return Response.ok().build();
  }

}
