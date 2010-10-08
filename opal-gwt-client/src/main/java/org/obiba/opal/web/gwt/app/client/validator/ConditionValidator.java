/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.validator;

import com.google.gwt.user.client.ui.HasValue;

/**
 *
 */
public class ConditionValidator extends AbstractFieldValidator {
  //
  // Instance Variables
  //

  private HasValue<Boolean> condition;

  //
  // Constructors
  //

  public ConditionValidator(HasValue<Boolean> condition, String errorMessageKey) {
    super(errorMessageKey);
    this.condition = condition;
  }

  //
  // AbstractFieldValidator Methods
  //

  @Override
  protected boolean hasError() {
    return !condition.getValue();
  }

}
