/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.reporting.service;

/**
 *
 */
public class ReportException extends Exception {

  private static final long serialVersionUID = -2033207336181207222L;

  public ReportException() {
    super();
  }

  public ReportException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReportException(String message) {
    super(message);
  }

  public ReportException(Throwable cause) {
    super(cause);
  }

}
