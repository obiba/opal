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

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.opal.Acl;

import com.google.gwt.core.client.JsArray;
import com.google.inject.Inject;

/**
 *
 */
public class AuthorizationPresenter extends WidgetPresenter<AuthorizationPresenter.Display> {

  private SubjectPermissionsRequest subjectPermissionsRequests;

  private List<SubjectPermissions> subjectPermissions;

  //
  // Constructors
  //

  @Inject
  public AuthorizationPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  public void addAclRequest(AclRequest.Builder... builders) {
    subjectPermissionsRequests = new SubjectPermissionsRequest(builders);

    DefaultAclCallback aclCallback = new DefaultAclCallback(eventBus) {

      @Override
      public void onGet(JsArray<Acl> resource) {
        subjectPermissions = new ArrayList<SubjectPermissions>();
        for(Acl acl : JsArrays.toList(JsArrays.toSafeArray(resource))) {
          SubjectPermissions perms = new SubjectPermissions(acl.getPrincipal());
          perms.addPermission(subjectPermissionsRequests.getMainAclRequest().getName());
          subjectPermissions.add(perms);
        }
        getDisplay().renderPermissions(subjectPermissionsRequests.getNames(), subjectPermissions);
      }

      @Override
      public void onDelete(String subject) {
        refreshDisplay();
      }

      @Override
      public void onAdd(Acl resource) {
        refreshDisplay();
      }
    };

    subjectPermissionsRequests.getMainAclRequest().setAclGetCallback(aclCallback);
    subjectPermissionsRequests.getMainAclRequest().setAclAddCallback(aclCallback);
    subjectPermissionsRequests.getMainAclRequest().setAclDeleteCallback(aclCallback);
  }

  private void addEventHandlers() {
    getDisplay().getActionsColumn().setActionHandler(new ActionHandler<SubjectPermissions>() {
      public void doAction(SubjectPermissions perms, String actionName) {
        if(actionName != null && actionName.equals(DELETE_ACTION)) {
          // TODO delete others
          subjectPermissionsRequests.getMainAclRequest().delete(perms.getSubject());
        }
      }
    });
    getDisplay().addHandler(new AddPrincipalHandler() {

      @Override
      public void onAdd(String subject) {
        if(subject.trim().length() > 0) {
          if(!hasSubject(subject)) {
            subjectPermissionsRequests.getMainAclRequest().add(subject.trim());
          }
          getDisplay().clear();
        }
      }
    });
  }

  private boolean hasSubject(String subject) {
    for(SubjectPermissions perms : subjectPermissions) {
      if(perms.getSubject().equals(subject)) return true;
    }
    return false;
  }

  //
  // WidgetPresenter methods
  //

  @Override
  public void refreshDisplay() {
    subjectPermissionsRequests.getMainAclRequest().get();
  }

  @Override
  public void revealDisplay() {
    subjectPermissionsRequests.getMainAclRequest().get();
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

  public interface Display extends WidgetDisplay {

    void renderPermissions(Iterable<String> names, List<SubjectPermissions> subjectPermissions);

    void clear();

    HasActionHandler<SubjectPermissions> getActionsColumn();

    String getPrincipal();

    void addHandler(AddPrincipalHandler handler);

  }

  public interface AddPrincipalHandler {
    void onAdd(String principal);
  }

}
