/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.users.support;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;

public class PasswordFieldValidators {
  private static final int MIN_PASSWORD_LENGTH = 6;

  private final String passwordForm;

  private final HasText confirmation;

  private final HasText password;

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  public PasswordFieldValidators(HasText passwd, HasText conf, String form) {
    password = passwd;
    confirmation = conf;
    passwordForm = form;
    addValidators();
  }

  public Set<FieldValidator> getValidators() {
    return validators;
  }

  private void addValidators() {

    validators.add(
        new RequiredTextValidator(password, "PasswordIsRequired", passwordForm));
    ConditionValidator minLength = new ConditionValidator(minLengthCondition(password),
        "PasswordLengthMin", passwordForm);
    minLength.setArgs(Arrays.asList(String.valueOf(MIN_PASSWORD_LENGTH)));
    validators.add(minLength);
    validators.add(
        new ConditionValidator(passwordsMatchCondition(password, confirmation), "PasswordsMustMatch", passwordForm));
  }

  private HasValue<Boolean> minLengthCondition(final HasText password) {
    return new HasBooleanValue() {
      @Override
      public Boolean getValue() {
        return password.getText().isEmpty() || password.getText().length() >= MIN_PASSWORD_LENGTH;
      }
    };
  }

  private HasValue<Boolean> passwordsMatchCondition(final HasText password, final HasText confirmPassword) {
    return new HasBooleanValue() {
      @Override
      public Boolean getValue() {
        return password.getText().isEmpty() && confirmPassword.getText().isEmpty() ||
            password.getText().equals(confirmPassword.getText());
      }
    };
  }
}
