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
public class DisallowedCharactersValidator extends AbstractFieldValidator {
  //
  // Instance Variables
  //

  private HasText textField;

  private char[] disallowedChars;

  //
  // Constructors
  //

  public DisallowedCharactersValidator(HasText textField, char[] disallowedChars, String errorMessageKey) {
    super(errorMessageKey);

    if(disallowedChars == null) {
      throw new IllegalArgumentException("null disallowedChars");
    }
    if(disallowedChars.length == 0) {
      throw new IllegalArgumentException("zero-length disallowedChars");
    }

    this.textField = textField;

    this.disallowedChars = new char[disallowedChars.length];
    System.arraycopy(disallowedChars, 0, this.disallowedChars, 0, disallowedChars.length);
  }

  //
  // AbstractFieldValidator
  //

  @Override
  protected boolean hasError() {
    String text = textField.getText();

    if(text != null) {
      for(char c : disallowedChars) {
        if(text.indexOf(c) != -1) {
          return true;
        }
      }
    }

    return false;
  }

}
