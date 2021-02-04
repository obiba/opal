/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.r.service.RServerService;
import org.obiba.opal.web.model.OpalR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.PrintWriter;

@Component
@Scope("request")
@Path("/service/r/cluster/{name}")
public class RServiceClusterResource {

  private static final Logger log = LoggerFactory.getLogger(RServiceClusterResource.class);

  @Autowired
  private RServerManagerService rServerManagerService;

  @GET
  public OpalR.RServerClusterDto getCluster(@PathParam("name") String name) {
    return Dtos.asDto(rServerManagerService.getRServerCluster(name));
  }

  @PUT
  public Response startCluster(@PathParam("name") String name) {
    rServerManagerService.getRServerCluster(name).start();
    return Response.ok().build();
  }

  @DELETE
  public Response stopCluster(@PathParam("name") String name) {
    rServerManagerService.getRServerCluster(name).stop();
    return Response.ok().build();
  }

  @GET
  @Path("/server/{sname}")
  public OpalR.RServerDto getServer(@PathParam("name") String name, @PathParam("sname") String serverName) {
    return Dtos.asDto(rServerManagerService.getRServerCluster(name).getRServerService(serverName));
  }

  @PUT
  @Path("/server/{sname}")
  public Response startServer(@PathParam("name") String name, @PathParam("sname") String serverName) {
    rServerManagerService.getRServerCluster(name).getRServerService(serverName).start();
    return Response.ok().build();
  }

  @DELETE
  @Path("/server/{sname}")
  public Response stopServer(@PathParam("name") String name, @PathParam("sname") String serverName) {
    rServerManagerService.getRServerCluster(name).getRServerService(serverName).stop();
    return Response.ok().build();
  }

  @GET
  @Path("/server/{sname}/_log")
  public Response tailRserveLog(@PathParam("name") String name, @PathParam("sname") String serverName, @QueryParam("n") @DefaultValue("10000") Integer nbLines) {
    RServerService server = rServerManagerService.getRServerCluster(name).getRServerService(serverName);
    String[] rlog = server.getLog(nbLines);
    log.info("received {} lines", rlog.length);
    StreamingOutput stream = output -> {
      try (PrintWriter writer = new PrintWriter(output)) {
        for (String line : rlog) {
          writer.println(line);
        }
      }
    };
    return Response.ok(stream, "text/plain")
        .header("Content-Disposition", "attachment; filename=RServer-" + name + "-" + serverName + ".log").build();
  }
}