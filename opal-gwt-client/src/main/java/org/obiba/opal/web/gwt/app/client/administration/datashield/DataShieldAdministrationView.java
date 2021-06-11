/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield;

import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.datashield.DataShieldProfileDto;

public class DataShieldAdministrationView extends ViewWithUiHandlers<DataShieldAdministrationUiHandlers> implements DataShieldAdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, DataShieldAdministrationView> {
  }

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Image clusterLoading;

  @UiField
  TabPanel clusterTabs;

  @UiField
  Image profileLoading;

  @UiField
  Panel profilesPanel;

  @UiField
  TabPanel profileTabs;

  @UiField
  Panel permissionsPanel;

  @UiField
  Panel permissions;

  @UiField
  Panel breadcrumbs;

  private int profileTabsCount;

  @Inject
  public DataShieldAdministrationView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void addToSlot(Object slot, IsWidget content) {
    if (slot instanceof DataShieldAdministrationPresenter.PackagesSlot) {
      Tab tab = new Tab();
      tab.setHeading(slot.toString());
      tab.add(content.asWidget());
      clusterTabs.add(tab);
      clusterTabs.selectTab(0);
      clusterTabs.setVisible(true);
      clusterLoading.setVisible(false);
    } else if (slot instanceof DataShieldAdministrationPresenter.ProfilesSlot) {
      Tab tab = new Tab();
      tab.setHeading(slot.toString());
      DataShieldAdministrationPresenter.ProfilesSlot pSlot = (DataShieldAdministrationPresenter.ProfilesSlot) slot;
      DataShieldProfileDto profile = pSlot.getProfile();
      if (!pSlot.hasCluster())
        tab.setIcon(IconType.BAN_CIRCLE);
      else if (profile.getName().equals(profile.getCluster()))
        tab.setIcon(IconType.PUSHPIN);
      tab.add(content.asWidget());
      profileTabs.add(tab);
      profileTabsCount++;
      if (pSlot.isSelected()) {
        profileTabs.selectTab(profileTabsCount - 1);
      } else if (profileTabsCount == 1)
        profileTabs.selectTab(0);
      profilesPanel.setVisible(true);
      profileLoading.setVisible(false);
    }
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if (slot == DataShieldAdministrationPresenter.PermissionSlot) {
      permissions.clear();
      permissions.add(content);
    }
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new WidgetAuthorizer(permissionsPanel);
  }

  @Override
  public void clearClusters() {
    clusterTabs.clear();
    clusterTabs.setVisible(false);
    clusterLoading.setVisible(true);
    clearProfiles();
  }

  @Override
  public void clearProfiles() {
    profileTabs.clear();
    profilesPanel.setVisible(false);
    profileLoading.setVisible(true);
    profileTabsCount = 0;
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("profileAdd")
  void onAddProfile(ClickEvent event) {
    getUiHandlers().onAddProfile();
  }


  @UiHandler("downloadLogs")
  void onDownloadLogs(ClickEvent event) {
    getUiHandlers().onDownloadLogs();
  }


}
