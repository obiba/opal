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
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

@Component
@Path("/apps")
public class AppsResource {

    @Autowired
    private AppsService appsService;

    @GET
    @NotAuthenticated
    public Apps.AppsDto list(@QueryParam("type") String type) {
        return Apps.AppsDto.newBuilder()
                .addAllApps(appsService.getApps(type).stream()
                        .map(Dtos::asDto).collect(Collectors.toList()))
                .build();
    }

    @POST
    @NotAuthenticated
    public Response registerApp(Apps.AppDto appDto) {
        appsService.registerApp(Dtos.fromDto(appDto));
        return Response.ok().build();
    }

    @DELETE
    @NotAuthenticated
    public Response unRegisterApp(Apps.AppDto appDto) {
        appsService.unregisterApp(Dtos.fromDto(appDto));
        return Response.ok().build();
    }
}
