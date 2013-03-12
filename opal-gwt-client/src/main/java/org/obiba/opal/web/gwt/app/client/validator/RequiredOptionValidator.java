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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.ui.HasValue;

/**
 *
 */
public class RequiredOptionValidator extends AbstractFieldValidator {
  //
  // Instance Variables
  //

  private Set<HasValue<Boolean>> options;

  //
  // Constructors
  //

  public RequiredOptionValidator(Set<HasValue<Boolean>> options, String errorMessageKey) {
    super(errorMessageKey);

    if(options == null) {
      throw new IllegalArgumentException("null options");
    }
    if(options.isEmpty()) {
      throw new IllegalArgumentException("empty options");
    }

    this.options = new HashSet<HasValue<Boolean>>();
    this.options.addAll(options);
  }

  //
  // AbstractFieldValidator Methods
  //

  @Override
  protected boolean hasError() {
    for(HasValue<Boolean> option : options) {
      if(option.getValue()) {
        return false;
      }
    }
    return true;
  }

  //
  // Static Methods
  //

  public static Set<HasValue<Boolean>> asSet(HasValue<Boolean>... options) {
    Set<HasValue<Boolean>> optionSet = new HashSet<HasValue<Boolean>>();
    optionSet.addAll(Arrays.asList(options));
    return optionSet;
  }
}
