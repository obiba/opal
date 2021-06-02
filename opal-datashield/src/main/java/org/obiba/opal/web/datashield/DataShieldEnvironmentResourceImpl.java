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

import com.google.common.collect.Lists;
import org.obiba.datashield.core.DSConfiguration;
import org.obiba.datashield.core.DSEnvironment;
import org.obiba.datashield.core.DSMethod;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.cfg.DatashieldConfig;
import org.obiba.opal.datashield.cfg.DatashieldConfigService;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.web.model.DataShield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class DataShieldEnvironmentResourceImpl implements DataShieldEnvironmentResource {

  private String profile = RServerManagerService.DEFAULT_CLUSTER_NAME;

  private DSMethodType methodType;

  @Autowired
  private DatashieldConfigService datashieldConfigService;

  @Override
  public void setProfile(String profile) {
    this.profile = profile;
  }

  @Override
  public void setMethodType(DSMethodType methodType) {
    this.methodType = methodType;
  }

  @Override
  public List<DataShield.DataShieldMethodDto> getDataShieldMethods() {
    List<DataShield.DataShieldMethodDto> dtos = Lists.newArrayList();
    for (DSMethod method : listMethods()) {
      dtos.add(Dtos.asDto(method));
    }
    sortByName(dtos);
    return dtos;
  }

  @Override
  public Response deleteDataShieldMethods() {
    DatashieldConfig config = datashieldConfigService.getConfiguration(profile);
    for (DSMethod method : getEnvironment(config).getMethods()) {
      getEnvironment(config).removeMethod(method.getName());
    }
    datashieldConfigService.saveConfiguration(config);
    DataShieldLog.adminLog("deleted all methods from type {}.", methodType);
    return Response.ok().build();
  }

  @Override
  public Response createDataShieldMethod(UriInfo uri, final DataShield.DataShieldMethodDto dto) {
    DatashieldConfig config = getDatashieldConfiguration();
    if (getEnvironment(config).hasMethod(dto.getName())) return Response.status(Status.BAD_REQUEST).build();
    getEnvironment(config).addOrUpdate(Dtos.fromDto(dto));
    datashieldConfigService.saveConfiguration(config);
    DataShieldLog.adminLog("added method '{}' to environment {}.", dto.getName(), methodType);
    UriBuilder ub = UriBuilder.fromUri(uri.getRequestUri().resolve(""))
        .path(DataShieldEnvironmentResource.class, "getDataShieldMethod");
    return Response.created(ub.build(dto.getName())).build();
  }

  @Override
  public Response getDataShieldMethod(String name) {
    return Response.ok().entity(Dtos.asDto(getEnvironment().getMethod(name))).build();
  }

  @Override
  public Response updateDataShieldMethod(String name, final DataShield.DataShieldMethodDto dto) {
    if (!name.equals(dto.getName())) return Response.status(Status.BAD_REQUEST).build();
    DatashieldConfig config = getDatashieldConfiguration();
    if (!getEnvironment(config).hasMethod(name)) return Response.status(Status.NOT_FOUND).build();
    getEnvironment(config).addOrUpdate(Dtos.fromDto(dto));
    datashieldConfigService.saveConfiguration(config);
    DataShieldLog.adminLog("modified method '{}' in type {}.", name, methodType);
    return Response.ok().build();
  }

  @Override
  public Response deleteDataShieldMethod(final String name) {
    DatashieldConfig config = getDatashieldConfiguration();
    getEnvironment(config).removeMethod(name);
    datashieldConfigService.saveConfiguration(config);
    DataShieldLog.adminLog("deleted method '{}' from type {}.", name, methodType);
    return Response.ok().build();
  }

  private Iterable<DSMethod> listMethods() {
    return getEnvironment().getMethods();
  }

  private DatashieldConfig getDatashieldConfiguration() {
    return datashieldConfigService.getConfiguration(profile);
  }

  private void sortByName(List<DataShield.DataShieldMethodDto> dtos) {
    // sort alphabetically
    Collections.sort(dtos, new Comparator<DataShield.DataShieldMethodDto>() {

      @Override
      public int compare(DataShield.DataShieldMethodDto d1, DataShield.DataShieldMethodDto d2) {
        return d1.getName().compareTo(d2.getName());
      }

    });
  }

  private DSEnvironment getEnvironment() {
    return getEnvironment(getDatashieldConfiguration());
  }

  private DSEnvironment getEnvironment(DSConfiguration config) {
    return config.getEnvironment(methodType);
  }

}
