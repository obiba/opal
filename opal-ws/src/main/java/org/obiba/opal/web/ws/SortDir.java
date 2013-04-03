/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.ws;

public enum SortDir {

  ASC("asc"), DESC("desc");

  public String toString() {
    return value;
  }

  //
  // Private members
  //

  private final String value;

  SortDir(String value) {
    this.value = value;
  }
}
