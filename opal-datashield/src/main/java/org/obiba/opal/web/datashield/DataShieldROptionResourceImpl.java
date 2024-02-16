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
import org.obiba.opal.web.model.DataShield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Component
@Transactional
@Scope("request")
@Path("/datashield/option")
public class DataShieldROptionResourceImpl implements DataShieldROptionResource {

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @Override
  public Response deleteDataShieldROption(String name, String profile) {
    DataShieldProfile config = getDataShieldProfile(profile);
    config.removeOption(name);
    datashieldProfileService.saveProfile(config);
    return Response.ok().build();
  }

  @Override
  public Response addOrUpdateDataShieldROption(String profile, final DataShield.DataShieldROptionDto dto) {
    DataShieldProfile config = getDataShieldProfile(profile);
    config.addOrUpdateOption(dto.getName(), dto.getValue());
    datashieldProfileService.saveProfile(config);
    return Response.ok().build();
  }

  @Override
  public Response getDataShieldROption(String name, String profile) {
    DataShieldProfile config = getDataShieldProfile(profile);

    if (config.hasOption(name)) {
      DataShield.DataShieldROptionDto dto = DataShield.DataShieldROptionDto.newBuilder().setName(name)
          .setValue(config.getOption(name).getValue()).build();

      return Response.ok().entity(dto).build();
    }

    return Response.status(Response.Status.NOT_FOUND).build();
  }

  private DataShieldProfile getDataShieldProfile(String profileName) {
    return datashieldProfileService.getProfile(profileName);
  }
}
