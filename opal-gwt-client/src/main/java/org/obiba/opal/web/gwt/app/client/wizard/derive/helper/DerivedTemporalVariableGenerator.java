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

import java.util.Date;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.TemporalVariableDerivationHelper.GroupMethod;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.model.client.magma.VariableDto;

/**
 *
 */
public class DerivedTemporalVariableGenerator extends DerivedVariableGenerator {

  private final GroupMethod groupMethod;

  private final Date fromDate;

  private final Date toDate;

  public DerivedTemporalVariableGenerator(VariableDto originalVariable, List<ValueMapEntry> valueMapEntries, GroupMethod groupMethod, Date fromDate, Date toDate) {
    super(originalVariable, valueMapEntries);
    this.groupMethod = groupMethod;
    this.fromDate = fromDate;
    this.toDate = toDate;
  }

  @Override
  protected void generateScript() {
    scriptBuilder.append("$('" + originalVariable.getName() + "')");
    scriptBuilder.append("." + groupMethod.getScript(fromDate, toDate));
    scriptBuilder.append(".map({");
    appendDistinctValueMapEntries();
    scriptBuilder.append("\n  }");
    appendSpecialValuesEntry(getOtherValuesMapEntry());
    appendSpecialValuesEntry(getEmptyValuesMapEntry());
    scriptBuilder.append(");");
  }
}
