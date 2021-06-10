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

import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.web.model.DataShield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Transactional
@Path("/datashield/profiles")
public class DataShieldProfilesResource {

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @GET
  public List<DataShield.DataShieldProfileDto> getProfiles() {
    return datashieldProfileService.getProfiles().stream()
        .map(Dtos::asDto)
        .collect(Collectors.toList());
  }

  @POST
  public Response addProfile(DataShield.DataShieldProfileDto profileDto) {
    if (profileDto == null)
      throw new BadRequestException("DataSHIELD profile is missing");
    if (datashieldProfileService.hasProfile(profileDto.getName()))
      throw new BadRequestException("DataSHIELD profile already exists: " + profileDto.getName());

    datashieldProfileService.saveProfile(Dtos.fromDto(profileDto));

    URI profileUri = UriBuilder.fromPath("/datashield/profile/" + profileDto.getName()).build();
    return Response.created(profileUri).build();
  }

}
