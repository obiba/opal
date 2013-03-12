/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.service;

import org.obiba.opal.shell.CommandJob;

/**
 * Signals an attempt to reference a {@link CommandJob} that does not exist.
 */
public class NoSuchCommandJobException extends RuntimeException {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Constructors
  //

  public NoSuchCommandJobException(Integer jobId) {
    super(jobId.toString());
  }
}
