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
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.magma.type.IntegerType;
import org.obiba.opal.web.magma.support.InvalidRequestException;
import org.obiba.opal.web.magma.support.PagingVectorSource;
import org.obiba.opal.web.model.Magma.ValueDto;

import com.google.common.collect.ImmutableList;

/**
 * Unit tests for {@link VariableResource}.
 */
public class VariableResourceTest extends AbstractMagmaResourceTest {
  //
  // Test Methods
  //

  @Test
  public void testGetValues() {
    // Setup
    final PagingVectorSource mockPagingVectorSource = createMock(PagingVectorSource.class);
    expect(mockPagingVectorSource.getValues(0, 2)).andReturn(createIntegerValues(100, 101)).atLeastOnce();

    VariableResource sut = new VariableResource(null, null) {

      @Override
      PagingVectorSource getPagingVectorSource() {
        return mockPagingVectorSource;
      }
    };

    replay(mockPagingVectorSource);

    // Exercise
    Iterable<ValueDto> valueDtos = sut.getValues(0, 2);

    // Verify behaviour
    verify(mockPagingVectorSource);

    // Verify state
    assertNotNull(valueDtos);
    ImmutableList<ValueDto> valueDtoList = ImmutableList.copyOf(valueDtos);
    assertEquals(2, valueDtoList.size());
    assertEquals("100", valueDtoList.get(0).getValue());
    assertEquals("101", valueDtoList.get(1).getValue());
  }

  @Test(expected = InvalidRequestException.class)
  public void testGetValues_ThrowsInvalidRequestExceptionIfLimitIsNegative() {
    // Setup
    VariableResource sut = new VariableResource(null, null);

    // Exercise
    sut.getValues(0, -1);
  }

  //
  // Helper Methods
  //

  private List<Value> createIntegerValues(Integer... intValues) {
    List<Value> values = new ArrayList<Value>();

    for(Integer i : intValues) {
      values.add(IntegerType.get().valueOf(i));
    }

    return values;
  }
}
