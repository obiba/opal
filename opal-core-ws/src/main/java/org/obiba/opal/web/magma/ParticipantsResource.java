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

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Component
@Path("/participants")
public class ParticipantsResource {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ParticipantsResource.class);

  private static final String PARTICIPANT_ENTITY_TYPE = "Participant";

  public ParticipantsResource() {
  }

  @GET
  @Path("/count")
  @NoAuthorization
  public Response getParticipantCount() {
    Set<Datasource> datasources = MagmaEngine.get().getDatasources();
    ImmutableSet.Builder<VariableEntity> participants = ImmutableSet.builder();
    for(Datasource ds : datasources) {
      for(ValueTable table : Iterables.filter(ds.getValueTables(), ParticipantEntityTypePredicate.INSTANCE)) {
        participants.addAll(table.getVariableEntities());
      }
    }
    return Response.ok(String.valueOf(participants.build().size())).build();
  }

  /**
   * Predicate that returns true when the entity type of a ValueTable is Participant.
   */
  private static final class ParticipantEntityTypePredicate implements Predicate<ValueTable> {

    private static final ParticipantEntityTypePredicate INSTANCE = new ParticipantEntityTypePredicate();

    @Override
    public boolean apply(ValueTable input) {
      return input.isForEntityType(PARTICIPANT_ENTITY_TYPE);
    }

  }
}
