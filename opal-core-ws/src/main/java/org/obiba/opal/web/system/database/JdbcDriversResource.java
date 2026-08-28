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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.opal.web.model.Database;
import org.obiba.opal.web.model.Database.DatabaseDto.Usage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@Transactional
@Path("/system/databases/jdbc-drivers")
@Tag(name = "Databases", description = "Operations on databases")
public class JdbcDriversResource {

  private static final Collection<String> ALL_USAGES = List
      .of(Usage.IMPORT.name(), Usage.STORAGE.name(), Usage.EXPORT.name());

  @GET
  @Operation(
      summary = "Get JDBC drivers",
      description = "Retrieves the list of supported JDBC drivers with connection URL templates and examples."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "JDBC drivers list retrieved successfully", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Iterable<Database.JdbcDriverDto> getJdbcDrivers() {
    Collection<Database.JdbcDriverDto> drivers = new ArrayList<>();
    drivers.add(Database.JdbcDriverDto.newBuilder() //
        .setDriverName("MySQL") //
        .setDriverClass("com.mysql.jdbc.Driver") //
        .setJdbcUrlTemplate("jdbc:mysql://{hostname}:{port}/{databaseName}") //
        .setJdbcUrlExample("jdbc:mysql://localhost:3306/opal") //
        .addSupportedSchemas("jdbc") //
        .addAllSupportedUsages(ALL_USAGES) //
        .build());
    drivers.add(Database.JdbcDriverDto.newBuilder() //
        .setDriverName("MariaDB") //
        .setDriverClass("org.mariadb.jdbc.Driver") //
        .setJdbcUrlTemplate("jdbc:mariadb://{hostname}:{port}/{databaseName}") //
        .setJdbcUrlExample("jdbc:mariadb://localhost:3306/opal") //
        .addSupportedSchemas("jdbc") //
        .addAllSupportedUsages(ALL_USAGES) //
        .build());
    drivers.add(Database.JdbcDriverDto.newBuilder() //
        .setDriverName("PostgreSQL") //
        .setDriverClass("org.postgresql.Driver") //
        .setJdbcUrlTemplate("jdbc:postgresql://{hostname}:{port}/{databaseName}") //
        .setJdbcUrlExample("jdbc:postgresql://localhost:5432/opal") //
        .addSupportedSchemas("jdbc") //
        .addAllSupportedUsages(ALL_USAGES) //
        .build());
    drivers.add(Database.JdbcDriverDto.newBuilder() //
        .setDriverName("SQL Server") //
        .setDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver") //
        .setJdbcUrlTemplate("jdbc:sqlserver://{hostname}:{port};databaseName={databaseName}") //
        .setJdbcUrlExample("jdbc:sqlserver://localhost:1433;databaseName=opal") //
        .addSupportedSchemas("jdbc") //
        .addAllSupportedUsages(ALL_USAGES) //
        .build());
    // H2 is embedded: the database is registered by name only, its file lives in the Opal H2 folder, and it can only
    // be used for storage as there is no pre-existing database to import from or export to.
    drivers.add(Database.JdbcDriverDto.newBuilder() //
        .setDriverName("H2") //
        .setDriverClass("org.h2.Driver") //
        .setJdbcUrlTemplate("jdbc:h2:file:{databaseName}") //
        .setJdbcUrlExample("jdbc:h2:file:opal") //
        .addSupportedSchemas("jdbc") //
        .addSupportedUsages(Usage.STORAGE.name()) //
        .build());
    return drivers;
  }
}
