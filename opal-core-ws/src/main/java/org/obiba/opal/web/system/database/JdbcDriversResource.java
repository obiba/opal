package org.obiba.opal.web.system.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
    List<Database.JdbcDriverDto> drivers = new ArrayList<>();
    drivers.add(Database.JdbcDriverDto.newBuilder() //
        .setDriverName("MySQL") //
        .setDriverClass("com.mysql.jdbc.Driver") //
        .setJdbcUrlTemplate("jdbc:mysql://{hostname}:{port}/{databaseName}?characterEncoding={encoding}") //
        .setJdbcUrlExample("jdbc:mysql://localhost:3306/opal?characterEncoding=UTF-8").build());
    drivers.add(Database.JdbcDriverDto.newBuilder() //
        .setDriverName("HSQLDB") //
        .setDriverClass("org.hsqldb.jdbcDriver") //
        .setJdbcUrlTemplate("jdbc:hsqldb:file:{databaseName};shutdown=true;hsqldb.tx=mvcc") //
        .setJdbcUrlExample("jdbc:hsqldb:file:opal;shutdown=true;hsqldb.tx=mvcc").build());

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
