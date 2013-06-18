package org.obiba.opal.web.gwt.app.client.widgets.maps;

import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.control.LayerSwitcher;
import org.gwtopenmaps.openlayers.client.control.OverviewMap;
import org.gwtopenmaps.openlayers.client.control.ScaleLine;
import org.gwtopenmaps.openlayers.client.layer.OSM;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;

public class BaseMap extends Composite {

  protected static final Projection DEFAULT_PROJECTION = new Projection("EPSG:4326");

  protected  static final int DEFAULT_ZOOM = 10;

  protected Panel contentPanel = new SimplePanel();

  protected Map map;

  protected BaseMap() {
    this("500px", "500px");
  }

  protected BaseMap(String width, String height) {
    initialize(width, height);
  }

  private void initialize(String width, String height) {
    //create some MapOptions
    MapOptions defaultMapOptions = new MapOptions();
    defaultMapOptions.setNumZoomLevels(16);

    //Create a MapWidget and add an OSM layers
    MapWidget mapWidget = new MapWidget(width, height, defaultMapOptions);
    OSM osm = OSM.Mapnik("Mapnik");
    osm.setIsBaseLayer(true);
    map = mapWidget.getMap();
    map.addLayer(osm);

    //Lets add some default controls to the map
    //map.addControl(new LayerSwitcher()); //+ sign in the upperright corner to display the layer switcher
    //map.addControl(new OverviewMap()); //+ sign in the lowerright to display the overviewmap
    //map.addControl(new ScaleLine()); //Display the scaleline

    contentPanel.add(mapWidget);
  }

  protected void center(double lon, double lat, int zoom) {
    //Center and zoom to a location
    LonLat lonLat = new LonLat(lon, lat);
    lonLat.transform(DEFAULT_PROJECTION.getProjectionCode(), map.getProjection());
    map.setCenter(lonLat, zoom);
  }

}
