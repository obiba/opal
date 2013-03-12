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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.collect.Range;
import com.google.gwt.junit.client.GWTTestCase;

/**
 *
 */
public class NumericalVariableDerivationHelperGwtTest extends GWTTestCase {

  public void testDerivedVariableGenerator_double() {

    // $('RES_FIRST_HEIGHT').group([150.0, 160.0, 170.0, 180.0, 190.0], [888]).map({
    // '-150': '1',
    // '150-160': '2',
    // '160-170': '3',
    // '190+': '6',
    // '888': '8'
    // },
    // null,
    // null)

    List<ValueMapEntry> entries = new ArrayList<ValueMapEntry>();
    Map<ValueMapEntry, Range<Double>> entryRangeMap = new HashMap<ValueMapEntry, Range<Double>>();

    VariableDto variable = VariableDto.create();
    variable.setName("numerical-variable");

    Range<Double> range_150 = NumericalVariableDerivationHelper.buildRange(null, 150d);
    ValueMapEntry entry_150 = ValueMapEntry.fromRange(range_150).newValue("1").build();
    entries.add(entry_150);
    entryRangeMap.put(entry_150, range_150);

    Range<Double> range_150_160 = NumericalVariableDerivationHelper.buildRange(150d, 160d);
    ValueMapEntry entry_150_160 = ValueMapEntry.fromRange(range_150_160).newValue("2").build();
    entries.add(entry_150_160);
    entryRangeMap.put(entry_150_160, range_150_160);

    Range<Double> range_160_170 = NumericalVariableDerivationHelper.buildRange(160d, 170d);
    ValueMapEntry entry_160_170 = ValueMapEntry.fromRange(range_160_170).newValue("3").build();
    entries.add(entry_160_170);
    entryRangeMap.put(entry_160_170, range_160_170);

    Range<Double> range_180 = NumericalVariableDerivationHelper.buildRange(180d, null);
    ValueMapEntry entry_180 = ValueMapEntry.fromRange(range_180).newValue("4").build();
    entries.add(entry_180);
    entryRangeMap.put(entry_180, range_180);

    entries.add(ValueMapEntry.fromDistinct(888d).newValue("5").build());

    DerivedNumericalVariableGenerator<Double> generator = new DerivedNumericalVariableGenerator<Double>(variable,
        entries, entryRangeMap);
    VariableDto derived = generator.generate(null);

    assertEquals("$('numerical-variable').group([150.0, 160.0, 170.0, 180.0], [888]).map({\n" //
        + "    '-150': '1', \n" //
        + "    '150-160': '2', \n" //
        + "    '160-170': '3', \n" //
        + "    '180+': '4', \n" //
        + "    '888': '5'\n" //
        + "  },\n" //
        + "  null,\n" //
        + "  null)", VariableDtos.getScript(derived));
  }

  @Override
  public String getModuleName() {
    return "org.obiba.opal.web.gwt.app.GwtApp";
  }
}
