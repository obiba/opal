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
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.Acl;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ResourcePermissionsPresenter extends PresenterWidget<ResourcePermissionsPresenter.Display>
    implements ResourcePermissionsUiHandlers, UpdateResourcePermissionHandler {

  private final ModalProvider<AddResourcePermissionModalPresenter> addModalProvider;
  private final ModalProvider<UpdateResourcePermissionModalPresenter> updateModalProvider;
  private ResourcePermissionType resourceType;
  private String resourcePath;

  @Inject
  public ResourcePermissionsPresenter(Display display, EventBus eventBus,
      ModalProvider<AddResourcePermissionModalPresenter> addModalProvider,
      ModalProvider<UpdateResourcePermissionModalPresenter> updateModalProvider) {
    super(eventBus, display);
    this.addModalProvider = addModalProvider.setContainer(this);
    this.updateModalProvider = updateModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  public void initialize(@Nonnull ResourcePermissionType type, @Nonnull String resource) {
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
          editPersmission(acl);
        }
        else if (ActionsColumn.DELETE_ACTION.equals(actionName)) {
          deletePersmission(acl);
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

  @Override
  public void addPersmission() {
    addModalProvider.get().initialize(resourceType, this, getView().getAclList());
  }

  @Override
  public void editPersmission(Acl acl) {
    updateModalProvider.get().initialize(resourceType, acl, this);
  }

  @Override
  public void deletePersmission(Acl acl) {
    String uri = UriBuilder.create().fromPath(resourcePath)
        .query(ResourcePermissionRequestPaths.PRINCIPAL_QUERY_PARAM, acl.getSubject().getPrincipal())
        .query(ResourcePermissionRequestPaths.TYPE_QUERY_PARAM, acl.getSubject().getType().getName()).build();
    ResourceRequestBuilderFactory.<JsArray<Acl>>newBuilder()
        .forResource(uri)
        .delete()
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            retrievePermissions();
          }
        }).send();
  }

  @Override
  public void update(List<String> subjectPrincipals, String subjectType, String permission) {
    UriBuilder uriBuilder = UriBuilder.create().fromPath(resourcePath)
        .query(ResourcePermissionRequestPaths.TYPE_QUERY_PARAM, subjectType)//
        .query(ResourcePermissionRequestPaths.PERMISSION_QUERY_PARAM, permission);

    for (String principal : subjectPrincipals) {
      uriBuilder.query(ResourcePermissionRequestPaths.PRINCIPAL_QUERY_PARAM, principal);
    }


    ResourceRequestBuilderFactory.<JsArray<Acl>>newBuilder()
        .forResource(uriBuilder.build())
        .post()
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            retrievePermissions();
          }
        }).send();
  }

  public interface Display extends View, HasUiHandlers<ResourcePermissionsUiHandlers> {
    void setData(@Nonnull ResourcePermissionType resourceType, @Nonnull List<Acl> acls);
    HasActionHandler<Acl> getActions();

    List<Acl> getAclList();
  }

}
