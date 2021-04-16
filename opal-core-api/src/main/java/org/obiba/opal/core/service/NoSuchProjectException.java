/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

public class NoSuchProjectException extends RuntimeException {

  private static final long serialVersionUID = -3397221631139533468L;
  
  private final String projectName;

  public NoSuchProjectException(String projectName) {
    super("No project exists with the specified name '" + projectName + "'");
    this.projectName = projectName;
  }

  public String getProjectName() {
    return projectName;
  }
}
