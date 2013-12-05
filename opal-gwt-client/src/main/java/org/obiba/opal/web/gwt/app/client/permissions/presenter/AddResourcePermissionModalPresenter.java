/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions.presenter;

import java.util.List;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.permissions.support.PermissionResourceType;
import org.obiba.opal.web.gwt.app.client.permissions.support.events.UpdateResourcePermissionEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class AddResourcePermissionModalPresenter
    extends ModalPresenterWidget<AddResourcePermissionModalPresenter.Display>
    implements ResourcePermissionModalUiHandlers {

  @Inject
  public AddResourcePermissionModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  public void initialize(@Nonnull PermissionResourceType type) {
    getView().setData(type);
  }

  @Override
  public void save() {
    fireEvent(new UpdateResourcePermissionEvent(getView().getPrincipals(), getView().getSubjectType(), getView().getPermission()));
    getView().close();
  }

  public interface Display extends PopupView, HasUiHandlers<ResourcePermissionModalUiHandlers> {
    void setData(PermissionResourceType type);
    String getPermission();
    List<String> getPrincipals();
    String getSubjectType();
    void close();
  }
}