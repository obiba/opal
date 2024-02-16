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
public class DatabasesResource {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @GET
  @NoAuthorization
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
  public List<DatabaseDto> getSqlDatabases() {
    return asDto(databaseRegistry.listSqlDatabases(), true);
  }

  @GET
  @Path("/mongodb")
  public List<DatabaseDto> getMongoDbDatabases() {
    return asDto(databaseRegistry.listMongoDatabases(), true);
  }

  @GET
  @Path("/identifiers")
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
  public Response addDatabase(DatabaseDto database) throws MultipleIdentifiersDatabaseException {
    databaseRegistry.create(Dtos.fromDto(database));
    return Response.ok().build();
  }

}
