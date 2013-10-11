/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.database;

public class NoSuchDatabaseException extends RuntimeException {

  private static final long serialVersionUID = -6357540199499515674L;

  private final String databaseName;

  public NoSuchDatabaseException(String databaseName) {
    super("No database exists with the specified name '" + databaseName + "'");
    this.databaseName = databaseName;
  }

  public String getDatabaseName() {
    return databaseName;
  }
}
