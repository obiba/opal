/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service;

import org.obiba.opal.core.unit.FunctionalUnit;

/**
 * Thrown when a method argument refers to a non-existing {@link FunctionalUnit}.
 */
public class NoSuchFunctionalUnitException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private String unitName;

  public NoSuchFunctionalUnitException(String unitName) {
    super("No such functional unit (" + unitName + ")");
    this.unitName = unitName;
  }

  public String getUnitName() {
    return unitName;
  }
}
