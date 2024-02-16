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

import org.apache.shiro.SecurityUtils;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.web.model.DataShield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Component
@Transactional
@Scope("request")
@Path("/datashield/profile/{name}")
public class DataShieldProfileResource {

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @GET
  public DataShield.DataShieldProfileDto getProfile(@PathParam("name") String name) {
    return Dtos.asDto(getProfileInternal(name));
  }

  @PUT
  @Path("_enable")
  public Response enableProfile(@PathParam("name") String name) {
    doEnableProfile(name, true);
    return Response.ok().build();
  }

  @DELETE
  @Path("_enable")
  public Response disableProfile(@PathParam("name") String name) {
    doEnableProfile(name, false);
    return Response.ok().build();
  }

  @PUT
  @Path("_access")
  public Response restrictedAccessProfile(@PathParam("name") String name) {
    doRestrictAccessProfile(name, true);
    return Response.ok().build();
  }

  @DELETE
  @Path("_access")
  public Response unrestrictedAccessProfile(@PathParam("name") String name) {
    doRestrictAccessProfile(name, false);
    return Response.ok().build();
  }

  @PUT
  @Path("_rparser")
  public Response setRParser(@PathParam("name") String name, @QueryParam("version") String version) {
    doApplyRParserVersion(name, version);
    return Response.ok().build();
  }

  @DELETE
  @Path("_rparser")
  public Response removeRParser(@PathParam("name") String name) {
    doApplyRParserVersion(name, null);
    return Response.ok().build();
  }

  /**
   * Remove a secondary profile, primary ones are sticked to their cluster.
   *
   * @param name
   * @return
   */
  @DELETE
  public Response deleteProfile(@PathParam("name") String name, @QueryParam("force") @DefaultValue("false") boolean force) {
    if (datashieldProfileService.hasProfile(name)) {
      // primary profiles cannot be removed (unless forced, in case a cluster is obsolete)
      DataShieldProfile profile = datashieldProfileService.getProfile(name);
      if (force || !profile.getName().equals(profile.getCluster()))
        datashieldProfileService.deleteProfile(new DataShieldProfile(name));
    }
    return Response.noContent().build();
  }

  //
  // Private methods
  //

  private void doApplyRParserVersion(String name, String version) {
    DataShieldProfile profile = getProfileInternal(name);
    profile.setRParserVersion(version);
    datashieldProfileService.saveProfile(profile);
  }

  private void doEnableProfile(String name, boolean enabled) {
    DataShieldProfile profile = getProfileInternal(name);
    profile.setEnabled(enabled);
    datashieldProfileService.saveProfile(profile);
  }

  private void doRestrictAccessProfile(String name, boolean restricted) {
    DataShieldProfile profile = getProfileInternal(name);
    profile.setRestrictedAccess(restricted);
    datashieldProfileService.saveProfile(profile);
  }

  private DataShieldProfile getProfileInternal(String name) {
    DataShieldProfile profile = datashieldProfileService.findProfile(name);
    if (profile == null)
      throw new NotFoundException("No DataSHIELD profile with name: " + name);
    if (profile.isRestrictedAccess() && !SecurityUtils.getSubject().isPermitted(String.format("rest:/datashield/profile/%s:GET", profile.getName())))
      throw new ForbiddenException("DataSHIELD profile access is forbidden: " + profile.getName());
    return profile;
  }

}
