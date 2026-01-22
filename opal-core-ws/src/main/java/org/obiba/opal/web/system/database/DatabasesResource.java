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

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.database.MultipleIdentifiersDatabaseException;
import org.obiba.opal.web.database.Dtos;
import org.obiba.opal.web.support.InvalidRequestException;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.obiba.opal.web.model.Database.DatabaseDto;

@Component
@Transactional
@Path("/system/databases")
@Tag(name = "Databases", description = "Operations on databases")
public class DatabasesResource {

  @Autowired
  private DatabaseRegistry databaseRegistry;

@GET
@NoAuthorization
@Operation(summary = "Get all databases", description = "Retrieve list of all configured databases with optional filtering by usage type and inclusion of settings")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "List of databases successfully retrieved"),
  @ApiResponse(responseCode = "400", description = "Invalid database usage parameter"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public List<DatabaseDto> getDatabases(@QueryParam("usage") String usage, @QueryParam("settings")
    @DefaultValue("false") Boolean settings) {
  try {
    return asDto(databaseRegistry.list(usage == null ? null : Database.Usage.valueOf(usage.toUpperCase())), settings);
  } catch(IllegalArgumentException e) {
    throw new InvalidRequestException("Not a valid database usage: " + usage);
  }
}

@GET
@Path("/sql")
@Operation(summary = "Get SQL databases", description = "Retrieve list of all SQL databases with settings included")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "List of SQL databases successfully retrieved"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public List<DatabaseDto> getSqlDatabases() {
  return asDto(databaseRegistry.listSqlDatabases(), true);
}

@GET
@Path("/mongodb")
@Operation(summary = "Get MongoDB databases", description = "Retrieve list of all MongoDB databases with settings included")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "List of MongoDB databases successfully retrieved"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public List<DatabaseDto> getMongoDbDatabases() {
  return asDto(databaseRegistry.listMongoDatabases(), true);
}

@GET
@Path("/identifiers")
@Operation(summary = "Get identifiers database", description = "Retrieve the database used for storing entity identifiers")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Identifiers database successfully retrieved"),
  @ApiResponse(responseCode = "404", description = "No identifiers database configured"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public DatabaseDto getIdentifiersDatabase() {
  Database database = databaseRegistry.getIdentifiersDatabase();
  return Dtos.asDto(database, databaseRegistry.hasDatasource(database));
}

  private List<DatabaseDto> asDto(Iterable<? extends Database> databases, boolean withSettings) {
    List<DatabaseDto> dtos = new ArrayList<>();
    for(Database database : databases) {
      dtos.add(Dtos.asDto(database, databaseRegistry.hasDatasource(database), withSettings));
    }
    return dtos;
  }

@POST
@Operation(summary = "Add or update database", description = "Create a new database configuration or update an existing one. If a database with the same name exists, it will be updated; otherwise a new database will be created.")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Database successfully added or updated"),
  @ApiResponse(responseCode = "400", description = "Invalid database configuration"),
  @ApiResponse(responseCode = "409", description = "Multiple identifiers database conflict"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Response addOrUpdateDatabase(DatabaseDto database) throws MultipleIdentifiersDatabaseException {
  if (databaseRegistry.hasDatabase(database.getName())) {
    databaseRegistry.update(Dtos.fromDto(database));
  } else {
    databaseRegistry.create(Dtos.fromDto(database));
  }
  return Response.ok().build();
}

}
