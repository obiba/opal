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
import org.obiba.datashield.core.DSMethodType;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.web.model.DataShield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Transactional
@Path("/datashield/profiles")
public class DataShieldProfilesResource {

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @GET
  public List<DataShield.DataShieldProfileDto> getProfiles(@QueryParam("enabled") Boolean enabled) {
    return datashieldProfileService.getProfiles().stream()
        .filter(this::canAccessProfile)
        .filter(p -> enabled == null || (enabled && p.isEnabled()) || (!enabled && !p.isEnabled()))
        .map(Dtos::asDto)
        .collect(Collectors.toList());
  }

  @POST
  public Response addProfile(DataShield.DataShieldProfileDto profileDto) {
    if (profileDto == null)
      throw new BadRequestException("DataSHIELD profile is missing");
    if (datashieldProfileService.hasProfile(profileDto.getName()))
      throw new BadRequestException("DataSHIELD profile already exists: " + profileDto.getName());

    DataShieldProfile profile = Dtos.fromDto(profileDto);
    // init with cluster's primary profile
    if (!profileDto.getName().equals(profileDto.getCluster())) {
      DataShieldProfile baseProfile = datashieldProfileService.getProfile(profileDto.getCluster());
      profile.addOrUpdateMethods(DSMethodType.AGGREGATE, baseProfile.getEnvironment(DSMethodType.AGGREGATE).getMethods());
      profile.addOrUpdateMethods(DSMethodType.ASSIGN, baseProfile.getEnvironment(DSMethodType.ASSIGN).getMethods());
      StreamSupport.stream(baseProfile.getOptions().spliterator(), false).forEach(o -> profile.addOrUpdateOption(o.getName(), o.getValue()));
    }

    datashieldProfileService.saveProfile(profile);

    URI profileUri = UriBuilder.fromPath("/datashield/profile/" + profileDto.getName()).build();
    return Response.created(profileUri).build();
  }

  private boolean canAccessProfile(DataShieldProfile profile) {
    // regular users cannot see disabled profiles
    if (!profile.isEnabled() && !SecurityUtils.getSubject().isPermitted("rest:/datashield/profiles:POST"))
      return false;
    if (!profile.isRestrictedAccess()) return true;
    return SecurityUtils.getSubject().isPermitted(String.format("rest:/datashield/profile/%s:GET", profile.getName()));
  }

}
