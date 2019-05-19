/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.validator;

import com.google.gwt.user.client.TakesValue;

/**
 *
 */
public class RequiredValueValidator extends AbstractFieldValidator {

  private final TakesValue<?> takesValue;

  public RequiredValueValidator(TakesValue<?> takesValue, String errorMessageKey) {
    super(errorMessageKey);
    this.takesValue = takesValue;
  }

  public RequiredValueValidator(TakesValue<?> takesValue, String errorMessageKey, String id) {
    super(errorMessageKey, id);
    this.takesValue = takesValue;
  }

  @Override
  protected boolean hasError() {
    return takesValue.getValue() == null;
  }
}
