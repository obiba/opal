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

import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.r.cluster.RServerCluster;
import org.obiba.opal.r.service.RServerService;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.r.service.RServerState;
import org.obiba.opal.spi.r.RRuntimeException;
import org.obiba.opal.spi.r.RServerException;
import org.obiba.opal.web.model.Apps;
import org.obiba.opal.web.model.OpalR;
import org.obiba.opal.web.model.Ws;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.io.File;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Utility class for building R related Dtos.
 */
public class Dtos {

  private Dtos() {
  }

  public static OpalR.RProfileDto asProfileDto(RServerCluster cluster) {
    return OpalR.RProfileDto.newBuilder()
        .setName(cluster.getName())
        .setEnabled(true)
        .setRestrictedAccess(false)
        .build();
  }


  public static OpalR.RServerClusterDto asDto(RServerCluster cluster) {
    return OpalR.RServerClusterDto.newBuilder()
        .setName(cluster.getName())
        .addAllServers(cluster.getRServerServices().stream().map(Dtos::asDto).collect(Collectors.toList()))
        .build();
  }

  public static OpalR.RServerDto asDto(RServerService server) {
    OpalR.RServerDto.Builder builder = OpalR.RServerDto.newBuilder()
        .setName(server.getName())
        .setRunning(server.isRunning());

    try {
      RServerState state = server.getState();
      builder.setCluster(state.getCluster())
          .setVersion(state.getVersion())
          .addAllTags(state.getTags())
          .setSessionCount(state.getRSessionsCount())
          .setBusySessionCount(state.getBusyRSessionsCount())
          .setCores(state.getSystemCores())
          .setFreeMemory(state.getSystemFreeMemory());
    } catch (RServerException e) {
      builder.setVersion("?")
          .setCluster("?")
          .setSessionCount(0)
          .setBusySessionCount(0)
          .setCores(0)
          .setFreeMemory(0);
    }

    App app = server.getApp();
    if (app != null) {
      builder.setApp(Apps.AppDto.newBuilder()
          .setId(app.getId())
          .setName(app.getName())
          .setType(app.getType())
          .setServer(app.getServer())
          .build());
    }

    return builder.build();
  }

  public static OpalR.RSessionDto asDto(RServerSession rSession) {
    UriBuilder ub = UriBuilder.fromPath("/").path(OpalRSessionParentResource.class)
        .path(OpalRSessionParentResource.class, "getOpalRSessionResource");
    return OpalR.RSessionDto.newBuilder().setId(rSession.getId())
        .setUser(rSession.getUser())
        .setCreationDate(DateTimeType.get().valueOf(rSession.getCreated()).toString())
        .setLastAccessDate(DateTimeType.get().valueOf(rSession.getTimestamp()).toString())
        .setStatus(rSession.isBusy() ? OpalR.RSessionStatus.BUSY : OpalR.RSessionStatus.WAITING)
        .setCurrentExecutionTimeMillis(rSession.getCurrentExecutionTimeMillis())
        .setTotalExecutionTimeMillis(rSession.getTotalExecutionTimeMillis())
        .setLink(ub.build(rSession.getId()).toString())
        .setContext(rSession.getExecutionContext())
        .setProfile(rSession.getProfile().getName())
        .setCluster(rSession.getProfile().getCluster())
        .setServer(rSession.getRServerServiceName())
        .build();
  }

  public static OpalR.RWorkspaceDto asDto(String executionContext, String user, File workspace) {
    long size = 0;
    try {
      size = FileUtils.sizeOf(workspace);
    } catch (Exception e) {
      // may happen if folder is written/deleted while iterating content
    }
    return OpalR.RWorkspaceDto.newBuilder()
        .setName(workspace.getName())
        .setUser(user)
        .setLastAccessDate(DateTimeType.get().valueOf(new Date(workspace.lastModified())).toString())
        .setContext(executionContext)
        .setSize(size)
        .build();
  }

  public static Ws.ClientErrorDto getErrorMessage(Response.StatusType status,
                                                  RRuntimeException exception) {

    Ws.ClientErrorDto.Builder builder = Ws.ClientErrorDto.newBuilder()
        .setStatus("RServerRuntimeError")
        .setCode(status.getStatusCode())
        .addExtension(OpalR.RRuntimeErrorDto.errors, OpalR.RRuntimeErrorDto.newBuilder().build());

    String message = exception.getMessage();

    builder.addArguments(
        Strings.isNullOrEmpty(message)
            ? String.format(exception.getClass().getSimpleName())
            : message);

    return builder.build();
  }

  public static Ws.ClientErrorDto getErrorMessage(Response.StatusType status,
                                                  RServerException exception) {

    Ws.ClientErrorDto.Builder builder = Ws.ClientErrorDto.newBuilder()
        .setStatus("RServerError")
        .setCode(status.getStatusCode())
        .addExtension(OpalR.RRuntimeErrorDto.errors, OpalR.RRuntimeErrorDto.newBuilder().build());

    String message = exception.getMessage();

    builder.addArguments(
        Strings.isNullOrEmpty(message)
            ? String.format(exception.getClass().getSimpleName())
            : message);

    return builder.build();
  }
}
