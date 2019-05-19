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

import java.util.Set;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;

import com.google.gwt.core.client.GWT;

public abstract class ViewValidationHandler implements ValidationHandler {

  private static final Translations translations = GWT.create(Translations.class);

  protected abstract Set<FieldValidator> getValidators();

  protected abstract void showMessage(String id, String message);

  @Override
  public boolean validate() {
    boolean valid = true;
    for(FieldValidator validator : getValidators()) {
      String message = validator.validate();
      if(message != null) {
        valid = false;
        if (translations.userMessageMap().containsKey(message)) {
          message = translations.userMessageMap().get(message);
        }
        if(validator instanceof AbstractFieldValidator && !((AbstractFieldValidator) validator).getArgs().isEmpty()) {
          message = TranslationsUtils.replaceArguments(message, ((AbstractFieldValidator) validator).getArgs());
        }
        showMessage(validator.getId(), message);
      }
    }

    return valid;
  }

}
