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

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.opal.Acl;
import org.obiba.opal.web.model.client.opal.SubjectAcls;

import com.google.gwt.core.client.JsArray;
import com.google.inject.Inject;

/**
 *
 */
public class AuthorizationPresenter extends WidgetPresenter<AuthorizationPresenter.Display> {

  private SubjectPermissionsRequest subjectPermissionsRequests;

  private JsArray<SubjectAcls> subjectPermissions;

  //
  // Constructors
  //

  @Inject
  public AuthorizationPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  public void addAclRequest(AclRequest.Builder... builders) {
    subjectPermissionsRequests = new SubjectPermissionsRequest(builders);

    for(String header : subjectPermissionsRequests.getHeaders()) {
      getDisplay().initColumn(header, subjectPermissionsRequests.getResources(header));
    }

    DefaultAclCallback aclCallback = new DefaultAclCallback(eventBus) {

      @Override
      public void onGet(JsArray<SubjectAcls> resource) {
        subjectPermissions = resource;
        getDisplay().renderPermissions(subjectPermissions);
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

    subjectPermissionsRequests.setAclGetCallback(aclCallback);
    subjectPermissionsRequests.setAclAddCallback(aclCallback);
    subjectPermissionsRequests.setAclDeleteCallback(aclCallback);
  }

  private void addEventHandlers() {
    getDisplay().getActionsColumn().setActionHandler(new ActionHandler<SubjectAcls>() {
      public void doAction(SubjectAcls perms, String actionName) {
        if(actionName != null && actionName.equals(DELETE_ACTION)) {
          subjectPermissionsRequests.delete(perms.getPrincipal());
        }
      }
    });
    getDisplay().addHandler(new AddPrincipalHandler() {

      @Override
      public void onAdd(String subject) {
        if(subject.trim().length() > 0) {
          if(!hasSubject(subject)) {
            subjectPermissionsRequests.add(subject.trim());
          }
          getDisplay().clear();
        }
      }
    });
  }

  private boolean hasSubject(String subject) {
    for(SubjectAcls perms : JsArrays.toIterable(subjectPermissions)) {
      if(perms.getPrincipal().equals(subject)) return true;
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

    void renderPermissions(JsArray<SubjectAcls> subjectPermissions);

    void initColumn(String header, Iterable<String> resources);

    void clear();

    HasActionHandler<SubjectAcls> getActionsColumn();

    String getPrincipal();

    void addHandler(AddPrincipalHandler handler);

  }

  public interface AddPrincipalHandler {
    void onAdd(String principal);
  }

}
