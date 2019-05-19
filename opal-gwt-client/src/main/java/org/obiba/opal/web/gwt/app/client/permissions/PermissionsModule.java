/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.permissions;

import org.obiba.opal.web.gwt.app.client.permissions.presenter.AddResourcePermissionModalPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.DeleteAllConfirmationModalPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.UpdateResourcePermissionModalPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.view.AddResourcePermissionModalView;
import org.obiba.opal.web.gwt.app.client.permissions.view.DeleteAllConfirmationModalView;
import org.obiba.opal.web.gwt.app.client.permissions.view.ResourcePermissionsView;
import org.obiba.opal.web.gwt.app.client.permissions.view.UpdateResourcePermissionModalView;
import org.obiba.opal.web.gwt.app.client.project.permissions.ProjectPermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.project.permissions.ProjectPermissionsView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 *
 */
public class PermissionsModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenterWidget(ResourcePermissionsPresenter.class, ResourcePermissionsPresenter.Display.class,
        ResourcePermissionsView.class);
    bindPresenterWidget(ProjectPermissionsPresenter.class, ProjectPermissionsPresenter.Display.class,
        ProjectPermissionsView.class);
    bindPresenterWidget(UpdateResourcePermissionModalPresenter.class,
        UpdateResourcePermissionModalPresenter.Display.class, UpdateResourcePermissionModalView.class);
    bindPresenterWidget(AddResourcePermissionModalPresenter.class, AddResourcePermissionModalPresenter.Display.class,
        AddResourcePermissionModalView.class);
    bindPresenterWidget(DeleteAllConfirmationModalPresenter.class, DeleteAllConfirmationModalPresenter.Display.class,
        DeleteAllConfirmationModalView.class);
  }

}
