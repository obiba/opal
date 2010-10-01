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

import com.google.gwt.user.client.ui.HasText;

/**
 *
 */
public class RequiredTextValidator extends AbstractFieldValidator {
  //
  // Instance Variables
  //

  private HasText textField;

  //
  // Constructors
  //

  public RequiredTextValidator(HasText textField, String errorMessageKey) {
    super(errorMessageKey);
    this.textField = textField;
  }

  //
  // AbstractFieldValidator Methods
  //

  @Override
  protected boolean hasError() {
    return textField.getText() == null || textField.getText().trim().length() == 0;
  }
}
