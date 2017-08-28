/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.plugins;

import com.github.gwtbootstrap.client.ui.TabPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;
import org.obiba.opal.web.model.client.opal.PluginPackagesDto;

public class PluginsAdministrationView extends ViewWithUiHandlers<PluginsAdministrationUiHandlers> implements PluginsAdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, PluginsAdministrationView> {
  }

  private final Translations translations;

  @UiField
  HasWidgets breadcrumbs;

  @UiField
  TabPanel tabPanel;

  @UiField
  PluginPackageTable installedTable;

  @UiField
  PluginPackageTable updatesTable;

  @UiField
  PluginPackageTable availableTable;

  private ListDataProvider<PluginPackageDto> installedPackageProvider = new ListDataProvider<>();
  private ListDataProvider<PluginPackageDto> updatesPackageProvider = new ListDataProvider<>();
  private ListDataProvider<PluginPackageDto> availablePackageProvider = new ListDataProvider<>();

  @Inject
  public PluginsAdministrationView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
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

  @Override
  public void showInstalledPackages(PluginPackagesDto pluginPackagesDto) {
    installedPackageProvider.setList(JsArrays.toList(pluginPackagesDto.getPackagesArray()));
    installedPackageProvider.refresh();
  }

  @Override
  public void showAvailablePackages(PluginPackagesDto pluginPackagesDto) {
    availablePackageProvider.setList(JsArrays.toList(pluginPackagesDto.getPackagesArray()));
    availablePackageProvider.refresh();
  }

  @Override
  public void showUpdatablePackages(PluginPackagesDto pluginPackagesDto) {
    updatesPackageProvider.setList(JsArrays.toList(pluginPackagesDto.getPackagesArray()));
    updatesPackageProvider.refresh();
  }

  @Override
  public void refresh() {
    if (tabPanel.getSelectedTab() == 0) getUiHandlers().getInstalledPlugins();
    if (tabPanel.getSelectedTab() == 1) getUiHandlers().getUpdatablePlugins();
    if (tabPanel.getSelectedTab() == 2) getUiHandlers().getAvailablePlugins();
  }
}
