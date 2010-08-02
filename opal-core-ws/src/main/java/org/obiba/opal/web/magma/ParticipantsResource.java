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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

@Component
@Path("/participants")
public class ParticipantsResource {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ParticipantsResource.class);

  private final String keysDatasourceName;

  @Autowired
  public ParticipantsResource(@Value("${org.obiba.opal.keys.tableReference}") String keysTableReference) {
    keysDatasourceName = MagmaEngineTableResolver.valueOf(keysTableReference).getDatasourceName();
    if(keysDatasourceName == null) {
      throw new IllegalArgumentException("invalid keys table reference");
    }
  }

  @GET
  @Path("/count")
  public Response getParticipantCount() {
    Datasource keysDs = MagmaEngine.get().getDatasource(keysDatasourceName);
    ImmutableSet.Builder<String> participants = ImmutableSet.builder();
    for(ValueTable table : keysDs.getValueTables()) {
      for(VariableEntity entity : table.getVariableEntities()) {
        participants.add(entity.getIdentifier());
      }
    }
    return Response.ok(String.valueOf(participants.build().size())).build();
  }
}
