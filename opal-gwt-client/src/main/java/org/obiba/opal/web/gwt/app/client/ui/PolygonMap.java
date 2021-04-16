/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui;

import java.util.ArrayList;
import java.util.List;

import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.Geometry;
import org.gwtopenmaps.openlayers.client.geometry.LinearRing;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.geometry.Polygon;
import org.gwtopenmaps.openlayers.client.layer.Vector;

public class PolygonMap extends BaseMap {

  private static final int INT = 1000000;

  private List<Point> points = new ArrayList<>();

  private double approxArea;

  public PolygonMap() {
    super("100%", "400px");
    initialize();
  }

  public PolygonMap(String width, String height) {
    super(width, height);
    initialize();
  }

  private void initialize() {
    initWidget(contentPanel);

  }

  public void addPoint(double lon, double lat) {
    Point p = new Point(lon, lat);
    p.transform(DEFAULT_PROJECTION, new Projection(map.getProjection()));
    points.add(p);
  }

  public Polygon drawPolygon() {
    Vector v = addVectorLayer("");
    Polygon p = addPolygonFeature(v, points.toArray(new Point[points.size()]));
    center(p);

    approxArea = p.getGeodesicArea(new Projection("EPSG:3857")) / 1000000;
    return p;
  }

  public double getApproxArea() {
    return approxArea;
  }

  protected Vector addVectorLayer(String name) {
    Vector vectorLayer = new Vector(name);
    map.addLayer(vectorLayer);
    return vectorLayer;
  }

  protected Polygon addPolygonFeature(Vector vectorLayer, Point[] points) {
    Polygon p = parsePolygon(points);
    addGeometryFeature(vectorLayer, p, "", getPolygonStyle());
    return p;
  }

  private void addGeometryFeature(Vector vectorLayer, Geometry g, String id, Style style) {
    style.setLabel(id);
    VectorFeature feature = new VectorFeature(g, style);

    vectorLayer.addFeature(feature);
  }

  protected Style getDefaultStyle() {
    // Create a style that we will use for the point
    Style style = new Style();
    style.setFontColor("#3c3c3c");
    style.setFontWeight("bold");
    style.setLabelXOffset(10);
    style.setLabelYOffset(10);
    style.setLabelAlign("lb");
    return style;
  }

  protected Style getPolygonStyle() {
    Style style = getDefaultStyle();
    //style.setFillColor("red");
    //style.setStrokeColor("green");
    style.setFillOpacity(0.4);
    return style;
  }

  protected Polygon parsePolygon(Point[] points) {
    LinearRing[] rings = new LinearRing[1];
    rings[0] = new LinearRing(points);
    return new Polygon(rings);
  }

}
