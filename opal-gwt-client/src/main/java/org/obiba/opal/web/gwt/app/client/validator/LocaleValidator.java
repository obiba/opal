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

import org.obiba.opal.web.gwt.app.client.support.LanguageLocale;

public class LocaleValidator extends AbstractFieldValidator {

  private final String locale;

  public LocaleValidator(String locale, String errorMessageKey) {
    this(locale, errorMessageKey, null);
  }

  public LocaleValidator(String locale, String errorMessageKey, String id) {
    super(errorMessageKey, id);
    this.locale = locale;
  }

  @Override
  public boolean hasError() {
    return !LanguageLocale.isValid(locale);
  }
}
