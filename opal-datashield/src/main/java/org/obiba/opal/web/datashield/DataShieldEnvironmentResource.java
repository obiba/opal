/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.datashield;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier.ExtensionConfigModificationTask;
import org.obiba.opal.datashield.DataShieldEnvironment;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.DataShieldMethod;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.web.datashield.support.DataShieldMethodConverterRegistry;
import org.obiba.opal.web.model.DataShield;

import com.google.common.collect.Lists;

public class DataShieldEnvironmentResource {

  private final DatashieldConfiguration.Environment environment;

  private final DatashieldConfigurationSupplier configurationSupplier;

  private final DataShieldMethodConverterRegistry methodConverterRegistry;

  DataShieldEnvironmentResource(DatashieldConfiguration.Environment environment,
      DatashieldConfigurationSupplier configurationSupplier,
      DataShieldMethodConverterRegistry methodConverterRegistry) {
    this.environment = environment;
    this.configurationSupplier = configurationSupplier;
    this.methodConverterRegistry = methodConverterRegistry;
  }

  @GET
  @Path("/methods")
  public List<DataShield.DataShieldMethodDto> getDataShieldMethods() {
    final List<DataShield.DataShieldMethodDto> dtos = Lists.newArrayList();
    for(DataShieldMethod method : listMethods()) {
      dtos.add(methodConverterRegistry.asDto(method));
    }
    sortByName(dtos);
    return dtos;
  }

  @DELETE
  @Path("/methods")
  public Response deleteDataShieldMethods() {
    configurationSupplier.modify(new ExtensionConfigModificationTask<DatashieldConfiguration>() {

      @Override
      public void doWithConfig(DatashieldConfiguration config) {
        for(DataShieldMethod method : getEnvironment(config).getMethods()) {
          getEnvironment(config).removeMethod(method.getName());
        }
      }

    });

    DataShieldLog.adminLog("deleted all methods from environment {}.", this.environment);
    return Response.ok().build();
  }

  @POST
  @Path("/methods")
  public Response createDataShieldMethod(@Context UriInfo uri, final DataShield.DataShieldMethodDto dto) {
    DatashieldConfiguration config = getDatashieldConfiguration();
    if(getEnvironment(config).hasMethod(dto.getName())) return Response.status(Status.BAD_REQUEST).build();

    configurationSupplier.modify(new ExtensionConfigModificationTask<DatashieldConfiguration>() {

      @Override
      public void doWithConfig(DatashieldConfiguration config) {
        getEnvironment(config).addMethod(methodConverterRegistry.parse(dto));
      }
    });
    DataShieldLog.adminLog("added method '{}' to environment {}.", dto.getName(), this.environment);
    UriBuilder ub = UriBuilder.fromUri(uri.getRequestUri().resolve(""))
        .path(DataShieldEnvironmentResource.class, "getDataShieldMethod");
    return Response.created(ub.build(dto.getName())).build();
  }

  @GET
  @Path("/method/{name}")
  public Response getDataShieldMethod(@PathParam("name") String name) {
    return Response.ok().entity(methodConverterRegistry.asDto(getEnvironment().getMethod(name))).build();
  }

  @PUT
  @Path("/method/{name}")
  public Response updateDataShieldMethod(@PathParam("name") String name, final DataShield.DataShieldMethodDto dto) {
    if(!name.equals(dto.getName())) return Response.status(Status.BAD_REQUEST).build();

    DatashieldConfiguration config = getDatashieldConfiguration();
    if(!getEnvironment(config).hasMethod(name)) return Response.status(Status.NOT_FOUND).build();

    configurationSupplier.modify(new ExtensionConfigModificationTask<DatashieldConfiguration>() {

      @Override
      public void doWithConfig(DatashieldConfiguration config) {
        getEnvironment(config).addMethod(methodConverterRegistry.parse(dto));
      }
    });

    DataShieldLog.adminLog("modified method '{}' in environment {}.", name, this.environment);

    return Response.ok().build();
  }

  @DELETE
  @Path("/method/{name}")
  public Response deleteDataShieldMethod(final @PathParam("name") String name) {

    configurationSupplier.modify(new ExtensionConfigModificationTask<DatashieldConfiguration>() {

      @Override
      public void doWithConfig(DatashieldConfiguration config) {
        getEnvironment(config).removeMethod(name);
      }
    });
    DataShieldLog.adminLog("deleted method '{}' from environment {}.", name, this.environment);
    return Response.ok().build();
  }

  private Iterable<DataShieldMethod> listMethods() {
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

  private DataShieldEnvironment getEnvironment() {
    return getEnvironment(getDatashieldConfiguration());
  }

  private DataShieldEnvironment getEnvironment(DatashieldConfiguration config) {
    return config.getEnvironment(this.environment);
  }

}
