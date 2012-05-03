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

import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.OpenTextualVariableDerivationHelper.DerivedOpenTextualVariableGenerator;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.junit.client.GWTTestCase;

/**
 *
 */
public class OpenTextualVariableDerivationHelperGwtTest extends GWTTestCase {

  public void testDerivedVariableGenerator() {

    VariableDto variable = VariableDto.create();
    variable.setName("open-textual-variable");

    List<ValueMapEntry> entries = new ArrayList<ValueMapEntry>();
    entries.add(ValueMapEntry.fromDistinct("QC").newValue("1").build());
    entries.add(ValueMapEntry.fromDistinct("ON").newValue("2").build());
    entries.add(ValueMapEntry.fromDistinct("BC").newValue("3").build());
    entries.add(ValueMapEntry.fromDistinct("NB").newValue("4").build());
    entries.add(ValueMapEntry.createEmpties("Empty value").newValue("888").build());
    entries.add(ValueMapEntry.createOthers("Other").newValue("999").build());

    DerivedOpenTextualVariableGenerator generator = new DerivedOpenTextualVariableGenerator(variable, entries);
    VariableDto derived = generator.generate(null);

    assertEquals("$('open-textual-variable').map({\n" //
        + "    'QC': '1',\n" //
        + "    'ON': '2',\n" //
        + "    'BC': '3',\n" //
        + "    'NB': '4'\n" //
        + "  },\n" //
        + "  '999',\n" //
        + "  '888');", VariableDtos.getScript(derived));

  }

  @Override
  public String getModuleName() {
    return "org.obiba.opal.web.gwt.app.GwtApp";
  }
}
