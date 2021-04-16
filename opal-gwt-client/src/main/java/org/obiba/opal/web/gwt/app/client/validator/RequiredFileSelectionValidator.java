/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.validator;

import com.google.common.base.Strings;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.HasText;

public class RequiredFileSelectionValidator extends AbstractFieldValidator  {

  private final String extensionPattern;

  private final HasText fileName;

  @SuppressWarnings("UnusedDeclaration")
  public RequiredFileSelectionValidator(String pattern, HasText file, String errorMessage) {
    this(pattern, file, errorMessage, null);
  }

  public RequiredFileSelectionValidator(String pattern, HasText file, String errorMessage, String id) {
    super(errorMessage, id);
    extensionPattern = pattern;
    fileName = file;
  }

  @Override
  public boolean hasError() {
    String name = fileName.getText().toLowerCase();
    return !Strings.isNullOrEmpty(name) && !RegExp.compile(extensionPattern).test(name);
  }
}
