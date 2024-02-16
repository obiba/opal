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

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.PrintWriter;
import java.util.List;

public class AbstractRServiceResource {

  private static final Logger log = LoggerFactory.getLogger(AbstractRServiceResource.class);

  @Autowired
  protected RServerManagerService rServerManagerService;

  @Autowired
  protected RPackageResourceHelper rPackageHelper;

  protected Response tailRserveLog(RServerService server, int nbLines, String fileId) {
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
        .header("Content-Disposition", "attachment; filename=RServer-" + fileId + ".log").build();
  }
}