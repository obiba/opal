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
public class JdbcDriversResource {

  @GET
  public Iterable<Database.JdbcDriverDto> getJdbcDrivers() {
    Collection<Database.JdbcDriverDto> drivers = new ArrayList<>();
    drivers.add(Database.JdbcDriverDto.newBuilder() //
        .setDriverName("MySQL") //
        .setDriverClass("com.mysql.jdbc.Driver") //
        .setJdbcUrlTemplate("jdbc:mysql://{hostname}:{port}/{databaseName}") //
        .setJdbcUrlExample("jdbc:mysql://localhost:3306/opal") //
        .addSupportedSchemas("hibernate") //
        .addSupportedSchemas("jdbc") //
        .build());
    drivers.add(Database.JdbcDriverDto.newBuilder() //
        .setDriverName("MariaDB") //
        .setDriverClass("org.mariadb.jdbc.Driver") //
        .setJdbcUrlTemplate("jdbc:mariadb://{hostname}:{port}/{databaseName}") //
        .setJdbcUrlExample("jdbc:mariadb://localhost:3306/opal") //
        .addSupportedSchemas("hibernate") //
        .addSupportedSchemas("jdbc") //
        .build());
    drivers.add(Database.JdbcDriverDto.newBuilder() //
        .setDriverName("PostgreSQL") //
        .setDriverClass("org.postgresql.Driver") //
        .setJdbcUrlTemplate("jdbc:postgresql://{hostname}:{port}/{databaseName}") //
        .setJdbcUrlExample("jdbc:postgresql://localhost:5432/opal") //
        .addSupportedSchemas("jdbc") //
        .build());
    return drivers;
  }
}
