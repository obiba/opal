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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.obiba.datashield.core.DSConfiguration;
import org.obiba.datashield.core.DSEnvironment;
import org.obiba.datashield.core.DSMethod;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.web.model.DataShield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class DataShieldEnvironmentResourceImpl implements DataShieldEnvironmentResource {

  private DSMethodType methodType;

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @Override
  public void setMethodType(DSMethodType methodType) {
    this.methodType = methodType;
  }

  @Override
  public List<DataShield.DataShieldMethodDto> getDataShieldMethods(String profile) {
    List<DataShield.DataShieldMethodDto> dtos = Lists.newArrayList();
    for (DSMethod method : listMethods(profile)) {
      dtos.add(Dtos.asDto(method));
    }
    dtos.sort(Comparator.comparing(DataShield.DataShieldMethodDto::getName));
    return dtos;
  }

  @Override
  public Response deleteDataShieldMethods(List<String> names, String profile) {
    DataShieldProfile config = datashieldProfileService.getProfile(profile);
    List<DSMethod> methods = getEnvironment(config).getMethods().stream()
        .filter(m -> (names == null || names.isEmpty() || names.contains(m.getName())))
        .collect(Collectors.toList());
    if (!methods.isEmpty()) {
      methods.forEach(m -> getEnvironment(config).removeMethod(m.getName()));
      datashieldProfileService.saveProfile(config);
      if (names == null || names.isEmpty())
        DataShieldLog.adminLog("deleted all methods from type {}.", methodType);
      else
        DataShieldLog.adminLog("deleted methods from type {}: {}", methodType, Joiner.on(", ").join(names));
    }
    return Response.ok().build();
  }

  @Override
  public Response createDataShieldMethod(UriInfo uri, String profile, final DataShield.DataShieldMethodDto dto) {
    DataShieldProfile config = getDatashieldProfile(profile);
    if (getEnvironment(config).hasMethod(dto.getName())) return Response.status(Status.BAD_REQUEST).build();
    getEnvironment(config).addOrUpdate(Dtos.fromDto(dto));
    datashieldProfileService.saveProfile(config);
    DataShieldLog.adminLog("added method '{}' to environment {}.", dto.getName(), methodType);
    UriBuilder ub = UriBuilder.fromUri(uri.getRequestUri().resolve(""))
        .path(DataShieldEnvironmentResource.class, "getDataShieldMethod");
    return Response.created(ub.build(dto.getName())).build();
  }

  @Override
  public Response getDataShieldMethod(String name, String profile) {
    return Response.ok().entity(Dtos.asDto(getEnvironment(profile).getMethod(name))).build();
  }

  @Override
  public Response updateDataShieldMethod(String name, String profile, final DataShield.DataShieldMethodDto dto) {
    if (!name.equals(dto.getName())) return Response.status(Status.BAD_REQUEST).build();
    DataShieldProfile config = getDatashieldProfile(profile);
    if (!getEnvironment(config).hasMethod(name)) return Response.status(Status.NOT_FOUND).build();
    getEnvironment(config).addOrUpdate(Dtos.fromDto(dto));
    datashieldProfileService.saveProfile(config);
    DataShieldLog.adminLog("modified method '{}' in type {}.", name, methodType);
    return Response.ok().build();
  }

  @Override
  public Response deleteDataShieldMethod(final String name, String profile) {
    DataShieldProfile config = getDatashieldProfile(profile);
    getEnvironment(config).removeMethod(name);
    datashieldProfileService.saveProfile(config);
    DataShieldLog.adminLog("deleted method '{}' from type {}.", name, methodType);
    return Response.ok().build();
  }

  private Iterable<DSMethod> listMethods(String profile) {
    return getEnvironment(profile).getMethods();
  }

  private DataShieldProfile getDatashieldProfile(String profile) {
    return datashieldProfileService.getProfile(profile);
  }

  private DSEnvironment getEnvironment(String profile) {
    return getEnvironment(getDatashieldProfile(profile));
  }

  private DSEnvironment getEnvironment(DSConfiguration config) {
    return config.getEnvironment(methodType);
  }

}
