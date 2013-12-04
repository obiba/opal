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

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.support.PermissionResourceType;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.Acl;
import org.obiba.opal.web.model.client.opal.VcsCommitInfoDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ResourcePermissionsPresenter extends PresenterWidget<ResourcePermissionsPresenter.Display>
    implements ResourcePermissionsUiHandlers {

  private PermissionResourceType resourceType;
  private String resourcePath;

  @Inject
  public ResourcePermissionsPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  public void initialize(@Nonnull PermissionResourceType type, @Nonnull String resource) {
    resourceType = type;
    resourcePath = resource;
    retrievePermissions();
  }

  @Override
  protected void onBind() {
    super.onBind();

    getView().getActions().setActionHandler(new ActionHandler<Acl>() {

      @Override
      public void doAction(Acl acl, String actionName) {
        if (ActionsColumn.EDIT_ACTION.equals(actionName)) {

        }
        else if (ActionsColumn.DELETE_ACTION.equals(actionName)) {

        }
      }
    });

  }

  private void retrievePermissions() {
    ResourceRequestBuilderFactory.<JsArray<Acl>>newBuilder()
        .forResource(UriBuilder.create().fromPath(resourcePath).build()).get()
        .withCallback(new ResourceCallback<JsArray<Acl>>() {
          @Override
          public void onResource(Response response, JsArray<Acl> acls) {
            getView().setData(resourceType, JsArrays.toList(acls));
          }
        }).send();
  }

  public interface Display extends View, HasUiHandlers<ResourcePermissionsUiHandlers> {
    void setData(PermissionResourceType resourceType, List<Acl> acls);
    HasActionHandler<Acl> getActions();
  }
}
