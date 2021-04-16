/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.plugins;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;
import org.obiba.opal.web.model.client.opal.PluginPackagesDto;

public class PluginsAdministrationView extends ViewWithUiHandlers<PluginsAdministrationUiHandlers> implements PluginsAdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, PluginsAdministrationView> {
  }

  private final Translations translations;

  @UiField
  HasWidgets breadcrumbs;

  @UiField
  Alert restartNotice;

  @UiField
  TabPanel tabPanel;

  @UiField
  PluginPackageTable installedTable;

  @UiField
  PluginPackageTable updatesTable;

  @UiField
  PluginPackageTable availableTable;

  @UiField
  Label lastUpdate;

  @UiField
  Anchor updateSite;

  private ListDataProvider<PluginPackageDto> installedPackageProvider = new ListDataProvider<>();
  private ListDataProvider<PluginPackageDto> updatesPackageProvider = new ListDataProvider<>();
  private ListDataProvider<PluginPackageDto> availablePackageProvider = new ListDataProvider<>();

  @Inject
  public PluginsAdministrationView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    updateSite.setTarget("_blank");
    installedTable.initInstalledPackagesColumns(new ActionHandler<PluginPackageDto>() {
      @Override
      public void doAction(PluginPackageDto object, String actionName) {
        if (ActionsColumn.REMOVE_ACTION.equals(actionName)) getUiHandlers().onUninstall(object.getName());
        else if (PluginPackageTable.RESTART_ACTION.equals(actionName)) getUiHandlers().onRestart(object.getName());
        else if (PluginPackageTable.CONFIGURE_ACTION.equals(actionName)) getUiHandlers().onConfigure(object.getName());
        else getUiHandlers().onCancelUninstall(object.getName());
      }
    });
    updatesTable.initInstallablePackagesColumns(new ActionHandler<PluginPackageDto>() {
      @Override
      public void doAction(PluginPackageDto object, String actionName) {
        getUiHandlers().onInstall(object.getName(), object.getVersion());
      }
    });
    availableTable.initInstallablePackagesColumns(new ActionHandler<PluginPackageDto>() {
      @Override
      public void doAction(PluginPackageDto object, String actionName) {
        getUiHandlers().onInstall(object.getName(), object.getVersion());
      }
    });
    installedPackageProvider.addDataDisplay(installedTable);
    updatesPackageProvider.addDataDisplay(updatesTable);
    availablePackageProvider.addDataDisplay(availableTable);
    tabPanel.addShownHandler(new TabPanel.ShownEvent.Handler() {
      @Override
      public void onShow(TabPanel.ShownEvent shownEvent) {
        refresh();
      }
    });
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("selectPluginArchive")
  public void onPluginArchiveSelection(ClickEvent event) {
    getUiHandlers().onPluginFileSelection();
  }

  @Override
  public void showInstalledPackages(PluginPackagesDto pluginPackagesDto) {
    installedPackageProvider.setList(JsArrays.toList(pluginPackagesDto.getPackagesArray()));
    installedPackageProvider.refresh();
    refreshPackagesInfo(pluginPackagesDto);
  }

  @Override
  public void showAvailablePackages(PluginPackagesDto pluginPackagesDto) {
    availablePackageProvider.setList(JsArrays.toList(pluginPackagesDto.getPackagesArray()));
    availablePackageProvider.refresh();
    refreshPackagesInfo(pluginPackagesDto);
  }

  @Override
  public void showUpdatablePackages(PluginPackagesDto pluginPackagesDto) {
    updatesPackageProvider.setList(JsArrays.toList(pluginPackagesDto.getPackagesArray()));
    updatesPackageProvider.refresh();
    refreshPackagesInfo(pluginPackagesDto);
  }

  @Override
  public void refresh() {
    if (tabPanel.getSelectedTab() == 0) getUiHandlers().getInstalledPlugins();
    if (tabPanel.getSelectedTab() == 1) getUiHandlers().getUpdatablePlugins();
    if (tabPanel.getSelectedTab() == 2) getUiHandlers().getAvailablePlugins();
  }

  private void refreshPackagesInfo(PluginPackagesDto pluginPackagesDto) {
    restartNotice.setVisible(pluginPackagesDto.getRestart());
    updateSite.setText(pluginPackagesDto.getSite());
    String pluginsIndex = pluginPackagesDto.getSite();
    if (!pluginsIndex.endsWith("/")) pluginsIndex = pluginsIndex + "/";
    pluginsIndex = pluginsIndex + "plugins.json";
    updateSite.setHref(pluginsIndex);
    if (pluginPackagesDto.hasUpdated())
      lastUpdate.setText(translations.userMessageMap().get("LastUpdate").replace("{0}", Moment.create(pluginPackagesDto.getUpdated()).fromNow()));
    else
      lastUpdate.setText("");
  }
}
