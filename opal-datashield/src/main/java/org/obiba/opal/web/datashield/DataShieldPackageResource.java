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

import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.web.datashield.support.DataShieldPackageMethodHelper;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manage a Datashield Package.
 */
@Component
@Transactional
@Scope("request")
@Path("/datashield/package/{name}")
public class DataShieldPackageResource {

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @Autowired
  private DataShieldPackageMethodHelper dsPackageMethodeHelper;

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  @GET
  public List<OpalR.RPackageDto> getPackage(@PathParam("name") String name, @QueryParam("profile") String profile) {
    return dsPackageMethodeHelper.getPackage(getDataShieldProfile(profile), name);
  }

  /**
   * Get all the methods of the package.
   *
   * @return
   */
  @GET
  @Path("methods")
  public DataShield.DataShieldPackageMethodsDto getPackageMethods(@PathParam("name") String name, @QueryParam("profile") String profile) {
    return dsPackageMethodeHelper.getPackageMethods(getDataShieldProfile(profile), name);
  }

  /**
   * Publish all the methods of the package.
   *
   * @return the installed methods
   */
  @PUT
  @Path("methods")
  @Deprecated
  public DataShield.DataShieldPackageMethodsDto publishPackageMethods(@PathParam("name") String name, @QueryParam("profile") List<String> profiles) {
    DataShield.DataShieldPackageMethodsDto rval = DataShield.DataShieldPackageMethodsDto.newBuilder().setName(name).build();
    if (profiles != null)
      dsPackageMethodeHelper.publish(profiles.stream().map(this::getDataShieldProfile).collect(Collectors.toList()), name);
    return rval;
  }

  /**
   * Append package DataSHIELD settings to profile configuration.
   *
   * @param name
   * @param profiles
   * @return
   */
  @PUT
  @Path("_publish")
  public Response publishPackageSettings(@PathParam("name") String name, @QueryParam("profile") List<String> profiles) {
    if (profiles != null)
      dsPackageMethodeHelper.publish(profiles.stream().map(this::getDataShieldProfile).collect(Collectors.toList()), name);
    return Response.ok().build();
  }

  /**
   * Delete all settings of the package from the listed profiles.
   *
   * @param name
   * @param profiles
   * @return
   */
  @DELETE
  @Path("methods")
  @Deprecated
  public Response deletePackageMethods(@PathParam("name") String name, @QueryParam("profile") List<String> profiles) {
    if (profiles != null)
      dsPackageMethodeHelper.unpublish(profiles.stream().map(this::getDataShieldProfile).collect(Collectors.toList()), name);
    return Response.noContent().build();
  }

  /**
   * Remove package DataSHIELD settings from each profile configuration.
   *
   * @param name
   * @param profiles
   * @return
   */
  @DELETE
  @Path("_publish")
  public Response deletePackageSettings(@PathParam("name") String name, @QueryParam("profile") List<String> profiles) {
    if (profiles != null)
      dsPackageMethodeHelper.unpublish(profiles.stream().map(this::getDataShieldProfile).collect(Collectors.toList()), name);
    return Response.noContent().build();
  }

  /**
   * Silently deletes a package and its methods.
   *
   * @return
   */
  @DELETE
  public Response deletePackage(@PathParam("name") String name, @QueryParam("profile") String profile) {
    if (!opalGeneralConfigService.getConfig().isAllowRPackageManagement())
      return Response.status(Response.Status.FORBIDDEN).build();

    try {
      dsPackageMethodeHelper.deletePackage(getDataShieldProfile(profile), name);
    } catch (Exception e) {
      // ignored
    }
    return Response.ok().build();
  }

  private DataShieldProfile getDataShieldProfile(String profileName) {
    return datashieldProfileService.getProfile(profileName);
  }

}
