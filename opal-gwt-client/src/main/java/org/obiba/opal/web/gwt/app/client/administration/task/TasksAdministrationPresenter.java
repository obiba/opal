/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.task;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.task.presenter.TasksPresenter;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class TasksAdministrationPresenter
    extends ItemAdministrationPresenter<TasksAdministrationPresenter.Display, TasksAdministrationPresenter.Proxy> {

  @ProxyStandard
  @NameToken(Places.TASKS)
  public interface Proxy extends ProxyPlace<TasksAdministrationPresenter> {}

  private final DefaultBreadcrumbsBuilder breadcrumbsBuilder;

  private final TasksPresenter tasksPresenter;

  public interface Display extends View, HasBreadcrumbs {

  }

  @Inject
  public TasksAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy, TasksPresenter tasksPresenter,
      DefaultBreadcrumbsBuilder breadcrumbsBuilder) {
    super(eventBus, display, proxy);
    this.tasksPresenter = tasksPresenter;
    this.breadcrumbsBuilder = breadcrumbsBuilder;
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
  }

  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageJobsTitle();
  }

  @Override
  protected void onBind() {
    setInSlot("Jobs", tasksPresenter);
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs()).build();
  }

}
