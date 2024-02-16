/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.app;


import org.obiba.opal.core.cfg.AppsService;
import org.obiba.opal.web.model.Apps;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Path("/apps")
public class AppsResource {

  private static final String APP_AUTH_HEADER = "X-App-Auth";

  @Autowired
  private AppsService appsService;

  @GET
  public List<Apps.AppDto> list(@QueryParam("type") String type) {
    return appsService.getApps(type).stream()
        .map(Dtos::asDto).collect(Collectors.toList());
  }

  @GET
  @Path("/config")
  public Apps.AppsConfigDto getConfig() {
    return Dtos.asDto(appsService.getAppsConfig());
  }

  @PUT
  @Path("/config")
  public Response updateConfig(Apps.AppsConfigDto configDto) {
    appsService.updateAppsConfig(Dtos.fromDto(configDto));
    return Response.ok().build();
  }

  @DELETE
  @Path("/config")
  public Response resetConfig() {
    appsService.resetConfig();
    return Response.ok().build();
  }

  /**
   * Self registration.
   *
   * @param servletRequest
   * @param appDto
   * @return
   */
  @POST
  @NotAuthenticated
  public Response registerApp(@Context HttpServletRequest servletRequest, Apps.AppDto appDto) {
    appsService.checkToken(servletRequest.getHeader(APP_AUTH_HEADER));
    appsService.checkSelfRegistrationRules(appDto.getServer());
    appsService.registerApp(Dtos.fromDto(appDto));
    return Response.ok().build();
  }

  /**
   * Self unregistration.
   *
   * @param servletRequest
   * @param appDto
   * @return
   */
  @DELETE
  @NotAuthenticated
  public Response unRegisterApp(@Context HttpServletRequest servletRequest, Apps.AppDto appDto) {
    appsService.checkToken(servletRequest.getHeader(APP_AUTH_HEADER));
    appsService.unregisterApp(Dtos.fromDto(appDto));
    return Response.ok().build();
  }
}
