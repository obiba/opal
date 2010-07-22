/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Path("/participants")
public class ParticipantsResource {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ParticipantsResource.class);

  @GET
  @Path("/count")
  public Response getParticipantCount() {
    Set<String> participantIdentifiers = new HashSet<String>();

    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      for(ValueTable valueTable : datasource.getValueTables()) {
        if(valueTable.isForEntityType("Participant")) {
          for(VariableEntity entity : valueTable.getVariableEntities()) {
            if(valueTable.hasValueSet(entity)) {
              participantIdentifiers.add(entity.getIdentifier());
            }
          }
        }
      }
    }

    return Response.ok(String.valueOf(participantIdentifiers.size())).build();
  }
}
