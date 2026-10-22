package org.obiba.opal.web.magma;

import java.util.List;

import org.junit.Test;
import org.obiba.magma.Coordinate;
import org.obiba.magma.math.H3Cell;
import org.obiba.magma.math.summary.support.DefaultGeoSummary;
import org.obiba.opal.web.model.Math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class GeoSummaryDtoTest {

  @Test
  public void test_bounding_box_centroid_and_cells() {
    DefaultGeoSummary summary = new DefaultGeoSummary();
    summary.setMin(new Coordinate(-73.5675, 45.5016));
    summary.setMax(new Coordinate(-71.2080, 46.8139));
    summary.setCentroid(new Coordinate(-72.9, 45.8));
    List<Coordinate> boundary = List.of(new Coordinate(-73.5, 45.5), new Coordinate(-73.6, 45.5), new Coordinate(-73.6, 45.6));
    summary.addCells(List.of(new H3Cell("872b8e8aaffffff", 3, boundary), new H3Cell("872baa36cffffff", 1, boundary)));

    Math.GeoSummaryDto dto = Dtos.asDto(summary, 1, true).build();
    assertEquals(45.5016, dto.getMin().getLat(), 1e-9);
    assertEquals(-73.5675, dto.getMin().getLon(), 1e-9);
    assertEquals(46.8139, dto.getMax().getLat(), 1e-9);
    assertEquals(45.8, dto.getCentroid().getLat(), 1e-9);
    assertEquals(2, dto.getCellsCount());
    assertEquals(3, dto.getCells(0).getBoundaryCount());

    // the cell counting a single point and the exact coordinates are left out
    dto = Dtos.asDto(summary, 2, false).build();
    assertEquals(1, dto.getCellsCount());
    assertEquals(3, dto.getCells(0).getCount());
    assertFalse(dto.hasMin());
    assertFalse(dto.hasMax());
    assertFalse(dto.hasCentroid());
  }

}
