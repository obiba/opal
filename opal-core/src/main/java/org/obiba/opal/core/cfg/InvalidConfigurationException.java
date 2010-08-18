/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.cfg;

/**
 *
 */
public class InvalidConfigurationException extends RuntimeException {

  private static final long serialVersionUID = -5339727974864943590L;

  public InvalidConfigurationException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
