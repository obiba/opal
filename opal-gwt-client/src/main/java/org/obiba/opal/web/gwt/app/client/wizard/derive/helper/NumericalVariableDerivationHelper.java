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

public class NumericalVariableDerivationHelper extends DerivationHelper {

  public NumericalVariableDerivationHelper(VariableDto originalVariable) {
    super(originalVariable);
    initializeValueMapEntries();
  }

  @Override
  protected void initializeValueMapEntries() {
    this.valueMapEntries = new ArrayList<ValueMapEntry>();
    valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).build());
    valueMapEntries.add(ValueMapEntry.createOthers(translations.otherValuesLabel()).build());
  }

  @Override
  public VariableDto getDerivedVariable() {
    VariableDto derived = copyVariable(originalVariable);
    derived.setValueType("text");

    StringBuilder scriptBuilder = new StringBuilder("$('" + originalVariable.getName() + "')");

    setScript(derived, scriptBuilder.toString());
    return derived;
  }

  public enum GroupMethod {
    RANGE, DISCRETE;
  }
}