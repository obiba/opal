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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.TakesValue;

/**
 *
 */
public class RequiredOptionValidator extends AbstractFieldValidator {
  //
  // Instance Variables
  //

  private final Set<TakesValue<Boolean>> options;

  //
  // Constructors
  //

  public RequiredOptionValidator(Collection<TakesValue<Boolean>> options, String errorMessageKey) {
    super(errorMessageKey);

    if(options == null) {
      throw new IllegalArgumentException("null options");
    }
    if(options.isEmpty()) {
      throw new IllegalArgumentException("empty options");
    }

    this.options = new HashSet<TakesValue<Boolean>>();
    this.options.addAll(options);
  }

  //
  // AbstractFieldValidator Methods
  //

  @Override
  protected boolean hasError() {
    for(TakesValue<Boolean> option : options) {
      if(option.getValue()) {
        return false;
      }
    }
    return true;
  }

  //
  // Static Methods
  //

  public static Set<TakesValue<Boolean>> asSet(TakesValue<Boolean>... options) {
    Set<TakesValue<Boolean>> optionSet = new HashSet<TakesValue<Boolean>>();
    optionSet.addAll(Lists.newArrayList(options));
    return optionSet;
  }
}
