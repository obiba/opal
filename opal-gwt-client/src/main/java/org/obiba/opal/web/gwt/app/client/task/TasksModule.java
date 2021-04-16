/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.task;

import org.obiba.opal.web.gwt.app.client.administration.task.TasksAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.task.TasksAdministrationView;
import org.obiba.opal.web.gwt.app.client.task.presenter.TaskDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.task.presenter.TasksPresenter;
import org.obiba.opal.web.gwt.app.client.task.view.TaskDetailsView;
import org.obiba.opal.web.gwt.app.client.task.view.TasksView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class TasksModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    // Bind concrete implementations to interfaces
    bindPresenter(TasksAdministrationPresenter.class, TasksAdministrationPresenter.Display.class,
        TasksAdministrationView.class, TasksAdministrationPresenter.Proxy.class);
    bindPresenterWidget(TasksPresenter.class, TasksPresenter.Display.class, TasksView.class);
    bindPresenterWidget(TaskDetailsPresenter.class, TaskDetailsPresenter.Display.class, TaskDetailsView.class);
  }

}
