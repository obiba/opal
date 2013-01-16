/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.authz.presenter;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest.AclGetCallback;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.opal.Acl;
import org.obiba.opal.web.model.client.opal.Acls;
import org.obiba.opal.web.model.client.opal.Subject;
import org.obiba.opal.web.model.client.opal.Subject.SubjectType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class SubjectAuthorizationPresenter extends PresenterWidget<SubjectAuthorizationPresenter.Display> {

  private SubjectPermissionsRequest subjectPermissionsRequests;

  private JsArray<Acls> subjects;

  private JsArray<Acls> subjectPermissions;

  private boolean initialized = false;

  private SubjectType type;

  @Inject
  public SubjectAuthorizationPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  public void setAclRequest(SubjectType type, AclRequest... requests) {
    this.type = type;
    this.subjectPermissionsRequests = new SubjectPermissionsRequest(type, new AclCallbackImpl(getEventBus()), requests);

    if(!initialized) {
      for(String header : subjectPermissionsRequests.getHeaders()) {
        getView().initColumn(header, new PermissionHandlerImpl());
      }
      initialized = true;
    }
    getView().renderSubjectType(type.toString());
  }

  @Override
  public void onReveal() {
    if(initialized) {
      subjectPermissionsRequests.getSubjects(new SubjectSuggestionsCallback());
    }
  }

  @Override
  protected void onBind() {
    getView().addPrincipalHandler(new AddPrincipalHandlerImpl());
  }

  private boolean hasSubject(String subject) {
    for(Acls perms : JsArrays.toIterable(subjectPermissions)) {
      if(perms.getSubject().getPrincipal().equals(subject)) return true;
    }
    return false;
  }

  /**
   *
   */
  private final class AddPrincipalHandlerImpl implements AddPrincipalHandler {

    @Override
    public void onAdd(String subject) {
      if(subject.trim().length() > 0) {
        if(!hasSubject(subject)) {
          Subject s = Subject.create();
          s.setPrincipal(subject.trim());
          s.setType(type);
          subjectPermissionsRequests.add(s);
        }
        getView().clear();
      }
    }
  }

  /**
   *
   */
  private final class SubjectSuggestionsCallback implements AclGetCallback {
    @SuppressWarnings("unchecked")
    @Override
    public void onGetFailed(Response response) {
      subjects = (JsArray<Acls>) JsArray.createArray();
      getSubjectPermissions();
    }

    @Override
    public void onGet(JsArray<Acls> resource) {
      subjects = JsArrays.toSafeArray(resource);
      getSubjectPermissions();
    }

    private void getSubjectPermissions() {
      getView().renderSubjectSuggestions(subjects);
      subjectPermissionsRequests.get();
    }
  }

  private final class AclCallbackImpl extends DefaultAclCallback {

    private AclCallbackImpl(EventBus eventBus) {
      super(eventBus);
    }

    @Override
    public void onGet(JsArray<Acls> resource) {
      subjectPermissions = JsArrays.create();
      JsArrays.pushAll(subjectPermissions,subjects);
      for (Acls acls : JsArrays.toIterable(resource)) {
         for (int i=0; i<subjectPermissions.length(); i++) {
           if (subjectPermissions.get(i).getSubject().getPrincipal().equals(acls.getSubject().getPrincipal())) {
             subjectPermissions.set(i,acls);
             break;
           }
         }
      }
      getView().renderPermissions(subjectPermissions);
    }

    @Override
    public void onDelete(Subject subject) {
      onReveal();
    }

    @Override
    public void onAdd(Acl resource) {
      onReveal();
    }
  }

  /**
   *
   */
  private final class PermissionHandlerImpl implements PermissionSelectionHandler {
    @Override
    public void unauthorize(Subject subject, String header) {
      subjectPermissionsRequests.unauthorize(subject, header);
    }

    @Override
    public Boolean hasPermission(String header, Acls acls) {
      return subjectPermissionsRequests.hasPermission(header, acls);
    }

    @Override
    public void authorize(Subject subject, String header) {
      subjectPermissionsRequests.authorize(subject, header);
    }
  }

  public interface Display extends View {

    void renderSubjectType(String type);

    void renderPermissions(JsArray<Acls> subjectPermissions);

    void initColumn(String header, PermissionSelectionHandler permHandler);

    void clear();

    String getPrincipal();

    void addPrincipalHandler(AddPrincipalHandler handler);

    void renderSubjectSuggestions(JsArray<Acls> subjects);

  }

  public interface AddPrincipalHandler {
    void onAdd(String principal);
  }

  public interface PermissionSelectionHandler {

    public Boolean hasPermission(String header, Acls acls);

    public void authorize(Subject subject, String header);

    public void unauthorize(Subject subject, String header);

  }

}
