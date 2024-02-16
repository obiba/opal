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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.obiba.opal.core.runtime.jdbc.JdbcDriverRegistry;
import org.obiba.opal.web.model.Database;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Path("/system/databases/jdbc-drivers")
public class JdbcDriversResource {

  @Autowired
  private JdbcDriverRegistry jdbcDriverRegistry;

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

  @POST
  @Consumes("multipart/form-data")
  @Produces("text/html")
  @AuthenticatedByCookie
  public Response addDriver(@Context UriInfo uriInfo, @Context HttpServletRequest request)
      throws FileUploadException, IOException {
    ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
    for(FileItem fileItem : upload.parseRequest(request)) {
      if(!fileItem.isFormField()) {
        jdbcDriverRegistry.addDriver(fileItem.getName(), fileItem.getInputStream());
      }
    }
    return Response.created(uriInfo.getRequestUri()).build();
  }
}
