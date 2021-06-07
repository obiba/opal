/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.datashield;

import org.obiba.opal.web.datashield.support.DataShieldPackageMethodHelper;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Manages Datashield packages.
 */
@Component
@Transactional
@Path("/datashield/packages")
public class DataShieldPackagesResource {

  @Autowired
  private DataShieldPackageMethodHelper dsPackageMethodeHelper;

  @GET
  public List<OpalR.RPackageDto> getPackages(@QueryParam("profile") String profile) {
    return dsPackageMethodeHelper.getInstalledPackagesDtos(profile);
  }

  @POST
  public Response installPackage(@Context UriInfo uriInfo, @QueryParam("name") String name,
                                 @QueryParam("ref") String ref, @QueryParam("profile") String profile) {
    dsPackageMethodeHelper.installDatashieldPackage(profile, name, ref);

    // install or re-install all known datashield package methods
    List<OpalR.RPackageDto> pkgs = getPackages(profile);
    for (OpalR.RPackageDto pkg : pkgs) {
      dsPackageMethodeHelper.publish(profile, pkg.getName());
    }
    // make sure last is the one we install (check it is a "datashield" package first)
    if (getPackages(profile).stream().anyMatch(p -> p.getName().equals(name)))
      dsPackageMethodeHelper.publish(profile, name);

    UriBuilder ub = uriInfo.getBaseUriBuilder().path(DataShieldPackageResource.class);
    return Response.created(ub.build(name)).build();
  }

  /**
   * Append DataSHIELD package settings to the profile configuration as a bulk operation.
   *
   * @param names
   * @param profile
   * @return
   */
  @PUT
  @Path("_publish")
  public Response publishPackagesSettings(@QueryParam("name") List<String> names, @QueryParam("profile") String profile) {
    names.forEach(name -> dsPackageMethodeHelper.publish(profile, name));
    return Response.ok().build();
  }

  /**
   * Remove package DataSHIELD settings from profile configuration as a bulk operation.
   *
   * @param names
   * @param profile
   * @return
   */
  @DELETE
  @Path("_publish")
  public Response deletePackageSettings(@QueryParam("name") List<String> names, @QueryParam("profile") String profile) {
    names.forEach(name -> dsPackageMethodeHelper.unpublish(profile, name));
    return Response.noContent().build();
  }

  @DELETE
  public Response deletePackages(@QueryParam("profile") String profile) {
    try {
      for (OpalR.RPackageDto pkg : getPackages(profile)) {
        dsPackageMethodeHelper.deletePackage(profile, pkg);
      }
    } catch (Exception e) {
      // ignored
    }
    return Response.ok().build();
  }

}
