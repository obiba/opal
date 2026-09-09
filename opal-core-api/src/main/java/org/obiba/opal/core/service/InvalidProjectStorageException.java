/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

/**
 * The storage a project was asked to use cannot be given to it. An {@code IllegalArgumentException} so that the
 * refusal reaches a client as a 400.
 */
public class InvalidProjectStorageException extends IllegalArgumentException {

  private static final long serialVersionUID = 8534002951231447920L;

  private final String project;

  private InvalidProjectStorageException(String project, String message) {
    super(message);
    this.project = project;
  }

  /**
   * Changing where a project stores its data drops what it holds - and, for a database the project owns, deletes a
   * file. The administration UI has always refused it; the server refuses it too, for registered storage alike.
   */
  public static InvalidProjectStorageException hasData(String project) {
    return new InvalidProjectStorageException(project,
        "Project '" + project + "' has tables: its storage cannot be changed while it holds data.");
  }

  /**
   * Two projects in one project-owned database is precisely what internal storage exists to prevent.
   */
  public static InvalidProjectStorageException ownedByAnotherProject(String project, String database, String owner) {
    return new InvalidProjectStorageException(project,
        "Project '" + project + "' cannot use database '" + database + "': it belongs to project '" + owner + "'.");
  }

  public String getProject() {
    return project;
  }
}
