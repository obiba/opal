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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 *
 */
public abstract class AbstractFieldValidator implements FieldValidator {
  //
  // Instance Variables
  //

  private final String errorMessageKey;

  private List<String> args = new ArrayList<String>();

  private String id;

  //
  // Constructors
  //

  public AbstractFieldValidator(String errorMessageKey) {
    this.errorMessageKey = errorMessageKey;
  }

  public AbstractFieldValidator(String errorMessageKey, String id) {
    this.errorMessageKey = errorMessageKey;
    this.id = id;
  }

  //
  // FieldValidator Methods
  //

  @Nullable
  @Override
  public final String validate() {
    return hasError() ? errorMessageKey : null;
  }

  public void setArgs(List<String> args) {
    this.args = args;
  }

  public List<String> getArgs() {
    return args;
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public AbstractFieldValidator setId(String id) {
    this.id = id;
    return this;
  }

  @Override
  public String getId() {
    return id;
  }

  //
  // Methods
  //

  protected abstract boolean hasError();
}
