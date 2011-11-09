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

import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.model.client.magma.VariableDto;

/**
 *
 */
public class OpenTextualVariableDerivationHelper extends VariableDuplicationHelper {

  private final Method method;

  public OpenTextualVariableDerivationHelper(VariableDto originalVariable, Method method) {
    super(originalVariable);
    this.method = method;
    initializeValueMapEntries();
  }

  @Override
  protected void initializeValueMapEntries() {
    this.valueMapEntries = new ArrayList<ValueMapEntry>();

    if(method.isAutomatically()) {
      valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).build());
      valueMapEntries.add(ValueMapEntry.createOthers(translations.otherValuesLabel()).build());
    }
  }

  public boolean addEntry(String value, String newValue) {
    if(value != null && !value.trim().equals("") && newValue != null && !newValue.trim().equals("")) {
      valueMapEntries.add(ValueMapEntry.fromDistinct(value).newValue(newValue).build());
      return true;
    }
    return false;
  }

  public enum Method {

    AUTOMATICALLY("Automatically"), MANUAL("Manual");

    private final String method;

    Method(String method) {
      this.method = method;
    }

    public static Method fromString(String text) {
      for(Method g : values()) {
        if(g.method.equalsIgnoreCase(text)) {
          return g;
        }
      }
      return null;
    }

    @Override
    public String toString() {
      return method;
    }

    public boolean isAutomatically() {
      return this == AUTOMATICALLY;
    }
  }

}
