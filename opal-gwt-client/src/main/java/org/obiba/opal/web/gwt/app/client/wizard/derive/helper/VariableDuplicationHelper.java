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

import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.model.client.magma.VariableDto;

public class VariableDuplicationHelper extends DerivationHelper {

  public VariableDuplicationHelper(VariableDto originalVariable) {
    this(originalVariable, null);
  }

  public VariableDuplicationHelper(VariableDto originalVariable, VariableDto destination) {
    super(originalVariable, destination);
  }

  @Override
  protected void initializeValueMapEntries() {

  }

  @Override
  public VariableDto getDerivedVariable() {
    VariableDto derived = DerivedVariableGenerator.copyVariable(originalVariable, true);
    VariableDtos.setScript(derived, "$('" + originalVariable.getName() + "')");
    return derived;
  }

  @Override
  protected DerivedVariableGenerator getDerivedVariableGenerator() {
    return null;
  }

}