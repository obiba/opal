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
public class RegExValidator extends AbstractFieldValidator {
  //
  // Instance Variables
  //

  private final HasText textField;

  private final String regex;

  private final String modifiers;

  //
  // Constructors
  //

  public RegExValidator(HasText textField, String regex, String errorMessageKey) {
    this(textField, regex, "", errorMessageKey);
  }

  public RegExValidator(HasText textField, String regex, String modifiers, String errorMessageKey) {
    super(errorMessageKey);

    this.textField = textField;
    this.regex = regex;
    this.modifiers = modifiers;
  }

  //
  // AbstractFieldValidator Methods
  //

  @Override
  protected boolean hasError() {
    return !matchesRegEx(textField.getText(), regex, modifiers);
  }

  public static native boolean matchesRegEx(String input, String regex)
  /*-{
      return input.match(new RegExp(regex)) != null;
  }-*/;

  public static native boolean matchesRegEx(String input, String regex, String modifiers)
  /*-{
      return input.match(new RegExp(regex, modifiers)) != null;
  }-*/;
}
