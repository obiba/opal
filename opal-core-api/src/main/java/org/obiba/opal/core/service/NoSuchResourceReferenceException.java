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

public class NoSuchResourceReferenceException extends RuntimeException {

  private final String project;

  private final String name;

  public NoSuchResourceReferenceException(String project, String name) {
    super("No Resource with name \"" + name +"\" in project \"" + project + "\"");
    this.project = project;
    this.name = name;
  }

  public String getProject() {
    return project;
  }

  public String getName() {
    return name;
  }
}
