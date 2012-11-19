/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.services;

import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.ServiceDto;
import org.obiba.opal.web.model.Opal.ServiceStatus;

import com.google.common.collect.Lists;

@Component
@Scope("request")
@Path("/services")
public class ServicesResource {

  @Autowired
  private Set<Service> services;

  @Autowired
  public ServicesResource(Set<Service> services) {
    this.services = services;
  }

  @GET
  public List<Opal.ServiceDto> services() {
    final List<Opal.ServiceDto> serviceDtos = Lists.newArrayList();

    for(Service service : services) {
      Opal.ServiceStatus status = service.isRunning() ? Opal.ServiceStatus.RUNNING : Opal.ServiceStatus.STOPPED;
      String link = "/service/" + service.getName();
      Opal.ServiceDto dto = Opal.ServiceDto.newBuilder().setName(service.getName()).setStatus(status).setLink(link)
          .build();
      serviceDtos.add(dto);
    }

    return serviceDtos;
  }

}
