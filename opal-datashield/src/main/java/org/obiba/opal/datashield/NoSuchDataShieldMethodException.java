/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datashield;

public class NoSuchDataShieldMethodException extends RuntimeException {

  private static final long serialVersionUID = -1295544695602633502L;

  private final String name;

  public NoSuchDataShieldMethodException(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
