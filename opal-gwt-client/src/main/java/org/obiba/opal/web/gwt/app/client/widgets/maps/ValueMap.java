/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.maps;

import java.util.Map;

import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.control.SelectFeature;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureSelectedListener;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureUnselectedListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.Geometry;
import org.gwtopenmaps.openlayers.client.geometry.LineString;
import org.gwtopenmaps.openlayers.client.geometry.LinearRing;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.geometry.Polygon;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.popup.FramedCloud;
import org.gwtopenmaps.openlayers.client.popup.Popup;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.collect.Maps;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONParser;

public abstract class ValueMap extends BaseMap {

  protected final VariableDto variable;

  protected final ValueSetsDto.ValueDto value;

  // index of the value in the value sequence
  protected final Integer index;

  private final Map<String, String> featurePopupContent = Maps.newHashMap();

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
  protected void addPointFeature(Vector vectorLayer, ValueSetsDto.ValueDto value, String id) {
    Point p = parsePoint(value);
    addGeometryFeature(vectorLayer, p, id);
  }

  protected void addLineStringFeature(Vector vectorLayer, ValueSetsDto.ValueDto value, String id) {
    LineString p = parseLineString(value);
    addGeometryFeature(vectorLayer, p, id);
  }

  protected void addPolygonFeature(Vector vectorLayer, ValueSetsDto.ValueDto value, String id) {
    Polygon p = parsePolygon(value);
    addGeometryFeature(vectorLayer, p, id);
  }

  private void addGeometryFeature(Vector vectorLayer, Geometry g, String id) {
    Style style = getDefaultPointStyle();
    style.setLabel(id);

    VectorFeature pointFeature = new VectorFeature(g, style);
    pointFeature.setFeatureId(id);
    vectorLayer.addFeature(pointFeature);

    addFeaturePopup(vectorLayer, pointFeature, value.getValue());
  }

  /**
   * Add a popup to a feature.
   *
   * @param vectorLayer
   * @param pointFeature
   * @param content html content
   */
  private void addFeaturePopup(Vector vectorLayer, VectorFeature pointFeature, String content) {
    if(featurePopupContent.size() == 0) {
      // We want to display the popup when the user clicks the feature.
      // So we add a VectorFeatureSelectedListener to the feature.

      // First create a select control and make sure it is actived
      SelectFeature selectFeature = new SelectFeature(vectorLayer);
      selectFeature.setAutoActivate(true);
      map.addControl(selectFeature);

      // Secondly add a VectorFeatureSelectedListener to the feature
      vectorLayer.addVectorFeatureSelectedListener(new VectorFeatureSelectedListener() {
        public void onFeatureSelected(FeatureSelectedEvent eventObject) {
          VectorFeature pointFeature = eventObject.getVectorFeature();
          String content = featurePopupContent.get(pointFeature.getFeatureId());
          //Attach a popup to the point, we use null as size cause we set autoSize to true
          Popup popup = new FramedCloud("id1", pointFeature.getCenterLonLat(), null, content, null, false);
          popup.setPanMapIfOutOfView(true); //this set the popup in a strategic way, and pans the map if needed.
          popup.setAutoSize(true);
          pointFeature.setPopup(popup);

          //And attach the popup to the map
          map.addPopup(eventObject.getVectorFeature().getPopup());
        }
      });

      // And add a VectorFeatureUnselectedListener which removes the popup.
      vectorLayer.addVectorFeatureUnselectedListener(new VectorFeatureUnselectedListener() {
        public void onFeatureUnselected(FeatureUnselectedEvent eventObject) {
          VectorFeature pointFeature = eventObject.getVectorFeature();
          map.removePopup(pointFeature.getPopup());
          pointFeature.resetPopup();
        }
      });
    }

    featurePopupContent.put(pointFeature.getFeatureId(), content);
  }

  protected Style getDefaultPointStyle() {
    // Create a style that we will use for the point
    Style st = new Style();
    st.setGraphicSize(25, 41);
    st.setExternalGraphic("img/marker-icon.png");
    st.setFillOpacity(1.0);
    st.setLabelXOffset(10);
    st.setLabelYOffset(10);
    st.setLabelAlign("lb");
    st.setFontColor("#0000FF");
    return st;
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
      JSONArray shape = (JSONArray) array.get(i);
      rings[i] = new LinearRing(parsePoints(shape));
    }
    return new Polygon(rings);
  }

  protected Point[] parsePoints(JSONArray array) {
    Point[] rval = new Point[array.size()];
    for(int i = 0; i < array.size(); i++) {
      JSONArray point = (JSONArray) array.get(i);
      double x = ((JSONNumber) point.get(0)).doubleValue();
      double y = ((JSONNumber) point.get(1)).doubleValue();
      rval[i] = new Point(x, y);
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
