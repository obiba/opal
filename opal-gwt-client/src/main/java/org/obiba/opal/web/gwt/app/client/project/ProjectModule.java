/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.project;

import org.obiba.opal.web.gwt.app.client.inject.AbstractOpalModule;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectKeyStorePresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectsPresenter;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectAdministrationView;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectKeyStoreView;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectPropertiesModalView;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectView;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectsView;

/**
 *
 */
@SuppressWarnings("OverlyCoupledClass")
public class ProjectModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    bindPresenter(ProjectsPresenter.class, ProjectsPresenter.Display.class, ProjectsView.class,
        ProjectsPresenter.Proxy.class);
    bindPresenter(ProjectPresenter.class, ProjectPresenter.Display.class, ProjectView.class,
        ProjectPresenter.Proxy.class);
    bindPresenterWidget(ProjectAdministrationPresenter.class, ProjectAdministrationPresenter.Display.class,
        ProjectAdministrationView.class);
    bindPresenterWidget(ProjectPropertiesModalPresenter.class, ProjectPropertiesModalPresenter.Display.class,
        ProjectPropertiesModalView.class);
    bindPresenterWidget(ProjectKeyStorePresenter.class, ProjectKeyStorePresenter.Display.class,
        ProjectKeyStoreView.class);
  }
}
