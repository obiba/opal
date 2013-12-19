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

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
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
      ModalProvider<DeleteAllConfirmationModalPresenter> deleteAllConfirmationModalProvider,
      Translations translations) {
    super(eventBus, display);
    this.deleteAllConfirmationModalProvider = deleteAllConfirmationModalProvider.setContainer(this);
    getView().setUiHandlers(this);
    getView().initializeTable(new NodeToPlaceConverterImpl(), new NodeNameFormatterImpl(translations),
        new NodeToTypeMapperImpl(translations), new ResourceTypeComparator());
  }

  public void initialize(@Nonnull ProjectDto projectDto) {
    project = projectDto;
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
          public void onResource(Response response, JsArray<Acl> acls) {
            List<Acl> subjectAcls = JsArrays.toList(acls);
            if(subjectAcls.size() > 0) {
              getView().setSubjectData(subject, subjectAcls);
            } else {
              // refresh and select another subject if any
              retrievePermissions();
            }
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
  public void deleteAllPermissions(Subject subject) {
    deleteAllConfirmationModalProvider.get().initialize(subject, this);
  }

  private void retrievePermissions() {
    String resourcePath = ResourcePermissionRequestPaths.projectSubjects(project.getName());
    ResourceRequestBuilderFactory.<JsArray<Subject>>newBuilder()
        .forResource(UriBuilder.create().fromPath(resourcePath).build()).get()
        .withCallback(new ResourceCallback<JsArray<Subject>>() {
          @Override
          public void onResource(Response response, JsArray<Subject> subjects) {
            List<Subject> subjectList = JsArrays.toList(subjects);
            getView().setData(subjectList);

            if(subjectList.size() > 0) {
              selectFirstUser(subjectList);
            }
          }

          private void selectFirstUser(List<Subject> subjectList) {
            Subject selection = subjectList.get(0);
            if(!Subject.SubjectType.USER.isSubjectType(selection.getType())) {
              for(Subject subject : subjectList) {
                if(Subject.SubjectType.USER.isSubjectType(subject.getType())) {
                  selection = subject;
                  break;
                }
              }
            }
            selectSubject(selection);
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

    void initializeTable(NodeToPlaceMapper nodeToPlaceMapper, NodeNameFormatter formatter,
        NodeToTypeMapper nodeToTypeMapper, Comparator<Acl> resourceTypeComparator);

    HasActionHandler<Acl> getActions();
  }

  public interface NodeToPlaceMapper {
    PlaceRequest map(Acl acl);
  }

  private static final class NodeToPlaceConverterImpl implements NodeToPlaceMapper {

    @Override
    public PlaceRequest map(Acl acl) {
      return getPlaceRequest(ResourcePermissionType.getTypeByPermission(acl.getActions(0)),
          acl.getResource().split("/"));
    }

    private PlaceRequest getPlaceRequest(ResourcePermissionType type, String... parts) {
      PlaceRequest placeRequest = null;
      switch(type) {
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

    private final Translations translations;

    private NodeNameFormatterImpl(Translations translations) {
      this.translations = translations;
    }

    @Override
    public String format(Acl acl) {
      return getName(ResourcePermissionType.getTypeByPermission(acl.getActions(0)), acl.getResource().split("/"));
    }

    private String getName(ResourcePermissionType type, String... parts) {
      String name = null;
      switch(type) {
        case PROJECT:
          name = parts[2];
          break;

        case DATASOURCE:
          name = translations.allTablesLabel();
          break;

        case TABLE:
          name = parts[4];
          break;

        case VARIABLE:
          name = parts[4] + ":" + parts[6];
          break;

        case REPORT_TEMPLATE:
          name = parts[2];
          break;
      }

      assert name != null;
      return name;
    }
  }

  public interface NodeToTypeMapper {
    String map(Acl acl);
  }

  private static final class NodeToTypeMapperImpl implements NodeToTypeMapper {

    private final Translations translations;

    private NodeToTypeMapperImpl(Translations translations) {
      this.translations = translations;
    }

    @Override
    public String map(Acl acl) {
      ResourcePermissionType type = ResourcePermissionType.getTypeByPermission(acl.getActions(0));
      return TranslationsUtils.replaceArguments(translations.permissionResourceNodeTypeMap().get(type.name()));
    }
  }

  private static final class ResourceTypeComparator implements Comparator<Acl> {
    @Override
    public int compare(Acl o1, Acl o2) {
      ResourcePermissionType t1 = ResourcePermissionType.getTypeByPermission(o1.getActions(0));
      ResourcePermissionType t2 = ResourcePermissionType.getTypeByPermission(o2.getActions(0));

      if(t1.ordinal() < t2.ordinal()) {
        return 1;
      }

      if(t1.ordinal() > t2.ordinal()) {
        return -1;
      }

      return 0;
    }
  }

}
