/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui;

import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.Geometry;
import org.gwtopenmaps.openlayers.client.geometry.LineString;
import org.gwtopenmaps.openlayers.client.geometry.LinearRing;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.geometry.Polygon;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONParser;

public abstract class ValueMap extends BaseMap {

  protected final VariableDto variable;

  protected final ValueSetsDto.ValueDto value;

  // index of the value in the value sequence
  protected final Integer index;

  public ValueMap(VariableDto variable, ValueSetsDto.ValueDto value, Integer index) {
    this.variable = variable;
    this.value = value;
    this.index = index;
    initialize();
  }

  public ValueMap(String width, String height, VariableDto variable, ValueSetsDto.ValueDto value, Integer index) {
    super(width, height);
    this.variable = variable;
    this.value = value;
    this.index = index;
    initialize();
  }

  private void initialize() {
    if(value != null) {
      if(value.getValuesArray() == null || value.getValuesArray().length() == 0) {
        initializeValue();
      } else {
        initializeValueSequence();
      }
    }
    initWidget(contentPanel);
  }

  /**
   * Display the value on the map.
   */
  protected abstract void initializeValue();

  /**
   * Display the value sequence on the map.
   */
  protected abstract void initializeValueSequence();

  protected Vector addVectorLayer(String name) {
    Vector vectorLayer = new Vector(name);
    map.addLayer(vectorLayer);
    return vectorLayer;
  }

  /**
   * Add a point to a vector layer.
   *
   * @param vectorLayer
   * @param value
   * @param style
   */
  protected Point addPointFeature(Vector vectorLayer, ValueSetsDto.ValueDto value, String id) {
    Point p = parsePoint(value);
    addGeometryFeature(vectorLayer, p, id, getPointStyle());
    return p;
  }

  protected LineString addLineStringFeature(Vector vectorLayer, ValueSetsDto.ValueDto value, String id) {
    LineString p = parseLineString(value);
    addGeometryFeature(vectorLayer, p, id, getLineStringStyle());
    return p;
  }

  protected Polygon addPolygonFeature(Vector vectorLayer, ValueSetsDto.ValueDto value, String id) {
    Polygon p = parsePolygon(value);
    addGeometryFeature(vectorLayer, p, id, getPolygonStyle());
    return p;
  }

  private void addGeometryFeature(Vector vectorLayer, Geometry g, String id, Style style) {
    style.setLabel(id);

    VectorFeature feature = new VectorFeature(g, style);
    feature.setFeatureId(id);
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

  protected Style getPointStyle() {
    Style style = getDefaultStyle();
    style.setGraphicSize(25, 41);
    style.setExternalGraphic("img/marker-icon.png");
    style.setFillOpacity(1.0);
    return style;
  }

  protected Style getLineStringStyle() {
    return getDefaultStyle();
  }

  protected Style getPolygonStyle() {
    Style style = getDefaultStyle();
    //style.setFillColor("red");
    //style.setStrokeColor("green");
    style.setFillOpacity(0.4);
    return style;
  }

  protected Point parsePoint(ValueSetsDto.ValueDto value) {
    JSONArray array = (JSONArray) JSONParser.parseStrict(value.getValue());
    double[] coordinates = new double[2];
    coordinates[0] = ((JSONNumber) array.get(0)).doubleValue();
    coordinates[1] = ((JSONNumber) array.get(1)).doubleValue();

    Point p = new Point(coordinates[0], coordinates[1]);
    p.transform(DEFAULT_PROJECTION, new Projection(map.getProjection()));

    return p;
  }

  protected LineString parseLineString(ValueSetsDto.ValueDto value) {
    JSONArray array = (JSONArray) JSONParser.parseStrict(value.getValue());
    return new LineString(parsePoints(array));
  }

  protected Polygon parsePolygon(ValueSetsDto.ValueDto value) {
    JSONArray array = (JSONArray) JSONParser.parseStrict(value.getValue());
    LinearRing[] rings = new LinearRing[array.size()];
    for(int i = 0; i < array.size(); i++) {
      //GWT.log("shape["+ i + "]");
      JSONArray shape = (JSONArray) array.get(i);
      rings[i] = new LinearRing(parsePoints(shape));
    }
    return new Polygon(rings);
  }

  protected Point[] parsePoints(JSONArray array) {
    Point[] rval = new Point[array.size()];
    for(int i = 0; i < array.size(); i++) {
      //GWT.log("  point["+ i + "]");
      JSONArray point = (JSONArray) array.get(i);
      double x = ((JSONNumber) point.get(0)).doubleValue();
      double y = ((JSONNumber) point.get(1)).doubleValue();
      //GWT.log("    ["+ x + ", " + y + "]");
      Point p = new Point(x, y);
      p.transform(DEFAULT_PROJECTION, new Projection(map.getProjection()));
      rval[i] = p;
    }
    return rval;
  }

  protected double[] parseCoordinates(ValueSetsDto.ValueDto value) {
    JSONArray array = (JSONArray) JSONParser.parseStrict(value.getValue());
    double[] coordinates = new double[2];
    coordinates[0] = ((JSONNumber) array.get(0)).doubleValue();
    coordinates[1] = ((JSONNumber) array.get(1)).doubleValue();
    return coordinates;
  }
}
