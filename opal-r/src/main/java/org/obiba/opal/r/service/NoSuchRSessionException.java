/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.r.service;

/**
 * Exception thrown when a current R session is not defined at execution of an R operation or when no R session can be
 * found from the provided identifier.
 */
public class NoSuchRSessionException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private String rSessionId;

  public NoSuchRSessionException() {
    super();
  }

  public NoSuchRSessionException(String rSessionId) {
    super();
    this.rSessionId = rSessionId;
  }

  public String getrSessionId() {
    return rSessionId;
  }

}
