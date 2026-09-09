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
 * Two projects whose names differ only in case would be one folder on a case insensitive file system, and the second
 * project would open the first one's database - silently, with both of them writing metadata into it.
 * <p>
 * An {@code IllegalArgumentException} so that the refusal reaches a client as a 400: the project is not created.
 */
public class ConflictingProjectDatabaseException extends IllegalArgumentException {

  private static final long serialVersionUID = 5545806123068893472L;

  private final String project;

  private final String conflictingProject;

  public ConflictingProjectDatabaseException(String project, String conflictingProject, String folder) {
    super("Project '" + project + "' cannot use an internal database: project '" + conflictingProject +
        "' already owns " + folder + ", and the two names differ only in case.");
    this.project = project;
    this.conflictingProject = conflictingProject;
  }

  public String getProject() {
    return project;
  }

  public String getConflictingProject() {
    return conflictingProject;
  }
}
