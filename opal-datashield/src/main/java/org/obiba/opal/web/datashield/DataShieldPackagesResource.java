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

import org.obiba.datashield.core.DSMethodType;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.web.datashield.support.DataShieldPackageMethodHelper;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
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

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  @GET
  public List<OpalR.RPackageDto> getPackages(@QueryParam("profile") String profile) {
    return dsPackageMethodeHelper.getInstalledPackagesDtos(getDataShieldProfile(profile));
  }

  @POST
  public Response installPackage(@Context UriInfo uriInfo, @QueryParam("name") String name,
                                 @QueryParam("ref") String ref, @QueryParam("manager") String manager, @QueryParam("profile") String profile) {
    if (!opalGeneralConfigService.getConfig().isAllowRPackageManagement())
      return Response.status(Response.Status.FORBIDDEN).build();

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
    DataShieldProfile profileObj = getDataShieldProfile(profile);
    profileObj.clear();
    if (names == null || names.isEmpty()) {
      dsPackageMethodeHelper.getInstalledPackagesDtos(profileObj)
          .forEach(pkg -> dsPackageMethodeHelper.publish(profileObj, pkg));
    } else {
      names.stream().distinct().forEach(name -> dsPackageMethodeHelper.publish(profileObj, name));
    }
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
    DataShieldProfile profileObj = getDataShieldProfile(profile);
    if (names == null || names.isEmpty()) {
      profileObj.clear();
      datashieldProfileService.saveProfile(profileObj);
    } else
      names.forEach(name -> dsPackageMethodeHelper.unpublish(getDataShieldProfile(profile), name));
    return Response.noContent().build();
  }

  @DELETE
  public Response deletePackages(@QueryParam("profile") String profile) {
    if (!opalGeneralConfigService.getConfig().isAllowRPackageManagement())
      return Response.status(Response.Status.FORBIDDEN).build();

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
