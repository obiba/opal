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
import com.google.gwt.user.client.ui.HasValue;

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

  public RequiredTextValidator(final HasValue<String> hasValue, String errorMessageKey) {
    super(errorMessageKey);
    this.textField = new HasText() {

      @Override
      public String getText() {
        return hasValue.getValue();
      }

      @Override
      public void setText(String text) {
        hasValue.setValue(text);
      }

    };
  }

  //
  // AbstractFieldValidator Methods
  //

  @Override
  protected boolean hasError() {
    return textField.getText() == null || textField.getText().trim().length() == 0;
  }
}
