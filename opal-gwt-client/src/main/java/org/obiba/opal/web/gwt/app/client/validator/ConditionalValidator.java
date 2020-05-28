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

import javax.annotation.Nullable;

import com.google.gwt.user.client.ui.HasValue;

/**
 *
 */
public class ConditionalValidator implements FieldValidator {
  //
  // Instance Variables
  //

  private final HasValue<Boolean> condition;

  private final FieldValidator delegate;

  private String id;

  //
  // Constructors
  //

  public ConditionalValidator(HasValue<Boolean> condition, FieldValidator delegate) {
    this.condition = condition;
    this.delegate = delegate;
  }

  //
  // FieldValidator Methods
  //

  @Nullable
  @Override
  public String validate() {
    if(condition.getValue()) {
      return delegate.validate();
    }
    return null;
  }

  public ConditionalValidator setId(String value) {
    id = value;
    return this;
  }

  @Override
  public String getId() {
    return id;
  }
}
