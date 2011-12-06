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

import org.obiba.opal.web.gwt.app.client.navigator.view.VariableViewHelper;
import org.obiba.opal.web.model.client.magma.CategoryDto;

import com.google.common.base.Strings;
import com.google.common.collect.Range;

public class ValueMapEntry {

  public enum ValueMapEntryType {
    CATEGORY_NAME, DISTINCT_VALUE, RANGE, EMPTY_VALUES, OTHER_VALUES
  }

  private ValueMapEntryType type;

  private String value;

  private String label;

  private String newValue;

  private boolean missing;

  private double count;

  private ValueMapEntry(ValueMapEntryType type, String value, String label, String newValue, boolean missing, double count) {
    super();
    this.type = type;
    this.value = value;
    this.label = label;
    this.newValue = newValue;
    this.missing = missing;
    this.count = count;
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

  public String getLabel() {
    return label;
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

  public double getCount() {
    return count;
  }

  public static Builder fromCategory(CategoryDto cat) {
    return fromCategory(cat, 0);
  }

  public static Builder fromCategory(CategoryDto cat, double count) {
    String label = VariableViewHelper.getLabelValue(cat.getAttributesArray());
    if(Strings.isNullOrEmpty(label)) {
      label = buildLabel(cat.getName());
    }
    return new Builder(ValueMapEntryType.CATEGORY_NAME).value(cat.getName()).label(label).count(count);
  }

  public static Builder fromDistinct(String value) {
    return fromDistinct(value, 0);
  }

  public static Builder fromDistinct(String value, double count) {
    return new Builder(ValueMapEntryType.DISTINCT_VALUE).value(value).label(buildLabel(value)).count(count);
  }

  public static Builder fromDistinct(Number value) {
    return fromRange(value, value);
  }

  public static Builder fromRange(Range<? extends Number> range) {
    return fromRange(range.hasLowerBound() ? range.lowerEndpoint() : null, range.hasUpperBound() ? range.upperEndpoint() : null).label(range.toString());
  }

  public static Builder fromRange(Number lower, Number upper) {
    String value = "";
    ValueMapEntryType type = ValueMapEntryType.RANGE;

    if(lower == null) {
      value = "-" + formatNumber(upper);
    } else if(upper == null) {
      value = formatNumber(lower) + "+";
    } else if(lower.equals(upper)) {
      value = formatNumber(lower);
      type = ValueMapEntryType.DISTINCT_VALUE;
    } else {
      value = formatNumber(lower) + "-" + formatNumber(upper);
    }
    return new Builder(type).value(value).label(value);
  }

  private static String formatNumber(Number nb) {
    if(nb == null) return null;
    String str = nb.toString();
    // TODO use NumberFormat
    return str.endsWith(".0") ? str.substring(0, str.length() - 2) : str;
  }

  public static Builder createEmpties(String label) {
    return new Builder(ValueMapEntryType.EMPTY_VALUES).value("null").label(label).missing();
  }

  public static Builder createOthers(String label) {
    return new Builder(ValueMapEntryType.OTHER_VALUES).value("*").label(label);
  }

  public static Builder create(ValueMapEntryType type) {
    return new Builder(type);
  }

  private static String buildLabel(String text) {
    if(Strings.isNullOrEmpty(text)) return "";

    String label = text.toLowerCase().replace('_', ' ');
    // Upper case for first letter of words
    StringBuilder b = new StringBuilder(label);
    int i = 0;
    do {
      b.replace(i, i + 1, b.substring(i, i + 1).toUpperCase());
      i = b.indexOf(" ", i) + 1;
    } while(i > 0 && i < b.length());

    return b.toString();
  }

  public static class Builder {
    private ValueMapEntry entry;

    private Builder(ValueMapEntryType type) {
      entry = new ValueMapEntry(type, "", "", "", false, 0);
    }

    public Builder value(String value) {
      entry.value = value;
      return this;
    }

    public Builder label(String label) {
      entry.label = label;
      return this;
    }

    public Builder newValue(String newValue) {
      entry.newValue = newValue;
      return this;
    }

    public Builder missing() {
      entry.missing = true;
      return this;
    }

    public Builder count(double count) {
      entry.count = count;
      return this;
    }

    public ValueMapEntry build() {
      return entry;
    }
  }
}