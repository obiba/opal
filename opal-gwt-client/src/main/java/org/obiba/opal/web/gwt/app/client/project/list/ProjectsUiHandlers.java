/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.list;

import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface ProjectsUiHandlers extends UiHandlers {

  void onProjectSelection(ProjectDto project);

  void onProjectTableSelection(ProjectDto project, String table);

  void showAddProject();

  void onProjectsFilterUpdate(String filter);
}
