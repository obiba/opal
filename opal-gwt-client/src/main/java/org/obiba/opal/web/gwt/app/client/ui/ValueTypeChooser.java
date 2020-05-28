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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.gwt.core.client.GWT;

public class ValueTypeChooser extends Chooser {

  private static final String[] textualTypes = new String[] { "text" };

  private static final String[] numericalTypes = new String[] { "integer", "decimal" };

  private static final String[] temporalTypes = new String[] { "date", "datetime" };

  private static final String[] geoTypes = new String[] { "point", "linestring", "polygon" };

  private static final String[] otherTypes = new String[] { "binary", "boolean", "locale" };

  private static final Translations translations = GWT.create(Translations.class);

  public ValueTypeChooser() {
    addGroupItems("TEXTUAL", textualTypes);
    addGroupItems("NUMERICAL", numericalTypes);
    addGroupItems("TEMPORAL", temporalTypes);
    addGroupItems("GEOSPATIAL", geoTypes);
    addGroupItems("OTHER", otherTypes);
  }

  private void addGroupItems(String label, String[] items) {
    addGroup(translations.valueTypeMap().get(label));
    for(String item : items) {
      addItemToGroup(translations.valueTypeMap().get(item + ".type"), item);
    }
  }
}
