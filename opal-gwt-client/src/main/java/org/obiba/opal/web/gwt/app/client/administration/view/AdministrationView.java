/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.view;

import java.util.Iterator;

import com.github.gwtbootstrap.client.ui.Alert;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Anchor;
import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.ui.OpalNavLink;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;

import com.github.gwtbootstrap.client.ui.NavList;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class AdministrationView extends ViewImpl implements AdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, AdministrationView> {}

  @UiField
  Alert noIDDatabasePanel;

  @UiField
  Anchor addIDDatabase;

  @UiField
  Alert noDataDatabasePanel;

  @UiField
  Alert noRServerPanel;

  @UiField
  Anchor adminRServer;

  @UiField
  Alert noResourceProvidersPanel;

  @UiField
  Anchor adminRPackages;

  @UiField
  Anchor addDataDatabase;

  @UiField
  OpalNavLink usersGroupsPlace;

  @UiField
  OpalNavLink profilesPlace;

  @UiField
  OpalNavLink idProvidersPlace;

  @UiField
  OpalNavLink identifiersPlace;

  @UiField
  OpalNavLink databasesPlace;

  @UiField
  OpalNavLink searchPlace;

  @UiField
  OpalNavLink rPlace;

  @UiField
  OpalNavLink dataShieldPlace;

  @UiField
  OpalNavLink reportsPlace;

  @UiField
  OpalNavLink filesPlace;

  @UiField
  OpalNavLink tasksPlace;

  @UiField
  OpalNavLink javaPlace;

  @UiField
  OpalNavLink pluginsPlace;

  @UiField
  OpalNavLink appsPlace;

  @UiField
  OpalNavLink serverPlace;

  @UiField
  OpalNavLink taxonomiesPlace;

  @UiField
  NavList dataAccess;

  @UiField
  FlowPanel identifiersAuthorizable;

  @UiField
  NavList dataAnalysis;

  @UiField
  NavList system;

  @UiField
  FlowPanel usersGroupsAuthorizable;

  @UiField
  FlowPanel profilesAuthorizable;

  @UiField
  FlowPanel idProvidersAuthorizable;

  @UiField
  FlowPanel rAuthorizable;

  @UiField
  FlowPanel dataShieldAuthorizable;

  @UiField
  FlowPanel searchAuthorizable;

  @UiField
  FlowPanel databasesAuthorizable;

  @UiField
  FlowPanel jvmAuthorizable;

  @UiField
  FlowPanel pluginsAuthorizable;

  @UiField
  FlowPanel appsAuthorizable;

  @UiField
  FlowPanel reportsAuthorizable;

  @UiField
  FlowPanel tasksAuthorizable;

  @UiField
  FlowPanel generalSettingsAuthorizable;

  @Inject
  public AdministrationView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
  }

  @Override
  public HasAuthorization getGeneralSettingsAuthorizer() {
    return new WidgetAuthorizer(generalSettingsAuthorizable);
  }

  @Override
  public HasAuthorization getTasksAuthorizer() {
    return new WidgetAuthorizer(tasksAuthorizable);
  }

  @Override
  public HasAuthorization getReportsAuthorizer() {
    return new WidgetAuthorizer(reportsAuthorizable);
  }

  @Override
  public HasAuthorization getJVMAuthorizer() {
    return new WidgetAuthorizer(jvmAuthorizable);
  }

  @Override
  public HasAuthorization getPluginsAuthorizer() {
    return new WidgetAuthorizer(pluginsAuthorizable);
  }

  @Override
  public HasAuthorization getAppsAuthorizer() {
    return new WidgetAuthorizer(appsAuthorizable);
  }

  @Override
  public HasAuthorization getDatabasesAuthorizer() {
    return new WidgetAuthorizer(databasesAuthorizable);
  }

  @Override
  public HasAuthorization getSearchAuthorizer() {
    return new WidgetAuthorizer(searchAuthorizable);
  }

  @Override
  public HasAuthorization getDataShieldAuthorizer() {
    return new WidgetAuthorizer(dataShieldAuthorizable);
  }

  @Override
  public HasAuthorization getRAuthorizer() {
    return new WidgetAuthorizer(rAuthorizable);
  }

  @Override
  public HasAuthorization getProfilesAuthorizer() {
    return new WidgetAuthorizer(profilesAuthorizable);
  }

  @Override
  public HasAuthorization getIDProvidersAuthorizer() {
    return new WidgetAuthorizer(idProvidersAuthorizable);
  }

  @Override
  public HasAuthorization getUsersGroupsAuthorizer() {
    return new WidgetAuthorizer(usersGroupsAuthorizable);
  }

  @Override
  public HasAuthorization getIdentifiersAuthorizer() {
    return new CompositeAuthorizer(new WidgetAuthorizer(identifiersAuthorizable), new HasAuthorization() {
      @Override
      public void beforeAuthorization() {
        noIDDatabasePanel.setVisible(false);
      }

      @Override
      public void authorized() {
        noIDDatabasePanel.setVisible(false);
      }

      @Override
      public void unauthorized() {
        noIDDatabasePanel.setVisible(true);
      }
    });
  }

  @Override
  public void showDataDatabasesAlert(boolean visible) {
    noDataDatabasePanel.setVisible(visible);
  }

  @Override
  public void showResourceProvidersAlert(boolean visible) {
    noResourceProvidersPanel.setVisible(visible);
  }

  @Override
  public void showRServerAlert(boolean visible) {
    noRServerPanel.setVisible(visible);
  }

  @Override
  public void setUsersGroupsHistoryToken(String historyToken) {
    usersGroupsPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setProfilesHistoryToken(String historyToken) {
    profilesPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setIDProvidersHistoryToken(String historyToken) {
    idProvidersPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setIdentifiersMappingsHistoryToken(String historyToken) {
    identifiersPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setDatabasesHistoryToken(final String historyToken) {
    databasesPlace.setHistoryToken(historyToken);
    addIDDatabase.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        History.newItem(historyToken);
      }
    });
    addDataDatabase.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        History.newItem(historyToken);
      }
    });
  }

  @Override
  public void setIndexHistoryToken(String historyToken) {
    searchPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setRHistoryToken(final String historyToken) {
    rPlace.setHistoryToken(historyToken);
    adminRServer.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        History.newItem(historyToken);
      }
    });
    adminRPackages.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        History.newItem(historyToken);
      }
    });
  }

  @Override
  public void setDataShieldHistoryToken(String historyToken) {
    dataShieldPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setReportsHistoryToken(String historyToken) {
    reportsPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setFilesHistoryToken(String historyToken) {
    filesPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setTasksHistoryToken(String historyToken) {
    tasksPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setJavaHistoryToken(String historyToken) {
    javaPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setPluginsHistoryToken(String historyToken) {
    pluginsPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setAppsHistoryToken(String historyToken) {
    appsPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setServerHistoryToken(String historyToken) {
    serverPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setTaxonomiesHistoryToken(String historyToken) {
    taxonomiesPlace.setHistoryToken(historyToken);
  }

  @Override
  public void postAutorizationUpdate() {
    dataAnalysis.setVisible(hasVisibleChild(dataAnalysis.iterator()));
    dataAccess.setVisible(hasVisibleChild(dataAccess.iterator()));
    system.setVisible(hasVisibleChild(system.iterator()));
  }

  private boolean hasVisibleChild(Iterator<Widget> iterator) {
    while (iterator.hasNext()) {
      Widget w = iterator.next();
      if ((w instanceof FlowPanel) && w.isVisible()) return true;
    }

    return false;
  }

}
