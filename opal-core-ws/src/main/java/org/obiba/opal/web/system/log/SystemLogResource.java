/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.log;

import org.obiba.opal.core.service.SystemLogService;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Component
@Path("/system/log")
public class SystemLogResource {

  @Autowired
  private SystemLogService systemLogService;

  @GET
  @Path("opal.log")
  @AuthenticatedByCookie
  public Response getOpalLog(@QueryParam("all") @DefaultValue("true") boolean all) {
    return all ? getLogs("opal.log", systemLogService.getOpalLogFiles()) : getLog(systemLogService.getOpalLogFile());
  }

  @GET
  @Path("datashield.log")
  @AuthenticatedByCookie
  public Response getDatashieldLog(@QueryParam("all") @DefaultValue("true") boolean all) {
    return all ? getLogs("datashield.log", systemLogService.getDatashieldLogFiles()) : getLog(systemLogService.getDatashieldLogFile());
  }

  @GET
  @Path("rest.log")
  @AuthenticatedByCookie
  public Response getRestLog(@QueryParam("all") @DefaultValue("true") boolean all) {
    return all ? getLogs("rest.log", systemLogService.getRestLogFiles()) : getLog(systemLogService.getRestLogFile());
  }

  @GET
  @Path("sql.log")
  @AuthenticatedByCookie
  public Response getSQLLog(@QueryParam("all") @DefaultValue("true") boolean all) {
    return all ? getLogs("sql.log", systemLogService.getSQLLogFiles()) : getLog(systemLogService.getSQLLogFile());
  }

  private Response getLog(File file) {
    StreamingOutput stream = os -> Files.copy(file.toPath(), os);
    return Response.ok(stream, "text/plain")
        .header("Content-Disposition", "attachment; filename=" + file.getName()).build();
  }

  private Response getLogs(String filename, List<File> files) {
    StreamingOutput stream = os ->
        files.forEach(file -> {
          try {
            Files.copy(file.toPath(), os);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
    return Response.ok(stream, "text/plain")
        .header("Content-Disposition", "attachment; filename=" + filename).build();
  }
}
