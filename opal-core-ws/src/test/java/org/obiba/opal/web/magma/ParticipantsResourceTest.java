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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

import com.google.common.collect.ImmutableSet;

/**
 *
 */
public class ParticipantsResourceTest extends AbstractMagmaResourceTest {
  //
  // Test Methods
  //

  @Test
  public void testGetParticipantCount_ReturnsZeroWhenThereAreNoTables() {
    // testGetParticipantCount(new HashSet<ValueTable>(), 0);
  }

  // @Test
  public void testGetParticipantCount_ReturnsZeroWhenThereAreNoParticipantTables() {
    testGetParticipantCount(ImmutableSet.of(createMockTable(ImmutableSet.of(createMockEntity("Instrument", "I1")), "Instrument", "Participant")), 0);
  }

  // @Test
  public void testGetParticipantCount() {
    testGetParticipantCount(ImmutableSet.of(createMockTable(ImmutableSet.of(createMockEntity("Participant", "P1"), createMockEntity("Participant", "P2")), "Participant", "Instrument"), createMockTable(ImmutableSet.of(createMockEntity("Instrument", "I1")), "Instrument", "Participant"), createMockTable(ImmutableSet.of(createMockEntity("Participant", "P1")), "Participant", "Instrument")), 2);
  }

  //
  // Helper Methods
  //

  private void testGetParticipantCount(Set<ValueTable> tables, int expectedCount) {
    // Setup
    Datasource mockDatasource = createMock(Datasource.class);
    expect(mockDatasource.getName()).andReturn("mockDatasource").anyTimes();
    mockDatasource.initialise();
    expectLastCall().once();
    expect(mockDatasource.getValueTables()).andReturn(tables).once();
    mockDatasource.dispose();
    expectLastCall().atLeastOnce();

    replay(mockDatasource);

    // Exercise
    MagmaEngine.get().addDatasource(mockDatasource);

    ParticipantsResource sut = new ParticipantsResource("keysDs");
    Response response = sut.getParticipantCount();

    MagmaEngine.get().removeDatasource(mockDatasource);

    // Verify mocks
    verify(mockDatasource);

    // Verify response
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    assertEquals(String.valueOf(expectedCount), response.getEntity());
  }

  private ValueTable createMockTable(Set<VariableEntity> entities, String type, String... otherTypes) {
    ValueTable mockTable = createMock(ValueTable.class);

    expect(mockTable.isForEntityType(type)).andReturn(true).anyTimes();
    for(String otherType : otherTypes) {
      expect(mockTable.isForEntityType(otherType)).andReturn(false).anyTimes();
    }
    expect(mockTable.getVariableEntities()).andReturn(entities).anyTimes();

    for(VariableEntity entity : entities) {
      expect(mockTable.hasValueSet(entity)).andReturn(true).anyTimes();
    }

    replay(mockTable);

    return mockTable;
  }

  private VariableEntity createMockEntity(String type, String identifier) {
    VariableEntity mockEntity = createMock(VariableEntity.class);

    expect(mockEntity.getType()).andReturn(type).anyTimes();
    expect(mockEntity.getIdentifier()).andReturn(identifier).anyTimes();

    replay(mockEntity);

    return mockEntity;
  }
}
