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
import org.springframework.transaction.annotation.Transactional;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import static org.obiba.opal.web.model.Database.DatabaseDto;

@Component
@Transactional
@Path("/system/databases")
@Api(value = "/system/databases", description = "Operations about databases")
public class DatabasesResource {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @GET
  @NoAuthorization
  @ApiOperation(value = "Returns all data databases",
      notes = "The Identifiers database will not be returned here", response = List.class)
  public List<DatabaseDto> getDatabases(
      @ApiParam(value = "database usage", allowableValues = "import,storage,export") @QueryParam("usage")
      String usage,
      @ApiParam(value = "should response contains database settings", defaultValue = "false") @QueryParam("settings")
      @DefaultValue("false") Boolean settings) {
    try {
      return asDto(databaseRegistry.list(usage == null ? null : Database.Usage.valueOf(usage.toUpperCase())), settings);
    } catch(IllegalArgumentException e) {
      throw new InvalidRequestException("Not a valid database usage: " + usage);
    }
  }

  @GET
  @Path("/sql")
  @NoAuthorization
  @ApiOperation(value = "Returns all SQL data databases",
      notes = "The Identifiers database will not be returned here", response = List.class)
  public List<DatabaseDto> getSqlDatabases() {
    return asDto(databaseRegistry.listSqlDatabases(), true);
  }

  @GET
  @Path("/mongodb")
  @NoAuthorization
  @ApiOperation(value = "Returns all MongoDB data databases",
      notes = "The Identifiers database will not be returned here", response = List.class)
  public List<DatabaseDto> getMongoDbDatabases() {
    return asDto(databaseRegistry.listMongoDatabases(), true);
  }

  @GET
  @Path("/identifiers")
  @ApiOperation(value = "Returns the Identifiers database", response = DatabaseDto.class)
  @ApiResponses(@ApiResponse(code = 404, message = "Identifiers database not found"))
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
  @ApiOperation("Create a new database")
  @ApiResponses(@ApiResponse(code = 400, message = "Database for identifiers already exists"))
  public Response addDatabase(DatabaseDto database) throws MultipleIdentifiersDatabaseException {
    databaseRegistry.save(Dtos.fromDto(database));
    return Response.ok().build();
  }

}
