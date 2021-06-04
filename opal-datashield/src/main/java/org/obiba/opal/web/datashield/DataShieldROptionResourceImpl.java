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

import org.obiba.opal.datashield.cfg.DatashieldConfig;
import org.obiba.opal.datashield.cfg.DatashieldConfigService;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.web.model.DataShield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Component
@Transactional
@Scope("request")
@Path("/datashield/option")
public class DataShieldROptionResourceImpl implements DataShieldROptionResource {

  @Autowired
  private DatashieldConfigService datashieldConfigService;

  @Override
  public Response deleteDataShieldROption(String name, String profile) {
    DatashieldConfig config = datashieldConfigService.getConfiguration(profile);
    config.removeOption(name);
    datashieldConfigService.saveConfiguration(config);
    return Response.ok().build();
  }

  @Override
  public Response addOrUpdateDataShieldROption(String profile, final DataShield.DataShieldROptionDto dto) {
    DatashieldConfig config = datashieldConfigService.getConfiguration(profile);
    config.addOrUpdateOption(dto.getName(), dto.getValue());
    datashieldConfigService.saveConfiguration(config);
    return Response.ok().build();
  }

  @Override
  public Response getDataShieldROption(String name, String profile) {
    DatashieldConfig config = datashieldConfigService.getConfiguration(profile);

    if (config.hasOption(name)) {
      DataShield.DataShieldROptionDto dto = DataShield.DataShieldROptionDto.newBuilder().setName(name)
          .setValue(config.getOption(name).getValue()).build();

      return Response.ok().entity(dto).build();
    }

    return Response.status(Response.Status.NOT_FOUND).build();
  }
}
