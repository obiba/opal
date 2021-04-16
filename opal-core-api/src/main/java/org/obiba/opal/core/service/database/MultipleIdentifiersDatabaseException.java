/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.database;

public class MultipleIdentifiersDatabaseException extends RuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  private final String existing;

  private final String database;

  public MultipleIdentifiersDatabaseException(String existing, String database) {
    super("Database for identifiers already exists: '" + existing + "'.");
    this.existing = existing;
    this.database = database;
  }

  public String getDatabase() {
    return database;
  }

  public String getExisting() {
    return existing;
  }
}
