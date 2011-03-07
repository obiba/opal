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
import org.obiba.opal.web.model.client.opal.Acls;

import com.google.gwt.core.client.JsArray;
import com.google.inject.Inject;

/**
 *
 */
public class AuthorizationPresenter extends WidgetPresenter<AuthorizationPresenter.Display> {

  private SubjectPermissionsRequest subjectPermissionsRequests;

  private JsArray<Acls> subjectPermissions;

  //
  // Constructors
  //

  @Inject
  public AuthorizationPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  public void setAclRequest(AclRequest.Builder... builders) {
    subjectPermissionsRequests = new SubjectPermissionsRequest(builders);

    for(String header : subjectPermissionsRequests.getHeaders()) {
      getDisplay().initColumn(header, new PermissionHandlerImpl());
    }

    subjectPermissionsRequests.setAclCallback(new AclCallbackImpl(eventBus));
  }

  private void addEventHandlers() {
    if(getDisplay().getActionsColumn() != null) {
      getDisplay().getActionsColumn().setActionHandler(new ActionHandler<Acls>() {
        public void doAction(Acls perms, String actionName) {
          if(actionName != null && actionName.equals(DELETE_ACTION)) {
            subjectPermissionsRequests.delete(perms.getName());
          }
        }
      });
    }
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
    for(Acls perms : JsArrays.toIterable(subjectPermissions)) {
      if(perms.getName().equals(subject)) return true;
    }
    return false;
  }

  //
  // WidgetPresenter methods
  //

  @Override
  public void refreshDisplay() {
    subjectPermissionsRequests.get();
  }

  @Override
  public void revealDisplay() {
    subjectPermissionsRequests.get();
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
    public void onDelete(String subject) {
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
    public void unauthorize(String subject, String header) {
      subjectPermissionsRequests.unauthorize(subject, header);
    }

    @Override
    public Boolean hasPermission(String header, Acls acls) {
      return subjectPermissionsRequests.hasPermission(header, acls);
    }

    @Override
    public void authorize(String subject, String header) {
      subjectPermissionsRequests.authorize(subject, header);
    }
  }

  public interface Display extends WidgetDisplay {

    void renderPermissions(JsArray<Acls> subjectPermissions);

    void initColumn(String header, PermissionSelectionHandler permHandler);

    void clear();

    HasActionHandler<Acls> getActionsColumn();

    String getPrincipal();

    void addHandler(AddPrincipalHandler handler);

  }

  public interface AddPrincipalHandler {
    void onAdd(String principal);
  }

  public interface PermissionSelectionHandler {

    public Boolean hasPermission(String header, Acls acls);

    public void authorize(String subject, String header);

    public void unauthorize(String subject, String header);

  }

}
