/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.math;

import org.obiba.magma.Category;
import org.obiba.magma.Variable;

enum VariableNature {

  CATEGORICAL, CONTINUOUS, TEMPORAL, UNDETERMINED;

  static VariableNature getNature(Variable variable) {
    if(variable.hasCategories()) {
      if(isAllMissing(variable.getCategories()) == false) {
        return CATEGORICAL;
      } else {
        return CONTINUOUS;
      }
    }
    if(variable.getValueType().isNumeric()) {
      return CONTINUOUS;
    }
    if(variable.getValueType().isDateTime()) {
      return TEMPORAL;
    }
    return UNDETERMINED;
  }

  private static boolean isAllMissing(Iterable<Category> categories) {
    for(Category c : categories) {
      if(c.isMissing() == false) return false;
    }
    return true;
  }

}
