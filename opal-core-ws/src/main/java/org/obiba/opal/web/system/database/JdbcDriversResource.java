package org.obiba.opal.web.system.database;

import java.io.IOException;
import java.sql.Driver;
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
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

@Component
@Path("/system/databases/jdbc-drivers")
public class JdbcDriversResource {

  @Autowired
  private JdbcDriverRegistry jdbcDriverRegistry;

  @GET
  @Path("/drivers")
  public Iterable<Opal.JdbcDriverDto> getJdbcDrivers() {
    return Iterables.transform(jdbcDriverRegistry.listDrivers(), new Function<Driver, Opal.JdbcDriverDto>() {

      @Override
      public Opal.JdbcDriverDto apply(Driver input) {
        return Opal.JdbcDriverDto.newBuilder()//
            .setDriverName(jdbcDriverRegistry.getDriverName(input))//
            .setDriverClass(input.getClass().getName())//
            .setJdbcUrlTemplate(jdbcDriverRegistry.getJdbcUrlTemplate(input))//
            .setVersion(input.getMajorVersion() + "." + input.getMinorVersion()).build();
      }
    });
  }

  @SuppressWarnings("unchecked")
  @POST
  @Consumes("multipart/form-data")
  @Produces("text/html")
  @AuthenticatedByCookie
  @Path("/drivers")
  public Response addDriver(@Context UriInfo uriInfo, @Context HttpServletRequest request)
      throws FileUploadException, IOException {
    ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
    for(FileItem fileItem : (List<FileItem>) upload.parseRequest(request)) {
      if(!fileItem.isFormField()) {
        jdbcDriverRegistry.addDriver(fileItem.getName(), fileItem.getInputStream());
      }
    }
    return Response.created(uriInfo.getRequestUri()).build();
  }
}
