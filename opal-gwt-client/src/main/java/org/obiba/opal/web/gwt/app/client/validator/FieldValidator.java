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

import javax.annotation.Nullable;

/**
 *
 */
public interface FieldValidator {

  /**
   * Validates the field to which it is attached.
   *
   * @return error message key if there is an error, <code>null</code> otherwise
   */
  @Nullable
  String validate();
}
