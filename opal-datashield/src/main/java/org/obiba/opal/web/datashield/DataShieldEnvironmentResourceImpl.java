/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.datashield;

import com.google.common.collect.Lists;
import org.obiba.datashield.core.DSEnvironment;
import org.obiba.datashield.core.DSMethod;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier.ExtensionConfigModificationTask;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.web.datashield.support.DataShieldMethodConverterRegistry;
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

  private DSMethodType methodType;

  private DatashieldConfigurationSupplier configurationSupplier;

  private DataShieldMethodConverterRegistry methodConverterRegistry;

  @Override
  public void setMethodType(DSMethodType methodType) {
    this.methodType = methodType;
  }

  @Autowired
  public void setConfigurationSupplier(DatashieldConfigurationSupplier configurationSupplier) {
    this.configurationSupplier = configurationSupplier;
  }

  @Autowired
  public void setMethodConverterRegistry(DataShieldMethodConverterRegistry methodConverterRegistry) {
    this.methodConverterRegistry = methodConverterRegistry;
  }

  @Override
  public List<DataShield.DataShieldMethodDto> getDataShieldMethods() {
    List<DataShield.DataShieldMethodDto> dtos = Lists.newArrayList();
    for(DSMethod method : listMethods()) {
      dtos.add(methodConverterRegistry.asDto(method));
    }
    sortByName(dtos);
    return dtos;
  }

  @Override
  public Response deleteDataShieldMethods() {
    configurationSupplier.modify(new ExtensionConfigModificationTask<DatashieldConfiguration>() {

      @Override
      public void doWithConfig(DatashieldConfiguration config) {
        for(DSMethod method : getEnvironment(config).getMethods()) {
          getEnvironment(config).removeMethod(method.getName());
        }
      }

    });

    DataShieldLog.adminLog("deleted all methods from type {}.", methodType);
    return Response.ok().build();
  }

  @Override
  public Response createDataShieldMethod(UriInfo uri, final DataShield.DataShieldMethodDto dto) {
    DatashieldConfiguration config = getDatashieldConfiguration();
    if(getEnvironment(config).hasMethod(dto.getName())) return Response.status(Status.BAD_REQUEST).build();

    configurationSupplier.modify(new ExtensionConfigModificationTask<DatashieldConfiguration>() {

      @Override
      public void doWithConfig(DatashieldConfiguration config) {
        getEnvironment(config).addOrUpdate(methodConverterRegistry.parse(dto));
      }
    });
    DataShieldLog.adminLog("added method '{}' to environment {}.", dto.getName(), methodType);
    UriBuilder ub = UriBuilder.fromUri(uri.getRequestUri().resolve(""))
        .path(DataShieldEnvironmentResource.class, "getDataShieldMethod");
    return Response.created(ub.build(dto.getName())).build();
  }

  @Override
  public Response getDataShieldMethod(String name) {
    return Response.ok().entity(methodConverterRegistry.asDto(getEnvironment().getMethod(name))).build();
  }

  @Override
  public Response updateDataShieldMethod(String name, final DataShield.DataShieldMethodDto dto) {
    if(!name.equals(dto.getName())) return Response.status(Status.BAD_REQUEST).build();

    DatashieldConfiguration config = getDatashieldConfiguration();
    if(!getEnvironment(config).hasMethod(name)) return Response.status(Status.NOT_FOUND).build();

    configurationSupplier.modify(new ExtensionConfigModificationTask<DatashieldConfiguration>() {

      @Override
      public void doWithConfig(DatashieldConfiguration config) {
        getEnvironment(config).addOrUpdate(methodConverterRegistry.parse(dto));
      }
    });

    DataShieldLog.adminLog("modified method '{}' in type {}.", name, methodType);

    return Response.ok().build();
  }

  @Override
  public Response deleteDataShieldMethod(final String name) {

    configurationSupplier.modify(new ExtensionConfigModificationTask<DatashieldConfiguration>() {

      @Override
      public void doWithConfig(DatashieldConfiguration config) {
        getEnvironment(config).removeMethod(name);
      }
    });
    DataShieldLog.adminLog("deleted method '{}' from type {}.", name, methodType);
    return Response.ok().build();
  }

  private Iterable<DSMethod> listMethods() {
    return getEnvironment().getMethods();
  }

  private DatashieldConfiguration getDatashieldConfiguration() {
    return configurationSupplier.get();
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

  private DSEnvironment getEnvironment(DatashieldConfiguration config) {
    return config.getEnvironment(methodType);
  }

}
