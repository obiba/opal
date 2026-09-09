/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.database;

/**
 * A database created for a project is Opal's to edit and remove for as long as that project exists. An operator sees
 * it, and may test its connection, but the application is what manages it: deleting the project is what deletes it.
 */
public class DatabaseOwnedByProjectException extends RuntimeException {

  private static final long serialVersionUID = 4130832455137880183L;

  private final String database;

  private final String project;

  public DatabaseOwnedByProjectException(String database, String project) {
    super("Database '" + database + "' belongs to project '" + project +
        "' and is managed by Opal: it is deleted when the project is deleted.");
    this.database = database;
    this.project = project;
  }

  public String getDatabase() {
    return database;
  }

  public String getProject() {
    return project;
  }
}
