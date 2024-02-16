/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;

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
        createMockTable(ImmutableList.of(createMockEntity("P1"))) //
    );

    testGetParticipantCount(tables, 1);
  }

  @Test
  public void testGetParticipantCount_WithMultipleTablesDoesNotCountTheSameParticipantTwice() {
    Set<ValueTable> tables = ImmutableSet.of( //
        createMockTable(ImmutableList.of(createMockEntity("P1"))), //
        createMockTable(ImmutableList.of(createMockEntity("P1"), createMockEntity("P2"))) //
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
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(String.valueOf(expectedCount));
  }

  private ValueTable createMockTable(List<VariableEntity> entities) {
    ValueTable mockTable = createMock(ValueTable.class);

    expect(mockTable.isForEntityType("Participant")).andReturn(true).anyTimes();
    expect(mockTable.getVariableEntities()).andReturn(entities).anyTimes();

    for (VariableEntity entity : entities) {
      expect(mockTable.hasValueSet(entity)).andReturn(true).anyTimes();
    }

    replay(mockTable);

    return mockTable;
  }

  private VariableEntity createMockEntity(String identifier) {
    return new VariableEntityBean("Participant", identifier);
  }
}
