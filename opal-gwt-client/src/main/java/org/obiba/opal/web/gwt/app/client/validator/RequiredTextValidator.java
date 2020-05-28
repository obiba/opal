/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.validator;

import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;

/**
 *
 */
public class RequiredTextValidator extends AbstractFieldValidator {
  //
  // Instance Variables
  //

  private final HasText textField;

  //
  // Constructors
  //

  public RequiredTextValidator(HasText textField, String errorMessageKey) {
    super(errorMessageKey);
    this.textField = textField;
  }

  public RequiredTextValidator(HasText textField, String errorMessageKey, String id) {
    super(errorMessageKey, id);
    this.textField = textField;
  }

  public RequiredTextValidator(final HasValue<String> hasValue, String errorMessageKey) {
    this(new HasText() {

      @Override
      public String getText() {
        return hasValue.getValue();
      }

      @Override
      public void setText(String text) {
        hasValue.setValue(text);
      }

    }, errorMessageKey);
  }

  //
  // AbstractFieldValidator Methods
  //

  @Override
  protected boolean hasError() {
    //noinspection SimplifiableConditionalExpression
    boolean enabled = textField instanceof HasEnabled ? ((HasEnabled) textField).isEnabled() : true;
    return enabled && (textField.getText() == null || textField.getText().trim().isEmpty());
  }
}
