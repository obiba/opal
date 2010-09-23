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
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.obiba.magma.ValueTable;
import org.obiba.magma.views.View;

/**
 * Unit tests for {@link ViewResource}.
 */
public class ViewResourceTest {
  //
  // Test Methods
  //

  @Test
  public void testConstructor_CallsSuperConstructorWithViewArgument() {
    // Setup
    View view = new View();

    // Exercise
    ViewResource sut = new ViewResource(view);

    // Verify
    assertEquals(view, sut.getValueTable());
  }

  @Test
  public void testGetFrom_ReturnsTableResourceForWrappedTable() {
    // Setup
    ValueTable fromTableMock = createMock(ValueTable.class);
    expect(fromTableMock.getName()).andReturn("fromTable").atLeastOnce();

    View view = new View("testView", fromTableMock);
    ViewResource sut = new ViewResource(view);

    replay(fromTableMock);

    // Exercise
    TableResource fromTableResource = sut.getFrom();

    // Verify state
    assertEquals("fromTable", fromTableResource.getValueTable().getName());
  }
}
