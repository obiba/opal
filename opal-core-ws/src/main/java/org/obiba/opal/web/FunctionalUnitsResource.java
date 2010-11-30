/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Opal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Path("/functional-units")
public class FunctionalUnitsResource {

  private static final Logger log = LoggerFactory.getLogger(FunctionalUnitsResource.class);

  private OpalRuntime opalRuntime;

  private final UnitKeyStoreService unitKeyStoreService;

  @Autowired
  public FunctionalUnitsResource(OpalRuntime opalRuntime, UnitKeyStoreService unitKeyStoreService) {
    super();
    this.opalRuntime = opalRuntime;
    this.unitKeyStoreService = unitKeyStoreService;
  }

  @GET
  public List<Opal.FunctionalUnitDto> getFunctionalUnits() {
    final List<Opal.FunctionalUnitDto> functionalUnits = Lists.newArrayList();
    for(FunctionalUnit functionalUnit : opalRuntime.getFunctionalUnits()) {
      Opal.FunctionalUnitDto.Builder fuBuilder = Opal.FunctionalUnitDto.newBuilder().setName(functionalUnit.getName()).setKeyVariableName(functionalUnit.getKeyVariableName());
      if(functionalUnit.getSelect() instanceof JavascriptClause) {
        fuBuilder.setSelect(((JavascriptClause) functionalUnit.getSelect()).getScript());
      }
      functionalUnits.add(fuBuilder.build());
    }
    sortByName(functionalUnits);
    return functionalUnits;
  }

  @POST
  public Response createFunctionalUnit(Opal.FunctionalUnitDto unit) {
    if(opalRuntime.getOpalConfiguration().hasFunctionalUnit(unit.getName())) {
      return Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "FunctionalUnitAlreadyExists").build()).build();
    }

    ResponseBuilder response = null;
    try {
      FunctionalUnit functionalUnit = new FunctionalUnit(unit.getName(), unit.getKeyVariableName());
      if(unit.hasSelect()) {
        functionalUnit.setSelect(new JavascriptClause(unit.getSelect()));
      }
      functionalUnit.setUnitKeyStoreService(unitKeyStoreService);

      opalRuntime.getOpalConfiguration().addOrReplaceFunctionalUnit(functionalUnit);
      opalRuntime.writeOpalConfiguration();
      response = Response.created(UriBuilder.fromPath("/").path(FunctionalUnitResource.class).build(unit.getName()));
    } catch(RuntimeException e) {
      response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "FunctionalUnitCreationFailed", e).build());
    }

    return response.build();
  }

  private void sortByName(List<Opal.FunctionalUnitDto> units) {
    // sort alphabetically
    Collections.sort(units, new Comparator<Opal.FunctionalUnitDto>() {

      @Override
      public int compare(Opal.FunctionalUnitDto d1, Opal.FunctionalUnitDto d2) {
        return d1.getName().compareTo(d2.getName());
      }

    });
  }

}
