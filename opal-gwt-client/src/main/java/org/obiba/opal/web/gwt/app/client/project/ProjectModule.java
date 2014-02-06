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
import org.obiba.opal.web.gwt.app.client.project.admin.ProjectAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.project.admin.ProjectAdministrationView;
import org.obiba.opal.web.gwt.app.client.project.keystore.ProjectKeyStorePresenter;
import org.obiba.opal.web.gwt.app.client.project.keystore.ProjectKeyStoreView;
import org.obiba.opal.web.gwt.app.client.project.list.ProjectsPresenter;
import org.obiba.opal.web.gwt.app.client.project.list.ProjectsView;
import org.obiba.opal.web.gwt.app.client.project.properties.ProjectPropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.project.properties.ProjectPropertiesModalView;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectPresenter;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectView;

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
