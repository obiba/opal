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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.ArrayList;
import java.util.Collection;

@Component
@Transactional
@Path("/system/databases/jdbc-drivers")
@Tag(name = "Databases", description = "Operations on databases")
public class JdbcDriversResource {

  @GET
  @Operation(
      summary = "Get JDBC drivers",
      description = "Retrieves the list of supported JDBC drivers with connection URL templates and examples."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "JDBC drivers list retrieved successfully"),
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
        .build());
    drivers.add(Database.JdbcDriverDto.newBuilder() //
        .setDriverName("MariaDB") //
        .setDriverClass("org.mariadb.jdbc.Driver") //
        .setJdbcUrlTemplate("jdbc:mariadb://{hostname}:{port}/{databaseName}") //
        .setJdbcUrlExample("jdbc:mariadb://localhost:3306/opal") //
        .addSupportedSchemas("jdbc") //
        .build());
    drivers.add(Database.JdbcDriverDto.newBuilder() //
        .setDriverName("PostgreSQL") //
        .setDriverClass("org.postgresql.Driver") //
        .setJdbcUrlTemplate("jdbc:postgresql://{hostname}:{port}/{databaseName}") //
        .setJdbcUrlExample("jdbc:postgresql://localhost:5432/opal") //
        .addSupportedSchemas("jdbc") //
        .build());
    drivers.add(Database.JdbcDriverDto.newBuilder() //
        .setDriverName("SQL Server") //
        .setDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver") //
        .setJdbcUrlTemplate("jdbc:sqlserver://{hostname}:{port};databaseName={databaseName}") //
        .setJdbcUrlExample("jdbc:sqlserver://localhost:1433;databaseName=opal") //
        .addSupportedSchemas("jdbc") //
        .build());
    return drivers;
  }
}
