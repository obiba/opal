package org.obiba.opal.web.gwt.app.client.widgets.maps;

import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.control.SelectFeature;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureSelectedListener;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureUnselectedListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.popup.FramedCloud;
import org.gwtopenmaps.openlayers.client.popup.Popup;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONParser;

/**
 * Map showing a point type value.
 */
public class PointValueMap extends ValueMap {

  public PointValueMap(ValueSetsDto.ValueDto value) {
    super(value);
  }

  @Override
  public void initializeValue() {
    // Center and zoom
    double[] coordinates = parseValue(value);
    center(coordinates[0], coordinates[1], DEFAULT_ZOOM);

    // Add a point feature
    Vector vectorLayer = new Vector("Vector Layer");
    map.addLayer(vectorLayer);
    Point p = new Point(coordinates[0], coordinates[1]);
    p.transform(DEFAULT_PROJECTION, new Projection(map.getProjection()));

    // Create a style that we will use for the point
    Style st = new Style();
    st.setGraphicSize(25, 41);
    st.setExternalGraphic("img/marker-icon.png");
    st.setFillOpacity(1.0);

    // Create the vectorfeature
    final VectorFeature pointFeature = new VectorFeature(p, st);
    pointFeature.setFeatureId("point");
    LonLat pLonLat = new LonLat(coordinates[0], coordinates[1]);
    pLonLat.transform(DEFAULT_PROJECTION.getProjectionCode(), map.getProjection());
    vectorLayer.addFeature(pointFeature);

    // We want to display the popup when the user clicks the feature.
    // So we add a VectorFeatureSelectedListener to the feature.

    // First create a select control and make sure it is actived
    SelectFeature selectFeature = new SelectFeature(vectorLayer);
    selectFeature.setAutoActivate(true);
    map.addControl(selectFeature);

    // Secondly add a VectorFeatureSelectedListener to the feature
    vectorLayer.addVectorFeatureSelectedListener(new VectorFeatureSelectedListener() {
      public void onFeatureSelected(FeatureSelectedEvent eventObject) {
        //Attach a popup to the point, we use null as size cause we set autoSize to true
        Popup popup = new FramedCloud("id1", pointFeature.getCenterLonLat(), null,
            value.getValue(), null, false);
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
        map.removePopup(eventObject.getVectorFeature().getPopup());
        pointFeature.resetPopup();
      }
    });
  }

  @Override
  public void initializeValueSequence() {

  }

  private double[] parseValue(ValueSetsDto.ValueDto value) {
    JSONArray array = (JSONArray) JSONParser.parseStrict(value.getValue());
    double[] coordinates = new double[2];
    coordinates[0] = ((JSONNumber) array.get(0)).doubleValue();
    coordinates[1] = ((JSONNumber) array.get(1)).doubleValue();
    return coordinates;
  }

}
