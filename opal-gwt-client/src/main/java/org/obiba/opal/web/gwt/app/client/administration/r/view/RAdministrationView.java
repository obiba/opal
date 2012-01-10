/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.r.view;

import org.obiba.opal.web.gwt.app.client.administration.r.presenter.RAdministrationPresenter;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class RAdministrationView extends ViewImpl implements RAdministrationPresenter.Display {

  @UiTemplate("RAdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, RAdministrationView> {
  }

  //
  // Constants
  //

  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  @UiField
  Button rTestButton;

  @UiField
  Panel permissionsPanel;

  @UiField
  Panel permissions;

  //
  // Constructors
  //

  public RAdministrationView() {
    super();
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    if(slot == RAdministrationPresenter.PermissionSlot) {
      permissions.clear();
      permissions.add(content);
    }
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public HandlerRegistration addTestRServerHandler(ClickHandler handler) {
    return rTestButton.addClickHandler(handler);
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new WidgetAuthorizer(permissionsPanel);
  }

}
