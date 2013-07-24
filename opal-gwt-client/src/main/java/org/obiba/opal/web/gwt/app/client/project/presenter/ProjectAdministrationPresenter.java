/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.presenter;

import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ProjectAdministrationPresenter extends PresenterWidget<ProjectAdministrationPresenter.Display> {

  @Inject
  public ProjectAdministrationPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
  }

  public void setProject(ProjectDto project) {
    getView().setProject(project);
  }

  public interface Display extends View {
    void setProject(ProjectDto project);
  }

}
