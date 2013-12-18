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
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.Acl;
import org.obiba.opal.web.model.client.opal.ProjectDto;
import org.obiba.opal.web.model.client.opal.Subject;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class ProjectResourcePermissionsPresenter extends PresenterWidget<ProjectResourcePermissionsPresenter.Display>
    implements ProjectResourcePermissionsUiHandlers, DeleteAllSubjectPermissionsHandler {

  private final ModalProvider<DeleteAllConfirmationModalPresenter> deleteAllConfirmationModalProvider;

  private ProjectDto project;

  @Inject
  public ProjectResourcePermissionsPresenter(Display display, EventBus eventBus,
      ModalProvider<DeleteAllConfirmationModalPresenter> deleteAllConfirmationModalProvider) {
    super(eventBus, display);
    this.deleteAllConfirmationModalProvider = deleteAllConfirmationModalProvider.setContainer(this);
    getView().setUiHandlers(this);
    getView().setNodeToPlaceConverter(new NodeToPlaceConverterImpl());
    getView().setNodeNameFormatter(new NodeNameFormatterImpl());
  }

  public void initialize(@Nonnull ProjectDto project) {
    this.project = project;
    retrievePermissions();
  }

  @Override
  protected void onBind() {
    super.onBind();

    getView().getActions().setActionHandler(new ActionHandler<Acl>() {

      @Override
      public void doAction(Acl acl, String actionName) {
        if(ActionsColumn.DELETE_ACTION.equals(actionName)) {
          deletePersmission(acl);
        }
      }
    });
  }

  @Override
  public void selectSubject(final Subject subject) {
    String requestPath = ResourcePermissionRequestPaths.projectSubject(project.getName(), subject.getPrincipal());
    ResourceRequestBuilderFactory.<JsArray<Acl>>newBuilder().forResource(UriBuilder.create().fromPath(requestPath)
        .query(ResourcePermissionRequestPaths.TYPE_QUERY_PARAM, subject.getType().getName()).build()).get()
        .withCallback(new ResourceCallback<JsArray<Acl>>() {
          @Override
          public void onResource(Response response, JsArray<Acl> subjectAcls) {
            getView().setSubjectData(subject, JsArrays.toList(subjectAcls));
          }
        }).send();
  }

  public void deletePersmission(Acl acl) {
    final Subject subject = acl.getSubject();
    String principal = subject.getPrincipal();
    String requestPath = ResourcePermissionRequestPaths
        .projectNode(ResourcePermissionType.getTypeByPermission(acl.getActions(0)), project.getName(),
            acl.getResource());
    String uri = UriBuilder.create().fromPath(requestPath)
        .query(ResourcePermissionRequestPaths.PRINCIPAL_QUERY_PARAM, principal)
        .query(ResourcePermissionRequestPaths.TYPE_QUERY_PARAM, subject.getType().getName()).build();
    ResourceRequestBuilderFactory.<JsArray<Acl>>newBuilder().forResource(uri).delete()
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            selectSubject(subject);
          }
        }).send();
  }

  @Override
  public void deleteAllPermissions(final Subject subject) {
    deleteAllConfirmationModalProvider.get().initialize(subject, this);
  }

  private void retrievePermissions() {
    String resourcePath = ResourcePermissionRequestPaths.projectSubjects(project.getName());
    ResourceRequestBuilderFactory.<JsArray<Subject>>newBuilder()
        .forResource(UriBuilder.create().fromPath(resourcePath).build()).get()
        .withCallback(new ResourceCallback<JsArray<Subject>>() {
          @Override
          public void onResource(Response response, JsArray<Subject> subjects) {
            final List<Subject> subjectList = JsArrays.toList(subjects);
            getView().setData(subjectList);

            if(subjectList.size() > 0) {
              selectSubject(subjectList.get(0));
            }
          }
        }).send();
  }

  @Override
  public void deleteAllSubjectPermissions(final Subject subject) {
    String requestPath = ResourcePermissionRequestPaths.projectSubject(project.getName(), subject.getPrincipal());
    String uri = UriBuilder.create().fromPath(requestPath)
        .query(ResourcePermissionRequestPaths.TYPE_QUERY_PARAM, subject.getType().getName()).build();
    ResourceRequestBuilderFactory.<JsArray<Acl>>newBuilder().forResource(uri).delete()
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            selectSubject(subject);
          }
        }).send();

  }

  public interface Display extends View, HasUiHandlers<ProjectResourcePermissionsUiHandlers> {
    void setData(@Nonnull List<Subject> subjects);

    void setSubjectData(Subject subject, List<Acl> subjectAcls);

    void setNodeToPlaceConverter(NodeToPlaceConverter converter);

    void setNodeNameFormatter(NodeNameFormatter formatter);

    HasActionHandler<Acl> getActions();
  }

  public interface NodeToPlaceConverter {
    PlaceRequest convert(Acl acl);
  }

  private static final class NodeToPlaceConverterImpl implements NodeToPlaceConverter {

    @Override
    public PlaceRequest convert(Acl acl) {
      ResourcePermissionType type = ResourcePermissionType.getTypeByPermission(acl.getActions(0));
      String[] parts = acl.getResource().split("/");
      PlaceRequest placeRequest = null;

      switch (type) {
        case PROJECT:
          placeRequest = ProjectPlacesHelper.getProjectPlace(parts[2]);
          break;

        case DATASOURCE:
          placeRequest = ProjectPlacesHelper.getDatasourcePlace(parts[2]);
          break;

        case TABLE:
          placeRequest = ProjectPlacesHelper.getTablePlace(parts[2], parts[4]);
          break;

        case VARIABLE:
          placeRequest = ProjectPlacesHelper.getVariablePlace(parts[2], parts[4], parts[6]);
          break;

        case REPORT_TEMPLATE:
          placeRequest = ProjectPlacesHelper.getReportsPlace(parts[2]);
          break;
      }

      assert placeRequest != null;
      return placeRequest;
    }
  }


  public interface NodeNameFormatter {
    String format(Acl acl);
  }

  private static final class NodeNameFormatterImpl implements NodeNameFormatter {

    @Override
    public String format(Acl acl) {
      ResourcePermissionType type = ResourcePermissionType.getTypeByPermission(acl.getActions(0));
      String[] parts = acl.getResource().split("/");
      String name = null;

      switch (type) {
        case PROJECT:
          name = parts[2] + " [project]";
          break;

        case DATASOURCE:
          name = "All Tables";
          break;

        case TABLE:
          name = parts[2] + " [table]";
          break;

        case VARIABLE:
              name = parts[4] + ":" + parts[6];
          break;

        case REPORT_TEMPLATE:
          name = parts[2] + " [report]";
          break;
      }

      assert name != null;
      return name;
    }
  }

}
