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

import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.web.model.DataShield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Scope("request")
@Path("/datashield/option")
public class DataShieldROptionResourceImpl implements DataShieldROptionResource {

  private DatashieldConfigurationSupplier configurationSupplier;

  @Autowired
  public void setConfigurationSupplier(DatashieldConfigurationSupplier configurationSupplier) {
    this.configurationSupplier = configurationSupplier;
  }

  @Override
  public Response deleteDataShieldROption(final @QueryParam("name") String name) {
    configurationSupplier
        .modify(config -> config.removeOption(name));

    return Response.ok().build();
  }

  @Override
  public Response addOrUpdateDataShieldROption(final DataShield.DataShieldROptionDto dto) {
    configurationSupplier
        .modify(config -> config.addOrUpdateOption(dto.getName(), dto.getValue()));

    return Response.ok().build();
  }

  @Override
  public Response getDataShieldROption(final @QueryParam("name") String name) {
    DatashieldConfiguration config = configurationSupplier.get();

    if (config.hasOption(name)) {
      DataShield.DataShieldROptionDto dto = DataShield.DataShieldROptionDto.newBuilder().setName(name)
          .setValue(config.getOption(name).getValue()).build();

      return Response.ok().entity(dto).build();
    }

    return Response.status(Response.Status.NOT_FOUND).build();
  }
}
