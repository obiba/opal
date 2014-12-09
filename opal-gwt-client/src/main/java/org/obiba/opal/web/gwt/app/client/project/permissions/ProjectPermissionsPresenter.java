/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.permissions;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.DeleteAllConfirmationModalPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.DeleteAllSubjectPermissionsHandler;
import org.obiba.opal.web.gwt.app.client.permissions.support.AclResourceTokenizer;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
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
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class ProjectPermissionsPresenter extends PresenterWidget<ProjectPermissionsPresenter.Display>
    implements ProjectPermissionsUiHandlers, DeleteAllSubjectPermissionsHandler {

  private final ModalProvider<DeleteAllConfirmationModalPresenter> deleteAllConfirmationModalProvider;

  private ProjectDto project;

  @Inject
  public ProjectPermissionsPresenter(Display display, EventBus eventBus,
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
        if(ActionsColumn.REMOVE_ACTION.equals(actionName)) {
          deletePermission(acl);
        }
      }
    });
  }

  @Override
  public void selectSubject(final Subject subject) {
    String uri = ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_SUBJECT.create()
        .query(ResourcePermissionRequestPaths.TYPE_QUERY_PARAM, subject.getType().getName())
        .build(project.getName(), subject.getPrincipal());

    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(uri) //
        .authorize(getView().getDeleteAllAuthorizer()) //
        .delete().send();

    ResourceRequestBuilderFactory.<JsArray<Acl>>newBuilder().forResource(uri).get()
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

  public void deletePermission(Acl acl) {
    final Subject subject = acl.getSubject();
    String nodeUri = getNodeUri(ResourcePermissionType.getTypeByPermission(acl.getActions(0)), acl.getResource(),
        subject.getPrincipal(), subject.getType().getName());

    ResourceRequestBuilderFactory.<JsArray<Acl>>newBuilder().forResource(nodeUri).delete()
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            selectSubject(subject);
          }
        })//
        .withCallback(Response.SC_FORBIDDEN, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            //ignore
          }
        })//
        .send();
  }

  private String getNodeUri(ResourcePermissionType type, String aclResource, String principal, String typeName) {
    AclResourceTokenizer aclTokenizer = new AclResourceTokenizer(aclResource);

    switch(type) {
      case PROJECT:
        return ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_PROJECT.create()
            .query(ResourcePermissionRequestPaths.PRINCIPAL_QUERY_PARAM, principal)
            .query(ResourcePermissionRequestPaths.TYPE_QUERY_PARAM, typeName)
            .build(aclTokenizer.getToken(AclResourceTokenizer.ResourceTokens.PROJECT));
      case DATASOURCE:
        return ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_DATASOURCE.create()
            .query(ResourcePermissionRequestPaths.PRINCIPAL_QUERY_PARAM, principal)
            .query(ResourcePermissionRequestPaths.TYPE_QUERY_PARAM, typeName)
            .build(aclTokenizer.getToken(AclResourceTokenizer.ResourceTokens.DATASOURCE));
      case VARIABLE:
        return ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_TABLE_VARIABLE.create()
            .query(ResourcePermissionRequestPaths.PRINCIPAL_QUERY_PARAM, principal)
            .query(ResourcePermissionRequestPaths.TYPE_QUERY_PARAM, typeName)
            .build(aclTokenizer.getToken(AclResourceTokenizer.ResourceTokens.PROJECT),
                aclTokenizer.getToken(AclResourceTokenizer.ResourceTokens.TABLE),
                aclTokenizer.getToken(AclResourceTokenizer.ResourceTokens.VARIABLE));
      case TABLE:
        return ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_TABLE.create()
            .query(ResourcePermissionRequestPaths.PRINCIPAL_QUERY_PARAM, principal)
            .query(ResourcePermissionRequestPaths.TYPE_QUERY_PARAM, typeName)
            .build(aclTokenizer.getToken(AclResourceTokenizer.ResourceTokens.PROJECT),
                aclTokenizer.getToken(AclResourceTokenizer.ResourceTokens.TABLE));
      case REPORT_TEMPLATE:
        return ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_REPORTTEMPLATE.create()
            .query(ResourcePermissionRequestPaths.PRINCIPAL_QUERY_PARAM, principal)
            .query(ResourcePermissionRequestPaths.TYPE_QUERY_PARAM, typeName)
            .build(aclTokenizer.getToken(AclResourceTokenizer.ResourceTokens.PROJECT),
                aclTokenizer.getToken(AclResourceTokenizer.ResourceTokens.REPORTTEMPLATE));
      default:
        return null;
    }
  }

  @Override
  public void deleteAllPermissions(Subject subject) {
    deleteAllConfirmationModalProvider.get().initialize(subject, this);
  }

  private void retrievePermissions() {
    ResourceRequestBuilderFactory.<JsArray<Subject>>newBuilder().forResource(
        ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_SUBJECTS.create().build(project.getName())).get()
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
    String uri = ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_SUBJECT.create()
        .query(ResourcePermissionRequestPaths.TYPE_QUERY_PARAM, subject.getType().getName())
        .build(project.getName(), subject.getPrincipal());
    ResourceRequestBuilderFactory.<JsArray<Acl>>newBuilder().forResource(uri).delete()
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            selectSubject(subject);
          }
        }).send();
  }

  public interface Display extends View, HasUiHandlers<ProjectPermissionsUiHandlers> {
    void setData(@Nonnull List<Subject> subjects);

    void setSubjectData(Subject subject, List<Acl> subjectAcls);

    void initializeTable(NodeToPlaceMapper nodeToPlaceMapper, NodeNameFormatter formatter,
        NodeToTypeMapper nodeToTypeMapper, Comparator<Acl> resourceTypeComparator);

    HasActionHandler<Acl> getActions();

    HasAuthorization getDeleteAllAuthorizer();
  }

  public interface NodeToPlaceMapper {
    PlaceRequest map(Acl acl);
  }

  private static final class NodeToPlaceConverterImpl implements NodeToPlaceMapper {

    @Override
    public PlaceRequest map(Acl acl) {
      return getPlaceRequest(ResourcePermissionType.getTypeByPermission(acl.getActions(0)),
          new AclResourceTokenizer(acl.getResource()));
    }

    private PlaceRequest getPlaceRequest(ResourcePermissionType type, AclResourceTokenizer tokenizer) {
      PlaceRequest placeRequest = null;
      switch(type) {
        case PROJECT:
          placeRequest = ProjectPlacesHelper
              .getProjectPlace(tokenizer.getToken(AclResourceTokenizer.ResourceTokens.PROJECT));
          break;
        case DATASOURCE:
          placeRequest = ProjectPlacesHelper
              .getDatasourcePlace(tokenizer.getToken(AclResourceTokenizer.ResourceTokens.DATASOURCE));
          break;
        case TABLE:
          placeRequest = ProjectPlacesHelper
              .getTablePlace(tokenizer.getToken(AclResourceTokenizer.ResourceTokens.PROJECT),
                  tokenizer.getToken(AclResourceTokenizer.ResourceTokens.TABLE));
          break;
        case VARIABLE:
          placeRequest = ProjectPlacesHelper
              .getVariablePlace(tokenizer.getToken(AclResourceTokenizer.ResourceTokens.PROJECT),
                  tokenizer.getToken(AclResourceTokenizer.ResourceTokens.TABLE),
                  tokenizer.getToken(AclResourceTokenizer.ResourceTokens.VARIABLE));
          break;
        case REPORT_TEMPLATE:
          placeRequest = ProjectPlacesHelper
              .getReportsPlace(tokenizer.getToken(AclResourceTokenizer.ResourceTokens.PROJECT));
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
      return getName(ResourcePermissionType.getTypeByPermission(acl.getActions(0)),
          new AclResourceTokenizer(acl.getResource()));
    }

    private String getName(ResourcePermissionType type, AclResourceTokenizer tokenizer) {
      String name = null;
      switch(type) {
        case PROJECT:
          name = tokenizer.getToken(AclResourceTokenizer.ResourceTokens.PROJECT);
          break;

        case DATASOURCE:
          name = translations.allTablesLabel();
          break;

        case TABLE:
          name = tokenizer.getToken(AclResourceTokenizer.ResourceTokens.TABLE);
          break;

        case VARIABLE:
          name = tokenizer.getToken(AclResourceTokenizer.ResourceTokens.TABLE) + ":" +
              tokenizer.getToken(AclResourceTokenizer.ResourceTokens.VARIABLE);
          break;

        case REPORT_TEMPLATE:
          name = tokenizer.getToken(AclResourceTokenizer.ResourceTokens.REPORTTEMPLATE);
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
