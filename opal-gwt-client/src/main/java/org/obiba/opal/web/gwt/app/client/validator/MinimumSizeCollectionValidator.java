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

import java.util.Collection;

/**
 *
 */
public class MinimumSizeCollectionValidator<T> extends AbstractFieldValidator {
  //
  // Instance Variables
  //

  private HasCollection<T> collectionField;

  private int minSize;

  //
  // Constructors
  //

  public MinimumSizeCollectionValidator(HasCollection<T> collectionField, int minSize, String errorMessageKey) {
    super(errorMessageKey);
    this.collectionField = collectionField;
    this.minSize = minSize;
  }

  //
  // AbstractFieldValidator Methods
  //

  @Override
  protected boolean hasError() {
    return collectionField.getCollection().size() < minSize;
  }

  //
  // Inner Classes / Interfaces
  //

  public static interface HasCollection<T> {

    Collection<T> getCollection();
  }
}
