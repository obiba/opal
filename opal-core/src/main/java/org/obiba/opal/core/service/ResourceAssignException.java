/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.google.common.base.Joiner;

import java.util.List;

public class ResourceAssignException extends RuntimeException {

  private final String project;

  private final String name;

  private final List<String> requiredPackages;

  public ResourceAssignException(String project, String name, List<String> requiredPackages) {
    super(String.format("Resource \"%s\" in project \"%s\" cannot be assigned. Associated R packages are probably missing or not functional: %s", name, project, Joiner.on(", ").join(requiredPackages)));
    this.project = project;
    this.name = name;
    this.requiredPackages = requiredPackages;
  }

  public String getProject() {
    return project;
  }

  public String getName() {
    return name;
  }

  public List<String> getRequiredPackages() {
    return requiredPackages;
  }
}
