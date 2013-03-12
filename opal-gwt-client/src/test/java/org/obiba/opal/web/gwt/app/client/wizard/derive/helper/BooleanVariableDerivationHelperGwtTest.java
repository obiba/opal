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
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.BooleanVariableDerivationHelper.DerivedBooleanVariableGenerator;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.junit.client.GWTTestCase;

/**
 *
 */
public class BooleanVariableDerivationHelperGwtTest extends GWTTestCase {

  public void testDerivedVariableGenerator() {

    VariableDto variable = VariableDto.create();
    variable.setName("boolean-variable");

    List<ValueMapEntry> entries = new ArrayList<ValueMapEntry>();
    entries.add(ValueMapEntry.fromDistinct(Boolean.FALSE.toString()).newValue("0").build());
    entries.add(ValueMapEntry.fromDistinct(Boolean.TRUE.toString()).newValue("1").build());
    entries.add(ValueMapEntry.createEmpties("Empty value").newValue("888").build());

    DerivedBooleanVariableGenerator generator = new DerivedBooleanVariableGenerator(variable, entries);
    VariableDto derived = generator.generate(null);

    assertEquals("$('boolean-variable').map({\n" //
        + "    'true': '1',\n" //
        + "    'false': '0'\n" //
        + "  },\n" //
        + "  null,\n" //
        + "  '888');", VariableDtos.getScript(derived));

  }

  @Override
  public String getModuleName() {
    return "org.obiba.opal.web.gwt.app.GwtApp";
  }

}
