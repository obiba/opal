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

import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
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
import java.util.stream.Collectors;

/**
 * Manages Datashield packages.
 */
@Component
@Transactional
@Path("/datashield/packages")
public class DataShieldPackagesResource {

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @Autowired
  private DataShieldPackageMethodHelper dsPackageMethodeHelper;

  @GET
  public List<OpalR.RPackageDto> getPackages(@QueryParam("profile") String profile) {
    return dsPackageMethodeHelper.getInstalledPackagesDtos(getDataShieldProfile(profile));
  }

  @POST
  public Response installPackage(@Context UriInfo uriInfo, @QueryParam("name") String name,
                                 @QueryParam("ref") String ref, @QueryParam("manager") String manager, @QueryParam("profile") String profile) {
    DataShieldProfile dsProfile = getDataShieldProfile(profile);
    List<String> originalPkgNames = getPackages(profile).stream()
        .map(OpalR.RPackageDto::getName)
        .collect(Collectors.toList());
    dsPackageMethodeHelper.installDatashieldPackage(dsProfile, name, ref, manager);

    // publish settings of new package(s)
    getPackages(profile).stream()
        .filter(p -> !originalPkgNames.contains(p.getName()))
        .forEach(p -> dsPackageMethodeHelper.publish(dsProfile, p.getName()));

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
    names.stream().distinct().forEach(name -> dsPackageMethodeHelper.publish(getDataShieldProfile(profile), name));
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
    names.forEach(name -> dsPackageMethodeHelper.unpublish(getDataShieldProfile(profile), name));
    return Response.noContent().build();
  }

  @DELETE
  public Response deletePackages(@QueryParam("profile") String profile) {
    try {
      for (OpalR.RPackageDto pkg : getPackages(profile)) {
        dsPackageMethodeHelper.deletePackage(getDataShieldProfile(profile), pkg);
      }
    } catch (Exception e) {
      // ignored
    }
    return Response.ok().build();
  }

  private DataShieldProfile getDataShieldProfile(String profileName) {
    return datashieldProfileService.getProfile(profileName);
  }

}
