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

import org.obiba.opal.datashield.cfg.DatashieldProfile;
import org.obiba.opal.datashield.cfg.DatashieldProfileService;
import org.obiba.opal.web.datashield.support.DataShieldPackageMethodHelper;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Manage a Datashield Package.
 */
@Component
@Transactional
@Scope("request")
@Path("/datashield/package/{name}")
public class DataShieldPackageResource {

  @Autowired
  private DatashieldProfileService datashieldProfileService;

  @Autowired
  private DataShieldPackageMethodHelper dsPackageMethodeHelper;

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
  public DataShield.DataShieldPackageMethodsDto publishPackageMethods(@PathParam("name") String name, @QueryParam("profile") String profile) {
    return dsPackageMethodeHelper.publish(getDataShieldProfile(profile), name);
  }

  /**
   * Append package DataSHIELD settings to profile configuration.
   *
   * @param name
   * @param profile
   * @return
   */
  @PUT
  @Path("_publish")
  public Response publishPackageSettings(@PathParam("name") String name, @QueryParam("profile") String profile) {
    dsPackageMethodeHelper.publish(getDataShieldProfile(profile), name);
    return Response.ok().build();
  }

  /**
   * Delete all methods of the package.
   *
   * @param name
   * @param profile
   * @return
   */
  @DELETE
  @Path("methods")
  @Deprecated
  public Response deletePackageMethods(@PathParam("name") String name, @QueryParam("profile") String profile) {
    dsPackageMethodeHelper.unpublish(getDataShieldProfile(profile), name);
    return Response.noContent().build();
  }

  /**
   * Remove package DataSHIELD settings from profile configuration.
   *
   * @param name
   * @param profile
   * @return
   */
  @DELETE
  @Path("_publish")
  public Response deletePackageSettings(@PathParam("name") String name, @QueryParam("profile") String profile) {
    dsPackageMethodeHelper.unpublish(getDataShieldProfile(profile), name);
    return Response.noContent().build();
  }

  /**
   * Silently deletes a package and its methods.
   *
   * @return
   */
  @DELETE
  public Response deletePackage(@PathParam("name") String name, @QueryParam("profile") String profile) {
    try {
      dsPackageMethodeHelper.deletePackage(getDataShieldProfile(profile), name);
    } catch (Exception e) {
      // ignored
    }
    return Response.ok().build();
  }

  private DatashieldProfile getDataShieldProfile(String profileName) {
    return datashieldProfileService.getProfile(profileName);
  }

}
