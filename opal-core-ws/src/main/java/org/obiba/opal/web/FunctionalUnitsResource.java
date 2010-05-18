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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.web.model.Opal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Path("/functional-units")
public class FunctionalUnitsResource {

  @Autowired
  private OpalRuntime opalRuntime;

  private static final Logger log = LoggerFactory.getLogger(FunctionalUnitsResource.class);

  @GET
  public List<Opal.FunctionalUnitDto> getFunctionalUnits() {
    final List<Opal.FunctionalUnitDto> functionalUnits = Lists.newArrayList();
    for(FunctionalUnit functionalUnit : opalRuntime.getFunctionalUnits()) {
      Opal.FunctionalUnitDto.Builder fuBuilder = Opal.FunctionalUnitDto.newBuilder().setName(functionalUnit.getName()).setKeyVariableName(functionalUnit.getKeyVariableName());
      functionalUnits.add(fuBuilder.build());
    }
    return functionalUnits;
  }

}
