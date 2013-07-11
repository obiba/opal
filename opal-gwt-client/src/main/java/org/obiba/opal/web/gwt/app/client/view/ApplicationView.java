/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.ui.HasUrl;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class ApplicationView implements ApplicationPresenter.Display {

  @UiTemplate("ApplicationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Panel, ApplicationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Panel dock;

  @UiField
  NavLink help;

  @UiField
  NavLink quit;

  @UiField
  NavLink administrationItem;

  @UiField
  NavLink username;

  @UiField
  Label version;

  @UiField
  NavLink dashboardItem;

  @UiField
  NavLink datasourcesItem;

  @UiField
  Panel workbench;

  @UiField
  Frame frame;

  MenuItem currentSelection;

  public ApplicationView() {
    dock = uiBinder.createAndBindUi(this);
  }

  @Override
  public void addToSlot(Object slot, IsWidget content) {
  }

  @Override
  public void removeFromSlot(Object slot, IsWidget content) {
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if (ApplicationPresenter.WORKBENCH == slot) {
      workbench.clear();
      workbench.add(content.asWidget());
    }
  }

  @Override
  public NavLink getDashboardItem() {
    return dashboardItem;
  }

  @Override
  public HasUrl getDownloder() {
    return new HasUrl() {

      @Override
      public void setUrl(String url) {
        frame.setUrl(url);
      }
    };
  }

  @Override
  public HasClickHandlers getQuit() {
    return quit;
  }

  @Override
  public HasClickHandlers getHelp() {
    return help;
  }

  @Override
  public Widget asWidget() {
    return dock;
  }

  @Override
  public void setCurrentSelection(MenuItem selection) {
    if(currentSelection != null) {
      currentSelection.removeStyleName("selected");
    }
    if(selection != null) {
      selection.addStyleName("selected");
    }
    currentSelection = selection;
  }

  @Override
  public void clearSelection() {
    setCurrentSelection(null);
  }

  @Override
  public NavLink getDatasourcesItem() {
    return datasourcesItem;
  }

  @Override
  public NavLink getAdministrationItem() {
    return administrationItem;
  }

  @Override
  public HasAuthorization getAdministrationAuthorizer() {
    return new UIObjectAuthorizer(administrationItem) {
      @Override
      public void authorized() {
        super.authorized();
      }

      @Override
      public void unauthorized() {
        super.unauthorized();
      }
    };
  }

  @Override
  public void setUsername(String username) {
    this.username.setText(username);
  }

  @Override
  public void setVersion(String version) {
    this.version.setText(version);
  }

}
