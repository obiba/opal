/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import java.util.List;

import org.junit.Test;
import org.obiba.magma.Coordinate;
import org.obiba.magma.math.summary.support.DefaultGeoSummary;
import org.obiba.opal.web.model.Math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class GeoSummaryDtoTest {

  @Test
  public void test_bounding_box_centroid_and_cells() {
    DefaultGeoSummary summary = new DefaultGeoSummary();
    // three points in the same cell in Montreal, one in Quebec city
    summary.addCoordinates(List.of(new Coordinate(-73.5673, 45.5017), new Coordinate(-73.5674, 45.5018),
        new Coordinate(-73.5675, 45.5016), new Coordinate(-71.2080, 46.8139)));

    Math.GeoSummaryDto dto = Dtos.asDto(summary, true, 1).build();
    assertEquals(45.5016, dto.getMin().getLat(), 1e-9);
    assertEquals(-73.5675, dto.getMin().getLon(), 1e-9);
    assertEquals(46.8139, dto.getMax().getLat(), 1e-9);
    assertEquals(-71.2080, dto.getMax().getLon(), 1e-9);
    assertEquals((45.5017 + 45.5018 + 45.5016 + 46.8139) / 4, dto.getCentroid().getLat(), 1e-9);
    assertEquals(2, dto.getCellsCount());
    assertEquals(4, dto.getCellsList().stream().mapToLong(Math.H3CellDto::getCount).sum());
    assertEquals(6, dto.getCells(0).getBoundaryCount());

    // the cell counting a single point is left out
    dto = Dtos.asDto(summary, true, 2).build();
    assertEquals(1, dto.getCellsCount());
    assertEquals(3, dto.getCells(0).getCount());

    // not points: bounding box only
    dto = Dtos.asDto(summary, false, 1).build();
    assertEquals(0, dto.getCellsCount());
    assertFalse(dto.getCentroid().getLat() == 0);
  }

}
