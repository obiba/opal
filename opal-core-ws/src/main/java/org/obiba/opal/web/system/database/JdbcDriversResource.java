package org.obiba.opal.web.system.database;

import java.io.IOException;
import java.sql.Driver;

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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

@Component
@Transactional
@Path("/system/databases/jdbc-drivers")
public class JdbcDriversResource {

  @Autowired
  private JdbcDriverRegistry jdbcDriverRegistry;

  @GET
  public Iterable<Database.JdbcDriverDto> getJdbcDrivers() {
    return Iterables.transform(jdbcDriverRegistry.listDrivers(), new Function<Driver, Database.JdbcDriverDto>() {
      @Override
      public Database.JdbcDriverDto apply(Driver driver) {
        return Database.JdbcDriverDto.newBuilder() //
            .setDriverName(jdbcDriverRegistry.getDriverName(driver)) //
            .setDriverClass(driver.getClass().getName()) //
            .setJdbcUrlTemplate(jdbcDriverRegistry.getJdbcUrlTemplate(driver)) //
            .setJdbcUrlExample(jdbcDriverRegistry.getJdbcUrlExample(driver)) //
            .setVersion(driver.getMajorVersion() + "." + driver.getMinorVersion()).build();
      }
    });
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
