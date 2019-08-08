/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import org.obiba.opal.r.service.OpalRService;
import org.obiba.opal.spi.r.RScriptROperation;
import org.rosuda.REngine.REXPMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.PrintWriter;

@Component
@Scope("request")
@Path("/service/r/log")
public class RServiceLogResource {

  private static final Logger log = LoggerFactory.getLogger(RServiceLogResource.class);

  @Autowired
  protected OpalRService opalRService;

  @GET
  @Path("Rserve.log")
  public Response tailRserveLog(@QueryParam("n") @DefaultValue("10000") Integer nbLines) {
    try {
      String cmd;
      if (nbLines > 0)
        cmd = String.format("tail(readLines(con = file('../../../logs/Rserve.log', 'r')), %s)", nbLines);
      else
        cmd = "readLines(con = file('../../../logs/Rserve.log', 'r'))";
      RScriptROperation rop = execute(cmd);
      String[] rlog = rop.getResult().asStrings();
      log.info("received {} lines", rlog.length);
      StreamingOutput stream = output -> {
        try (PrintWriter writer = new PrintWriter(output)) {
          for (String line : rlog) {
            writer.println(line);
          }
        }
      };
      return Response.ok(stream, "text/plain")
          .header("Content-Disposition", "attachment; filename=Rserve.log").build();
    } catch (REXPMismatchException e) {
      log.error("Failed at downloading Rserve.log", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  protected RScriptROperation execute(String rscript) {
    log.info(rscript);
    RScriptROperation rop = new RScriptROperation(rscript, false);
    opalRService.execute(rop);
    return rop;
  }


}
