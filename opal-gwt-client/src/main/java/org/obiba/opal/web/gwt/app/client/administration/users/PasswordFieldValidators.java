/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.users;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.Lists;
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

  private final Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  public PasswordFieldValidators(HasText password, HasText confirmation, String passwordForm) {
    this.password = password;
    this.confirmation = confirmation;
    this.passwordForm = passwordForm;
    addValidators();
  }

  public Set<FieldValidator> getValidators() {
    return validators;
  }

  private void addValidators() {

    validators.add(new RequiredTextValidator(password, "PasswordIsRequired", passwordForm));
    ConditionValidator minLength = new ConditionValidator(minLengthCondition(password), "PasswordLengthMin",
        passwordForm);
    minLength.setArgs(Lists.newArrayList(String.valueOf(MIN_PASSWORD_LENGTH)));
    validators.add(minLength);
    validators.add(
        new ConditionValidator(passwordsMatchCondition(password, confirmation), "PasswordsMustMatch", passwordForm));
  }

  private HasValue<Boolean> minLengthCondition(final HasText passwordHasText) {
    return new HasBooleanValue() {
      @Override
      public Boolean getValue() {
        return passwordHasText.getText().isEmpty() || passwordHasText.getText().length() >= MIN_PASSWORD_LENGTH;
      }
    };
  }

  private HasValue<Boolean> passwordsMatchCondition(final HasText passwordHasText, final HasText confirmPassword) {
    return new HasBooleanValue() {
      @Override
      public Boolean getValue() {
        return passwordHasText.getText().isEmpty() && confirmPassword.getText().isEmpty() ||
            passwordHasText.getText().equals(confirmPassword.getText());
      }
    };
  }
}
