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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.Acl;
import org.obiba.opal.web.model.client.opal.Subject;

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

  private ResourcePermissionRequestPaths.UriBuilders uriBuilder;

  private String[] resources;

  @Inject
  public ResourcePermissionsPresenter(Display display, EventBus eventBus,
      ModalProvider<AddResourcePermissionModalPresenter> addModalProvider,
      ModalProvider<UpdateResourcePermissionModalPresenter> updateModalProvider) {
    super(eventBus, display);
    this.addModalProvider = addModalProvider.setContainer(this);
    this.updateModalProvider = updateModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  public void initialize(@Nonnull ResourcePermissionType type,
      @Nonnull ResourcePermissionRequestPaths.UriBuilders uriBuilder, @Nonnull String... resources) {
    resourceType = type;
    this.uriBuilder = uriBuilder;
    this.resources = resources;
    refreshPermissions();
  }

  @Override
  protected void onBind() {
    super.onBind();

    getView().setActionHandler(new ActionHandler<Acl>() {

      @Override
      public void doAction(Acl acl, String actionName) {
        if(ActionsColumn.EDIT_ACTION.equals(actionName)) {
          editPermission(acl);
        } else if(ActionsColumn.REMOVE_ACTION.equals(actionName)) {
          deletePermission(acl);
        }
      }
    });
  }

  public void refreshPermissions() {
    ResourceRequestBuilderFactory.<JsArray<Acl>>newBuilder().forResource(uriBuilder.create().build(resources)).get()
        .withCallback(new ResourceCallback<JsArray<Acl>>() {
          @Override
          public void onResource(Response response, JsArray<Acl> acls) {
            getView().setData(JsArrays.toList(acls));
          }
        })
        .withCallback(Response.SC_FORBIDDEN, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setData(new ArrayList<Acl>());
          }
        }).send();
  }

  @Override
  public void addUserPermission() {
    addModalProvider.get().initialize(resourceType, this, getView().getAclList(), Subject.SubjectType.USER);
  }

  @Override
  public void addGroupPermission() {
    addModalProvider.get().initialize(resourceType, this, getView().getAclList(), Subject.SubjectType.GROUP);
  }

  @Override
  public void editPermission(Acl acl) {
    updateModalProvider.get().initialize(resourceType, acl, this);
  }

  @Override
  public void deletePermission(Acl acl) {
    String uri = uriBuilder.create()
        .query(ResourcePermissionRequestPaths.PRINCIPAL_QUERY_PARAM, acl.getSubject().getPrincipal())
        .query(ResourcePermissionRequestPaths.TYPE_QUERY_PARAM, acl.getSubject().getType().getName()).build(resources);
    ResourceRequestBuilderFactory.<JsArray<Acl>>newBuilder().forResource(uri).delete()
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            refreshPermissions();
          }
        })
        .withCallback(Response.SC_FORBIDDEN, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setData(new ArrayList<Acl>());
          }
        }).send();
  }

  @Override
  public void update(List<String> subjectPrincipals, Subject.SubjectType subjectType, String permission) {
    UriBuilder uri = uriBuilder.create().query(ResourcePermissionRequestPaths.TYPE_QUERY_PARAM, subjectType.getName())//
        .query(ResourcePermissionRequestPaths.PERMISSION_QUERY_PARAM, permission);

    for(String principal : subjectPrincipals) {
      uri.query(ResourcePermissionRequestPaths.PRINCIPAL_QUERY_PARAM, principal);
    }

    ResourceRequestBuilderFactory.<JsArray<Acl>>newBuilder().forResource(uri.build(resources)).post()
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            refreshPermissions();
          }
        })
        .withCallback(Response.SC_FORBIDDEN, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setData(new ArrayList<Acl>());
          }
        }).send();
  }

  public interface Display extends View, HasUiHandlers<ResourcePermissionsUiHandlers> {
    void setData(@Nonnull List<Acl> acls);

    void setActionHandler(ActionHandler<Acl> handler);

    List<Acl> getAclList();
  }

}
