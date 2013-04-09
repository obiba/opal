/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.project;

import org.obiba.magma.Datasource;
import org.obiba.opal.project.Project;
import org.obiba.opal.project.cfg.ProjectsConfigurationService;

abstract class AbstractProjectResource {

  protected abstract ProjectsConfigurationService getProjectsConfigurationService();

  protected Project getProject(Datasource ds) {
    Project project;
    if (getProjectsConfigurationService().getConfig().hasProject(ds.getName())) {
      project = getProjectsConfigurationService().getConfig().getProject(ds.getName());
    } else {
      project = Project.Builder.create(ds.getName()).build();
    }
    return project;
  }

}
