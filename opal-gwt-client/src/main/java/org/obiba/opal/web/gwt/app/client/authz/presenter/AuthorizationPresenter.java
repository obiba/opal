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

import com.google.gwt.core.client.JsArray;
import com.google.inject.Inject;

/**
 *
 */
public class AuthorizationPresenter extends WidgetPresenter<AuthorizationPresenter.Display> {

  private AclRequest acls;

  //
  // Constructors
  //

  @Inject
  public AuthorizationPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  public void setAclRequest(final String header, AclRequest.Builder builder) {
    DefaultAclCallback aclCallback = new DefaultAclCallback(eventBus) {

      @Override
      public void onGet(JsArray<Acl> resource) {
        getDisplay().renderAcls(JsArrays.toSafeArray(resource));
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

    this.acls = builder.build();

    acls.setAclGetCallback(aclCallback);
    acls.setAclAddCallback(aclCallback);
    acls.setAclDeleteCallback(aclCallback);
  }

  private void addEventHandlers() {
    getDisplay().getActionsColumn().setActionHandler(new ActionHandler<Acl>() {
      public void doAction(Acl dto, String actionName) {
        if(actionName != null && actionName.equals(DELETE_ACTION)) {
          acls.delete(dto.getPrincipal());
        }
      }
    });
    getDisplay().addHandler(new AddPrincipalHandler() {

      @Override
      public void onAdd(String principal) {
        if(principal.trim().length() > 0) {
          acls.add(principal.trim());
          getDisplay().clear();
        }
      }
    });
  }

  //
  // WidgetPresenter methods
  //

  @Override
  public void refreshDisplay() {
    acls.get();
  }

  @Override
  public void revealDisplay() {
    acls.get();
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

    void renderAcls(JsArray<Acl> acls);

    void clear();

    HasActionHandler<Acl> getActionsColumn();

    String getPrincipal();

    void addHandler(AddPrincipalHandler handler);

  }

  public interface AddPrincipalHandler {
    void onAdd(String principal);
  }

}
