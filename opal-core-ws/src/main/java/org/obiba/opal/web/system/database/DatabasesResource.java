package org.obiba.opal.web.system.database;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbDatabase;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.opal.core.runtime.database.MultipleIdentifiersDatabaseException;
import org.obiba.opal.web.database.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.obiba.opal.web.model.Database.DatabaseDto;
import static org.obiba.opal.web.model.Database.DatabasesStatusDto;

@Component
@Path("/system/databases")
public class DatabasesResource {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @GET
  @Path("/_status")
  public DatabasesStatusDto getDatabases() {
    DatabasesStatusDto.Builder builder = DatabasesStatusDto.newBuilder();
    builder.setHasIdentifiers(databaseRegistry.hasIdentifiersDatabase());
    builder.setHasStorage(databaseRegistry.hasDatabases(Database.Usage.STORAGE));
    return builder.build();
  }

  @GET
  @Path("/sql")
  public List<DatabaseDto> getSqlDatabases() {
    return asDto(databaseRegistry.list(SqlDatabase.class));
  }

  @GET
  @Path("/mongodb")
  public List<DatabaseDto> getMongoDbDatabases() {
    return asDto(databaseRegistry.list(MongoDbDatabase.class));
  }

  @GET
  @Path("/storage")
  public List<DatabaseDto> getStorageDatabases() {
    return asDto(databaseRegistry.list(Database.Usage.STORAGE));
  }

  @GET
  @Path("/identifiers")
  public DatabaseDto getIdentifiersDatabase() {
    return Dtos.asDto(databaseRegistry.getIdentifiersDatabase());
  }

  private List<DatabaseDto> asDto(Iterable<? extends Database> databases) {
    List<DatabaseDto> dtos = new ArrayList<DatabaseDto>();
    for(Database database : databases) {
      dtos.add(Dtos.asDto(database));
    }
    return dtos;
  }

  @POST
  public Response addDatabase(DatabaseDto database) throws MultipleIdentifiersDatabaseException {
    databaseRegistry.addOrReplaceDatabase(Dtos.fromDto(database));
    return Response.ok().build();
  }

}
