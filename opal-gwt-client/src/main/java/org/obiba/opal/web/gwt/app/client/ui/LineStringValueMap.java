/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui;

import javax.annotation.Nullable;

import org.gwtopenmaps.openlayers.client.geometry.LineString;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

/**
 * Map showing a point type value.
 */
public class LineStringValueMap extends ValueMap {

  public LineStringValueMap(VariableDto variable, ValueSetsDto.ValueDto value, @Nullable Integer index) {
    super(variable, value, index);
  }

  public LineStringValueMap(String width, String height, VariableDto variable, ValueSetsDto.ValueDto value,
      @Nullable Integer index) {
    super(width, height, variable, value, index);
  }

  @Override
  protected void initializeValue() {
    // Add a vector layer
    Vector vectorLayer = addVectorLayer(variable.getName());

    // Add a linestring feature to the vector layer
    center(addLineStringFeature(vectorLayer, value, variable.getName()));
  }

  @Override
  protected void initializeValueSequence() {
    // Add a vector layer
    Vector vectorLayer = addVectorLayer(variable.getName());

    LineString mainLine = null;
    // Add a linestring feature to the vector layer
    for(int i = 0; i < value.getValuesArray().length(); i++) {
      LineString line = addLineStringFeature(vectorLayer, value.getValuesArray().get(i),
          variable.getName() + " [" + (i + 1) + "]");
      if(mainLine == null && (index == null && i == 0) || (index != null && i == index)) {
        mainLine = line;
      }
    }
    center(mainLine);
  }



}
