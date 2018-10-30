/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import org.apache.commons.io.FileUtils;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.web.model.OpalR;
import org.obiba.opal.web.model.Ws;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.util.Date;

/**
 * Utility class for building R related Dtos.
 */
public class Dtos {

  private Dtos() {}

  public static OpalR.RSessionDto asDto(OpalRSession rSession) {
    UriBuilder ub = UriBuilder.fromPath("/").path(OpalRSessionParentResource.class)
        .path(OpalRSessionParentResource.class, "getOpalRSessionResource");
    return OpalR.RSessionDto.newBuilder().setId(rSession.getId()) //
        .setUser(rSession.getUser()) //
        .setCreationDate(DateTimeType.get().valueOf(rSession.getCreated()).toString()) //
        .setLastAccessDate(DateTimeType.get().valueOf(rSession.getTimestamp()).toString()) //
        .setStatus(rSession.isBusy() ? OpalR.RSessionStatus.BUSY : OpalR.RSessionStatus.WAITING) //
        .setLink(ub.build(rSession.getId()).toString()) //
        .setContext(rSession.getExecutionContext()) //
        .build();
  }

  public static OpalR.RWorkspaceDto asDto(String executionContext, String user, File workspace) {
    long size = 0;
    try {
      size = FileUtils.sizeOf(workspace);
    } catch (Exception e) {
      // may happen if folder is written/deleted while iterating content
    }
    return OpalR.RWorkspaceDto.newBuilder() //
        .setName(workspace.getName()) //
        .setUser(user) //
        .setLastAccessDate(DateTimeType.get().valueOf(new Date(workspace.lastModified())).toString()) //
        .setContext(executionContext) //
        .setSize(size) //
        .build();
  }


  public static Ws.ClientErrorDto getErrorMessage(Response.StatusType status,
                                                  String errorStatus,
                                                  RuntimeException exception) {
    return Ws.ClientErrorDto.newBuilder()
            .setStatus(errorStatus)
            .setCode(status.getStatusCode())
            .addArguments(exception.getMessage())
            .addExtension(OpalR.RServerRuntimeErrorDto.errors, OpalR.RServerRuntimeErrorDto.newBuilder().build())
            .build();
  }
}
