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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Component
@Scope("request")
@Path("/app/{id}")
public class AppResource {

  @PathParam("id")
  private String id;

  @Autowired
  private AppsService appsService;

  @GET
  public Apps.AppDto get() {
    return Dtos.asDto(appsService.getApp(id));
  }

  @DELETE
  public Response unregister() {
    try {
      appsService.unregisterApp(appsService.getApp(id));
    } catch (Exception e) {
      // ignore
    }
    return Response.noContent().build();
  }

}
