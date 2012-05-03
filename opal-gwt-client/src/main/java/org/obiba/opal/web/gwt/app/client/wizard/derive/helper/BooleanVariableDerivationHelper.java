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

import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.model.client.magma.VariableDto;

/**
 *
 */
public class BooleanVariableDerivationHelper extends DerivationHelper {

  public BooleanVariableDerivationHelper(VariableDto originalVariable, VariableDto destination) {
    super(originalVariable, destination);
    initializeValueMapEntries();
  }

  @Override
  protected void initializeValueMapEntries() {
    valueMapEntries = new ArrayList<ValueMapEntry>();
    valueMapEntries.add(ValueMapEntry.fromDistinct(Boolean.TRUE.toString()).label(translations.trueLabel()).newValue("1").build());
    valueMapEntries.add(ValueMapEntry.fromDistinct(Boolean.FALSE.toString()).label(translations.falseLabel()).newValue("0").build());
    valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).build());
  }

  @Override
  protected DerivedVariableGenerator getDerivedVariableGenerator() {
    return new DerivedBooleanVariableGenerator(originalVariable, valueMapEntries);
  }

  public static class DerivedBooleanVariableGenerator extends DerivedVariableGenerator {

    public DerivedBooleanVariableGenerator(VariableDto originalVariable, List<ValueMapEntry> valueMapEntries) {
      super(originalVariable, valueMapEntries);
    }

    @Override
    protected void generateScript() {
      scriptBuilder.append("$('").append(originalVariable.getName()).append("').map({");
      appendBooleanValueMapEntries();
      scriptBuilder.append("  }");
      appendSpecialValuesEntry(getOtherValuesMapEntry());
      appendSpecialValuesEntry(getEmptyValuesMapEntry());
      scriptBuilder.append(");");
    }

    private void appendBooleanValueMapEntries() {
      ValueMapEntry trueEntry = appendValueMapEntry(Boolean.TRUE.toString());
      scriptBuilder.append(",");
      addNewCategory(trueEntry);

      ValueMapEntry falseEntry = appendValueMapEntry(Boolean.FALSE.toString());
      scriptBuilder.append("\n");
      addNewCategory(falseEntry);
    }
  }

}
