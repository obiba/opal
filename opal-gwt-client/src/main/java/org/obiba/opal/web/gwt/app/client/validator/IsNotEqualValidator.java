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

public class IsNotEqualValidator<T> extends AbstractFieldValidator {

  private final HasValue<T> hasValue;

  private final T notExpected;

  public IsNotEqualValidator(HasValue<T> hasValue, T notExpected, String errorMessageKey) {
    super(errorMessageKey);
    this.hasValue = hasValue;
    this.notExpected = notExpected;
  }

  @Override
  protected boolean hasError() {
    T value = hasValue.getValue();
    return value == null || value.equals(notExpected) == true;
  }
}
