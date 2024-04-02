/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.services.ServiceConfigurationHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/service/search/cfg/enabled")
public class SearchServiceConfigResource {

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private ServiceConfigurationHandlerRegistry configHandler;

  @PUT
  public Response startEnable() {

    Service service = opalRuntime.getService("search");

    Opal.ServiceCfgDto dto = configHandler.get(service.getConfig(), "search");
    Opal.ESCfgDto esDto = dto.getExtension(Opal.ESCfgDto.params).toBuilder().setEnabled(true).build();
    configHandler.put(dto.toBuilder().setExtension(Opal.ESCfgDto.params, esDto).build(), "search");

    service.start();

    return Response.ok().build();
  }

  @DELETE
  public Response stopDisable() {

    Service service = opalRuntime.getService("search");

    Opal.ServiceCfgDto dto = configHandler.get(service.getConfig(), "search");
    Opal.ESCfgDto esDto = dto.getExtension(Opal.ESCfgDto.params).toBuilder().setEnabled(false).build();
    configHandler.put(dto.toBuilder().setExtension(Opal.ESCfgDto.params, esDto).build(), "search");

    service.stop();
    return Response.ok().build();
  }
}
