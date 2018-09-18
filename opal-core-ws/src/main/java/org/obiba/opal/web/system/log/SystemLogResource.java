package org.obiba.opal.web.system.log;

import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.nio.file.Files;

@Component
@Path("/system/log")
public class SystemLogResource {

  @Autowired
  private SystemLogService systemLogService;

  @GET
  @Path("opal.log")
  @AuthenticatedByCookie
  public Response getOpalLog() {
    return getLog(systemLogService.getOpalLogFile());
  }

  @GET
  @Path("datashield.log")
  @AuthenticatedByCookie
  public Response getDatashieldLog() {
    return getLog(systemLogService.getDatashieldLogFile());
  }

  @GET
  @Path("rest.log")
  @AuthenticatedByCookie
  public Response getRestLog() {
    return getLog(systemLogService.getRestLogFile());
  }

  private Response getLog(File file) {
    StreamingOutput stream = os -> Files.copy(file.toPath(), os);
    return Response.ok(stream, "text/plain")
        .header("Content-Disposition", "attachment; filename=opal.log").build();
  }

}
