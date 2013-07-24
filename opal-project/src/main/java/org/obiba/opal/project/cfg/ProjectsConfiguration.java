/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.project.cfg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.project.NoSuchProjectException;
import org.obiba.opal.project.domain.Project;

public class ProjectsConfiguration implements OpalConfigurationExtension, Serializable {

  private List<Project> projects;

  public List<Project> getProjects() {
    return projects == null ? projects = new ArrayList<Project>() : projects;
  }

  /**
   * Check that there is a project with the given name.
   * @param name
   * @return
   */
  public boolean hasProject(String name) {
    for(Project p : getProjects()) {
      if(name.equals(p.getName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Add or update a project.
   * @param project
   */
  public void putProject(Project project) {
    removeProject(project.getName());
    projects.add(project);
  }

  public void removeProject(String name) {
    Project found = null;
    for(Project p : getProjects()) {
      if(p.getName().equals(name)) {
        found = p;
        break;
      }
    }
    if(found != null) {
      projects.remove(found);
    }
  }

  /**
   * Get the project by its name.
   * @param name
   * @return
   * @throws NoSuchProjectException if project cannot be found with the given name
   */
  public Project getProject(String name) {
    for(Project p : getProjects()) {
      if(p.getName().equals(name)) {
        return p;
      }
    }
    throw new NoSuchProjectException(name);
  }
}
