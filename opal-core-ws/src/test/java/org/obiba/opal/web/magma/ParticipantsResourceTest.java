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

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;

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
    testGetParticipantCount(new HashSet<ValueTable>(), 0);
  }

  @Test
  public void testGetParticipantCount_WithSingleTable() {
    Set<ValueTable> tables = ImmutableSet.of( //
    /**/createMockTable(ImmutableSet.of(createMockEntity("P1"))) //
    );

    testGetParticipantCount(tables, 1);
  }

  @Test
  public void testGetParticipantCount_WithMultipleTablesDoesNotCountTheSameParticipantTwice() {
    Set<ValueTable> tables = ImmutableSet.of( //
    /**/createMockTable(ImmutableSet.of(createMockEntity("P1"))), //
    /**/createMockTable(ImmutableSet.of(createMockEntity("P1"), createMockEntity("P2"))) //
    );

    testGetParticipantCount(tables, 2);
  }

  //
  // Helper Methods
  //

  private ParticipantsResource createParticipantsResource() {
    return new ParticipantsResource();
  }

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

    ParticipantsResource sut = createParticipantsResource();
    Response response = sut.getParticipantCount();

    MagmaEngine.get().removeDatasource(mockDatasource);

    // Verify mocks
    verify(mockDatasource);

    // Verify response
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    assertEquals(String.valueOf(expectedCount), response.getEntity());
  }

  private ValueTable createMockTable(Set<VariableEntity> entities) {
    ValueTable mockTable = createMock(ValueTable.class);

    expect(mockTable.isForEntityType("Participant")).andReturn(true).anyTimes();
    expect(mockTable.getVariableEntities()).andReturn(entities).anyTimes();

    for(VariableEntity entity : entities) {
      expect(mockTable.hasValueSet(entity)).andReturn(true).anyTimes();
    }

    replay(mockTable);

    return mockTable;
  }

  private VariableEntity createMockEntity(String identifier) {
    return new VariableEntityBean("Participant", identifier);
  }
}
