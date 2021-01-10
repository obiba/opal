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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Component
@Scope("request")
@Path("/app/{name}")
public class AppResource {

    @PathParam("name")
    private String name;

    @Autowired
    private AppsService appsService;

    @GET
    public Apps.AppDto get() {
        return Dtos.asDto(appsService.getApp(name));
    }

    @DELETE
    @NotAuthenticated
    public Response disable() {
        appsService.unregisterApp(name);
        return Response.noContent().build();
    }

}
