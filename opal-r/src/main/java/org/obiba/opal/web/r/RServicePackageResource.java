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
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

/**
 * R package management of the default R server cluster.
 */
@Component
@Scope("request")
@Path("/service/r/package/{name}")
public class RServicePackageResource {

  @Autowired
  private RPackageResourceHelper rPackageHelper;

  @Autowired
  private RServerManagerService rServerManagerService;

  @GET
  public OpalR.RPackageDto getPackage(@PathParam("name") String name, @QueryParam("profile") String profile) {
    return rPackageHelper.getInstalledPackagesDtos(rServerManagerService.getRServer(profile)).stream()
        .filter(dto -> dto.getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new NoSuchRPackageException(name));
  }

  @DELETE
  public Response deletePackage(@PathParam("name") String name, @QueryParam("profile") String profile) {
    try {
      rPackageHelper.removePackage(rServerManagerService.getRServer(profile), name);
    } catch (Exception e) {
      // ignore
    }
    return Response.noContent().build();
  }
}
