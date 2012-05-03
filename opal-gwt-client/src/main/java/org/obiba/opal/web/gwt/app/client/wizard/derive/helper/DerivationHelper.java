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

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.ValueMapEntryType;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;

/**
 *
 */
public abstract class DerivationHelper {

  protected static final String NA = "N/A";

  protected Translations translations = GWT.create(Translations.class);

  protected List<ValueMapEntry> valueMapEntries;

  protected VariableDto originalVariable;
  private final VariableDto destination;

  public DerivationHelper(VariableDto originalVariable, VariableDto destination) {
    this.originalVariable = originalVariable;
    this.destination = destination;
  }

  protected abstract void initializeValueMapEntries();

  protected abstract DerivedVariableGenerator getDerivedVariableGenerator();

  public VariableDto getDerivedVariable() {
    return getDerivedVariableGenerator().generate(destination);
  }

  public List<ValueMapEntry> getValueMapEntries() {
    return valueMapEntries;
  }

  public boolean addEntry(ValueMapEntry entryArg) {
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.getValue().equals(entryArg.getValue())) {
        return false;
      }
    }
    for(int i = valueMapEntries.size() - 1; i >= 0; i--) {
      if(!valueMapEntries.get(i).getType().equals(ValueMapEntryType.EMPTY_VALUES)
          && !valueMapEntries.get(i).getType().equals(ValueMapEntryType.OTHER_VALUES)) {
        valueMapEntries.add(i + 1, entryArg);
        break;
      }
    }
    if(!valueMapEntries.contains(entryArg)) {
      valueMapEntries.add(0, entryArg);
    }

    return true;
  }

  public boolean hasValueMapEntryWithValue(String value) {
    return getValueMapEntryWithValue(value) != null;
  }

  public ValueMapEntry getValueMapEntryWithValue(String value) {
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.getValue().equals(value)) {
        return entry;
      }
    }
    return null;
  }

  public ValueMapEntry getEmptiesValueMapEntry() {
    return getValueMapEntryWithValue(ValueMapEntry.EMPTY_VALUES_VALUE);
  }

  public ValueMapEntry getOthersValueMapEntry() {
    return getValueMapEntryWithValue(ValueMapEntry.OTHER_VALUES_VALUE);
  }

  public VariableDto getOriginalVariable() {
    return originalVariable;
  }

}
