/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.view;

public class ValueMapEntry {
  private String value;

  private String newValue;

  private boolean missing;

  public ValueMapEntry(String value) {
    this(value, "", false);
  }

  public ValueMapEntry(String value, String newValue) {
    this(value, newValue, false);
  }

  public ValueMapEntry(String value, String newValue, boolean missing) {
    super();
    this.value = value;
    this.newValue = newValue;
    this.missing = missing;
  }

  public String getValue() {
    return value;
  }

  public String getNewValue() {
    return newValue;
  }

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  public boolean isMissing() {
    return missing;
  }

  public void setMissing(boolean missing) {
    this.missing = missing;
  }
}