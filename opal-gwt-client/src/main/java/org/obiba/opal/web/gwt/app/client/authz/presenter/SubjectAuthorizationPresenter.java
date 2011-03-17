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

import static org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn.DELETE_ACTION;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest.AclGetCallback;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.opal.Acl;
import org.obiba.opal.web.model.client.opal.Acls;
import org.obiba.opal.web.model.client.opal.Subject;
import org.obiba.opal.web.model.client.opal.Subject.SubjectType;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

/**
 *
 */
public class SubjectAuthorizationPresenter extends WidgetPresenter<SubjectAuthorizationPresenter.Display> {

  private SubjectPermissionsRequest subjectPermissionsRequests;

  private JsArray<Acls> subjectPermissions;

  private boolean initialized = false;

  private SubjectType type;

  //
  // Constructors
  //

  @Inject
  public SubjectAuthorizationPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  public void setAclRequest(SubjectType type, AclRequest.Builder... builders) {
    this.type = type;
    this.subjectPermissionsRequests = new SubjectPermissionsRequest(type, new AclCallbackImpl(eventBus), builders);

    if(!initialized) {
      for(String header : subjectPermissionsRequests.getHeaders()) {
        getDisplay().initColumn(header, new PermissionHandlerImpl());
      }
      initialized = true;
    }
    getDisplay().renderSubjectType(type.toString());
  }

  private void addEventHandlers() {
    if(getDisplay().getActionsColumn() != null) {
      getDisplay().getActionsColumn().setActionHandler(new ActionHandler<Acls>() {
        public void doAction(Acls perms, String actionName) {
          if(actionName != null && actionName.equals(DELETE_ACTION)) {
            subjectPermissionsRequests.delete(perms.getSubject());
          }
        }
      });
    }
    getDisplay().addPrincipalHandler(new AddPrincipalHandlerImpl());
  }

  private boolean hasSubject(String subject) {
    for(Acls perms : JsArrays.toIterable(subjectPermissions)) {
      if(perms.getSubject().getPrincipal().equals(subject)) return true;
    }
    return false;
  }

  //
  // WidgetPresenter methods
  //

  @Override
  public void refreshDisplay() {
    subjectPermissionsRequests.get();
    subjectPermissionsRequests.getSubjects(new SubjectSuggestionsCallback());
  }

  @Override
  public void revealDisplay() {
    subjectPermissionsRequests.get();
    subjectPermissionsRequests.getSubjects(new SubjectSuggestionsCallback());
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Interface and inner classes
  //

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
        getDisplay().clear();
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
      getDisplay().renderSubjectSuggestions((JsArray<Acls>) JsArray.createArray());
    }

    @Override
    public void onGet(JsArray<Acls> resource) {
      getDisplay().renderSubjectSuggestions(resource);
    }
  }

  private final class AclCallbackImpl extends DefaultAclCallback {

    private AclCallbackImpl(EventBus eventBus) {
      super(eventBus);
    }

    @Override
    public void onGet(JsArray<Acls> resource) {
      subjectPermissions = resource;
      getDisplay().renderPermissions(subjectPermissions);
    }

    @Override
    public void onDelete(Subject subject) {
      refreshDisplay();
    }

    @Override
    public void onAdd(Acl resource) {
      refreshDisplay();
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

  public interface Display extends WidgetDisplay {

    void renderSubjectType(String type);

    void renderPermissions(JsArray<Acls> subjectPermissions);

    void initColumn(String header, PermissionSelectionHandler permHandler);

    void clear();

    HasActionHandler<Acls> getActionsColumn();

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
