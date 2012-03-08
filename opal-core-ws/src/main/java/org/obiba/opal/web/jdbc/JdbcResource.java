/*******************************************************************************
 * Copyright 2012(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.jdbc;

import java.io.IOException;
import java.sql.Driver;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.obiba.opal.core.runtime.jdbc.JdbcDataSourceRegistry;
import org.obiba.opal.core.runtime.jdbc.JdbcDriverRegistry;
import org.obiba.opal.web.model.Opal.JdbcDataSourceDto;
import org.obiba.opal.web.model.Opal.JdbcDriverDto;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

@Component
@Path("/jdbc")
public class JdbcResource {

  private final JdbcDriverRegistry jdbcDriverRegistry;

  private final JdbcDataSourceRegistry jdbcDataSourceRegistry;

  @Autowired
  public JdbcResource(JdbcDriverRegistry jdbcDriverRegistry, JdbcDataSourceRegistry jdbcDataSourceRegistry) {
    this.jdbcDriverRegistry = jdbcDriverRegistry;
    this.jdbcDataSourceRegistry = jdbcDataSourceRegistry;
  }

  @GET
  @Path("/drivers")
  public Iterable<JdbcDriverDto> getJdbcDrivers() {
    return Iterables.transform(jdbcDriverRegistry.listDrivers(), new Function<Driver, JdbcDriverDto>() {

      @Override
      public JdbcDriverDto apply(Driver input) {
        return JdbcDriverDto.newBuilder()//
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
  public Response
      addDriver(@Context UriInfo uriInfo, @Context HttpServletRequest request) throws FileUploadException, IOException {
    ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
    for(FileItem fileItem : (List<FileItem>) upload.parseRequest(request)) {
      if(!fileItem.isFormField()) {
        jdbcDriverRegistry.addDriver(fileItem.getName(), fileItem.getInputStream());
      }
    }
    return Response.created(uriInfo.getRequestUri()).build();
  }

  @GET
  @Path("/databases")
  public Iterable<JdbcDataSourceDto> getJdbcDataSources() {
    return Iterables.transform(jdbcDataSourceRegistry.listDataSources(), Dtos.JdbcDataSourceDtos.asDto);
  }

  @POST
  @Path("/databases")
  public Response addJdbcDataSource(JdbcDataSourceDto dto) {
    jdbcDataSourceRegistry.registerDataSource(Dtos.JdbcDataSourceDtos.fromDto.apply(dto));
    return Response.ok().build();
  }

  @Path("/database/{name}")
  public Object getJdbcDatasource(@PathParam("name") String name) {
    return new JdbcDataSourceResource(jdbcDataSourceRegistry, jdbcDataSourceRegistry.getJdbcDataSource(name));
  }
}
