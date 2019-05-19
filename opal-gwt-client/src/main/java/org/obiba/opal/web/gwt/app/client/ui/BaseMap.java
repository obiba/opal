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

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.geometry.LineString;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.geometry.Polygon;
import org.gwtopenmaps.openlayers.client.layer.GoogleV3;
import org.gwtopenmaps.openlayers.client.layer.GoogleV3MapType;
import org.gwtopenmaps.openlayers.client.layer.GoogleV3Options;
import org.gwtopenmaps.openlayers.client.layer.OSM;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;

public class BaseMap extends Composite {

  protected static final Projection DEFAULT_PROJECTION = new Projection("EPSG:4326");

  protected static final int DEFAULT_ZOOM = 10;

  protected Panel contentPanel = new SimplePanel();

  private MapWidget mapWidget;

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
    mapWidget = new MapWidget(width, height, defaultMapOptions);
    map = mapWidget.getMap();

    // open street map
    addOSMMapnikLayer();
    //addOSMCycleMapLayer();

    // some Google Layers
    //addGoogleV3Layer(GoogleV3MapType.G_HYBRID_MAP, "Google Hybrid");
    //addGoogleV3Layer(GoogleV3MapType.G_NORMAL_MAP, "Google Normal");
    //addGoogleV3Layer(GoogleV3MapType.G_SATELLITE_MAP, "Google Satellite");
    //addGoogleV3Layer(GoogleV3MapType.G_TERRAIN_MAP, "Google Terrain");

    //Lets add some default controls to the map
    //map.addControl(new LayerSwitcher()); //+ sign in the upperright corner to display the layer switcher
    //map.addControl(new OverviewMap()); //+ sign in the lowerright to display the overviewmap
    //map.addControl(new ScaleLine()); //Display the scaleline

    contentPanel.add(mapWidget);
  }

  @Override
  public void setHeight(String height) {
    mapWidget.setHeight(height);
  }

  @Override
  public void setWidth(String width) {
    mapWidget.setWidth(width);
  }

  protected void center(double lon, double lat, int zoom) {
    //Center and zoom to a location
    LonLat lonLat = new LonLat(lon, lat);
    lonLat.transform(DEFAULT_PROJECTION.getProjectionCode(), map.getProjection());
    map.setCenter(lonLat, zoom);
  }

  protected void center(Point point) {
    // Center and zoom
    if(point != null) {
      point.transform(DEFAULT_PROJECTION, new Projection(map.getProjection()));
      center(point.getX(), point.getY(), DEFAULT_ZOOM);
    }
  }

  protected void center(Bounds bounds) {
    // Center and zoom
    if(bounds != null) {
      map.zoomToExtent(
          new Bounds(bounds.getLowerLeftX(), bounds.getLowerLeftY(), bounds.getUpperRightX(), bounds.getUpperRightY()),
          true);
      map.setCenter(bounds.getCenterLonLat());
    }
  }

  protected void center(LineString line) {
    // Center and zoom
    if(line != null) {
      center(line.getBounds());
    }
  }

  protected void center(Polygon polygon) {
    // Center and zoom
    if(polygon != null) {
      center(polygon.getBounds());
    }
  }

  protected void addOSMMapnikLayer() {
    OSM osm = OSM.Mapnik("Mapnik");
    osm.setIsBaseLayer(true);
    map.addLayer(osm);
  }

  protected void addOSMCycleMapLayer() {
    OSM osm = OSM.CycleMap("Cycle Map");
    osm.setIsBaseLayer(true);
    map.addLayer(osm);
  }

  protected void addGoogleV3Layer(GoogleV3MapType type, String label) {
    GoogleV3Options options = new GoogleV3Options();
    options.setIsBaseLayer(true);
    options.setType(type);
    map.addLayer(new GoogleV3(label, options));
  }

}
