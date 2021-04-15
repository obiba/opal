/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.model.client.opal.Acl;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class UpdateResourcePermissionModalPresenter
    extends ModalPresenterWidget<UpdateResourcePermissionModalPresenter.Display>
    implements ResourcePermissionModalUiHandlers {

  private Acl acl;

  private UpdateResourcePermissionHandler updateHandler;

  @Inject
  public UpdateResourcePermissionModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  public void initialize(@Nonnull ResourcePermissionType type, @Nonnull Acl acl,
      @Nonnull UpdateResourcePermissionHandler updateHandler) {
    this.acl = acl;
    this.updateHandler = updateHandler;
    getView().setData(type, acl);
  }

  @Override
  public void save() {
    if(updateHandler != null) {
      updateHandler.update(Lists.newArrayList(acl.getSubject().getPrincipal()), acl.getSubject().getType(),
          getView().getPermission());
    }
    getView().close();
  }

  public interface Display extends PopupView, HasUiHandlers<ResourcePermissionModalUiHandlers> {
    void setData(ResourcePermissionType type, Acl acl);

    String getPermission();

    void close();
  }

}

