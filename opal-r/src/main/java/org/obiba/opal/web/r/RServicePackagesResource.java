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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * R package management of the default R server cluster.
 */
@Component
@Scope("request")
@Path("/service/r/packages")
public class RServicePackagesResource {

  private static final Logger log = LoggerFactory.getLogger(RServicePackagesResource.class);

  @Autowired
  private RPackageResourceHelper rPackageHelper;

  @Autowired
  private RServerManagerService rServerManagerService;

  @GET
  public List<OpalR.RPackageDto> getPackages() {
    return rPackageHelper.getInstalledPackagesDtos(rServerManagerService.getDefaultRServer());
  }

  @PUT
  public Response updateAllPackages() {
    try {
      rPackageHelper.updateAllCRANPackages(rServerManagerService.getDefaultRServer());
    } catch (Exception e) {
      log.error("Failed at updating all R packages", e);
    }
    return Response.ok().build();
  }

  @POST
  public Response installPackage(@Context UriInfo uriInfo, @QueryParam("name") String name,
                                 @QueryParam("ref") String ref, @QueryParam("manager") String manager) {
    rPackageHelper.installPackage(rServerManagerService.getDefaultRServer(), name, ref, manager);
    UriBuilder ub = uriInfo.getBaseUriBuilder().path(RServicePackageResource.class);
    return Response.created(ub.build(name)).build();
  }
}
