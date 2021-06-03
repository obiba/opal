/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.r;

import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.NavPillsPanel;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;

/**
 *
 */
public class RAdministrationView extends ViewWithUiHandlers<RAdministrationUiHandlers>
    implements RAdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, RAdministrationView> {
  }

  @UiField
  TabPanel clusterTabs;

  @UiField
  Panel permissionsPanel;

  @UiField
  Panel rSessions;

  @UiField
  Panel rWorkspaces;

  @UiField
  Panel permissions;

  @UiField
  Panel breadcrumbs;

  //
  // Constructors
  //

  @Inject
  public RAdministrationView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void clearClusters() {
    clusterTabs.clear();
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if (slot == Slots.RSessions) {
      rSessions.clear();
      rSessions.add(content);
    } else if (slot == Slots.RWorkspaces) {
      rWorkspaces.clear();
      rWorkspaces.add(content);
    } else if (slot == Slots.Permissions) {
      permissions.clear();
      permissions.add(content);
    } else {
      Tab tab = new Tab();
      tab.setHeading(slot.toString());
      tab.add(content.asWidget());
      clusterTabs.add(tab);
      clusterTabs.selectTab(0);
    }
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new WidgetAuthorizer(permissionsPanel);
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

}
