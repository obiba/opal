/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.ValueMapEntryType;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.collect.Range;

/**
 *
 */
public class DerivedNumericalVariableGenerator<N extends Number & Comparable<N>> extends DerivedVariableGenerator {

  private final Map<ValueMapEntry, Range<N>> entryRangeMap;

  public DerivedNumericalVariableGenerator(VariableDto originalVariable, List<ValueMapEntry> valueMapEntries,
      Map<ValueMapEntry, Range<N>> entryRangeMap) {
    super(originalVariable, valueMapEntries);
    this.entryRangeMap = entryRangeMap;
  }

  @Override
  protected void generateScript() {
    scriptBuilder.append("$('").append(originalVariable.getName()).append("')");
    appendGroupMethod();
    appendMapMethod();
  }

  private void appendGroupMethod() {
    // group method
    // ImmutableSortedSet.Builder<Range<N>> ranges = ImmutableSortedSet.naturalOrder();
    List<Range<N>> ranges = new ArrayList<Range<N>>();
    List<ValueMapEntry> outliers = new ArrayList<ValueMapEntry>();
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.getType() == ValueMapEntryType.CATEGORY_NAME || entry.getType() == ValueMapEntryType.DISTINCT_VALUE) {
        outliers.add(entry);
      } else if(entry.getType() == ValueMapEntryType.RANGE) {
        ranges.add(entryRangeMap.get(entry));
      }
    }

    if(ranges.isEmpty()) return;

    scriptBuilder.append(".group(");

    appendBounds(ranges);
    appendOutliers(outliers);

    scriptBuilder.append(")");
  }

  private void appendBounds(List<Range<N>> ranges) {
    scriptBuilder.append("[");

    boolean first = true;
    Range<N> previousRange = null;
    N bound = null;
    for(Range<N> range : ranges) {
      if(previousRange != null && !previousRange.isConnected(range)) {
        appendBound(previousRange.upperEndpoint(), first);
        first = false;
      }

      if(range.hasLowerBound()) {
        bound = range.lowerEndpoint();
        appendBound(bound, first);
        first = false;
      }

      previousRange = range;
    }
    // close the last range
    if(previousRange != null && previousRange.hasUpperBound()) {
      appendBound(previousRange.upperEndpoint(), false);
    }
    scriptBuilder.append("]");
  }

  private void appendBound(N bound, boolean first) {
    if(!first) {
      scriptBuilder.append(", ");
    }
    scriptBuilder.append(bound);
  }

  private void appendOutliers(List<ValueMapEntry> outliers) {
    if(outliers.isEmpty()) return;
    scriptBuilder.append(", [");
    boolean first = true;
    for(ValueMapEntry entry : outliers) {
      if(first) {
        first = false;
      } else {
        scriptBuilder.append(", ");
      }
      scriptBuilder.append(entry.getValue());
    }
    scriptBuilder.append("]");
  }

  private void appendMapMethod() {
    scriptBuilder.append(".map({");
    boolean first = true;
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.getType() == ValueMapEntryType.CATEGORY_NAME || entry.getType() == ValueMapEntryType.DISTINCT_VALUE ||
          entry.getType() == ValueMapEntryType.RANGE) {
        if(first) {
          first = false;
        } else {
          scriptBuilder.append(", ");
        }
        scriptBuilder.append("\n    '").append(entry.getValue()).append("': ");
        appendNewValue(entry);
        addNewCategory(entry);
      }
    }
    scriptBuilder.append("\n  }");
    appendSpecialValuesEntry(getOtherValuesMapEntry());
    appendSpecialValuesEntry(getEmptyValuesMapEntry());
    scriptBuilder.append(")");
  }

}
