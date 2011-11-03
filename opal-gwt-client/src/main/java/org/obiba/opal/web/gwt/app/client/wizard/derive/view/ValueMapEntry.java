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
  public enum ValueMapEntryType {
    CATEGORY_NAME, DISTINCT_VALUE, EMPTY_VALUES, OTHER_VALUES
  }

  private ValueMapEntryType type;

  private String value;

  private String newValue;

  private boolean missing;

  public ValueMapEntry(String value) {
    this(ValueMapEntryType.DISTINCT_VALUE, value, "", false);
  }

  public ValueMapEntry(ValueMapEntryType type, String value) {
    this(type, value, "", false);
  }

  public ValueMapEntry(String value, String newValue) {
    this(ValueMapEntryType.DISTINCT_VALUE, value, newValue, false);
  }

  public ValueMapEntry(String value, String newValue, boolean missing) {
    this(ValueMapEntryType.DISTINCT_VALUE, value, newValue, missing);
  }

  public ValueMapEntry(ValueMapEntryType type, String value, String newValue, boolean missing) {
    super();
    this.type = type;
    this.value = value;
    this.newValue = newValue;
    this.missing = missing;
  }

  public boolean isType(ValueMapEntryType... types) {
    for(ValueMapEntryType t : types) {
      if(type.equals(t)) return true;
    }
    return false;
  }

  public ValueMapEntryType getType() {
    return type;
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