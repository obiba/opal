/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

public class NoSuchReportTemplateException extends RuntimeException {

  private static final long serialVersionUID = -6357540199499515674L;

  private final String name;

  private final String project;

  public NoSuchReportTemplateException(String name, String project) {
    super("No report template exists with name '" + name + "' in project '" + project + "'");
    this.name = name;
    this.project = project;
  }

  public String getName() {
    return name;
  }

  public String getProject() {
    return project;
  }
}
