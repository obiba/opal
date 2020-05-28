/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.derive.helper;

import com.google.common.base.Strings;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos;
import org.obiba.opal.web.model.client.magma.VariableDto;

public class VariableDuplicationHelper extends DerivationHelper {

  private int valueAt = -1;

  public VariableDuplicationHelper(VariableDto originalVariable) {
    this(null, originalVariable, null);
  }

  public VariableDuplicationHelper(VariableDto originalVariable, int valueAt) {
    this(null, originalVariable, valueAt);
  }

  public VariableDuplicationHelper(String originalTableReference, VariableDto originalVariable, int valueAt) {
    this(originalTableReference, originalVariable, null);
    this.valueAt = valueAt;
  }

  public VariableDuplicationHelper(VariableDto originalVariable, VariableDto destination) {
    this(null, originalVariable, destination);
  }

  public VariableDuplicationHelper(String originalTableReference, VariableDto originalVariable, VariableDto destination) {
    super(originalTableReference, originalVariable, destination);
  }

  @Override
  protected void initializeValueMapEntries() {

  }

  @Override
  public VariableDto getDerivedVariable() {
    VariableDto derived = DerivedVariableGenerator.copyVariable(originalVariable, true, originalVariable.getLink());
    String script = "$('" + getOriginalVariableName() + "')";
    if (valueAt>=0) {
      if (Strings.isNullOrEmpty(originalTableReference)) script = script + ".valueAt(" + valueAt + ")";
      derived.setName(derived.getName() + "_" + (valueAt + 1));
      derived.setIsRepeatable(false);
      derived.setOccurrenceGroup("");
    }
    VariableDtos.setScript(derived, script);
    return derived;
  }

  @Override
  protected DerivedVariableGenerator getDerivedVariableGenerator() {
    return null;
  }

}