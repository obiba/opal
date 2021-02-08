/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

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
  public OpalR.RPackageDto getPackage(@PathParam("name") String name) {
    return rPackageHelper.getInstalledPackagesDtos(rServerManagerService.getDefaultRServer()).stream()
        .filter(dto -> dto.getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new NoSuchRPackageException(name));
  }

  @DELETE
  public Response deletePackage(@PathParam("name") String name) {
    try {
      rPackageHelper.removePackage(rServerManagerService.getDefaultRServer(), name);
    } catch (Exception e) {
      // ignore
    }
    return Response.noContent().build();
  }
}
