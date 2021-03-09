/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.apps;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.URLColumn;
import org.obiba.opal.web.model.client.opal.AppDto;
import org.obiba.opal.web.model.client.opal.AppsConfigDto;
import org.obiba.opal.web.model.client.opal.RockAppConfigDto;

public class AppsAdministrationView extends ViewWithUiHandlers<AppsAdministrationUiHandlers> implements AppsAdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, AppsAdministrationView> {
  }

  private static final String UNREGISTER_ACTION = "Unregister";

  private final Translations translations;

  @UiField
  Panel tokenPanel;

  @UiField
  HasText token;

  @UiField
  HasWidgets breadcrumbs;

  @UiField
  OpalSimplePager appsPager;

  @UiField
  Table<AppDto> appsTable;

  @UiField
  Table<RockAppConfigDto> rockConfigsTable;

  private JsArrayDataProvider<AppDto> appsProvider = new JsArrayDataProvider<AppDto>();

  private JsArrayDataProvider<RockAppConfigDto> rockConfigsProvider = new JsArrayDataProvider<RockAppConfigDto>();

  @Inject
  public AppsAdministrationView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initAppsTable();
    initRockConfigsTable();
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("refresh")
  public void onRefresh(ClickEvent event) {
    appsPager.setPagerVisible(false);
    appsTable.showLoadingIndicator(appsProvider);
    getUiHandlers().onRefresh();
  }

  @UiHandler("editToken")
  public void onEditToken(ClickEvent event) {
    getUiHandlers().onEditToken();
  }

  @UiHandler("addRockConfig")
  public void onRockConfigAdd(ClickEvent event) {
    getUiHandlers().onRockConfigAdd();
  }

  @Override
  public void renderApps(JsArray<AppDto> apps) {
    appsTable.hideLoadingIndicator();
    appsProvider.setArray(apps);
    appsPager.firstPage();
    appsProvider.refresh();
    appsPager.setPagerVisible(appsProvider.getList().size() > appsPager.getPageSize());
  }

  @Override
  public void renderAppsConfig(AppsConfigDto config) {
    token.setText(config.getToken());
    tokenPanel.setVisible(!Strings.isNullOrEmpty(config.getToken()));
    rockConfigsProvider.setArray(config.getRockConfigsArray());
  }

  private void initAppsTable() {
    appsTable.addColumn(new TextColumn<AppDto>() {
      @Override
      public String getValue(AppDto dto) {
        return dto.getName();
      }
    }, translations.nameLabel());

    appsTable.addColumn(new TextColumn<AppDto>() {
      @Override
      public String getValue(AppDto dto) {
        return dto.getType();
      }
    }, translations.typeLabel());

    appsTable.addColumn(new TextColumn<AppDto>() {
      @Override
      public String getValue(AppDto dto) {
        return dto.getCluster();
      }
    }, translations.clusterLabel());

    appsTable.addColumn(new TextColumn<AppDto>() {
      @Override
      public String getValue(AppDto dto) {
        return Joiner.on(", ").join(JsArrays.toList(dto.getTagsArray()));
      }
    }, translations.tagsLabel());

    appsTable.addColumn(new URLColumn<AppDto>() {

      @Override
      protected String getURL(AppDto object) {
        return object.getServer();
      }
    }, translations.hostLabel());

    ActionsColumn<AppDto> actionsColumn = new ActionsColumn<AppDto>(new ActionsProvider<AppDto>() {

      @Override
      public String[] allActions() {
        return new String[]{UNREGISTER_ACTION};
      }

      @Override
      public String[] getActions(AppDto value) {
        return new String[]{UNREGISTER_ACTION};
      }
    });

    appsTable.addColumn(actionsColumn, translations.actionsLabel());

    actionsColumn.setActionHandler(new ActionHandler<AppDto>() {
      @Override
      public void doAction(AppDto object, String actionName) {
        getUiHandlers().onUnregister(object);
      }
    });

    appsTable.setEmptyTableWidget(new Label(translations.noItems()));
    appsPager.setDisplay(appsTable);
    appsProvider.addDataDisplay(appsTable);
    appsProvider.setArray(null);
    appsProvider.refresh();
  }

  private void initRockConfigsTable() {
    rockConfigsTable.addColumn(new URLColumn<RockAppConfigDto>() {
      @Override
      protected String getURL(RockAppConfigDto object) {
        return object.getHost();
      }
    }, translations.hostLabel());

    ActionsColumn<RockAppConfigDto> actionsColumn = new ActionsColumn<RockAppConfigDto>(new ActionsProvider<RockAppConfigDto>() {

      @Override
      public String[] allActions() {
        return new String[]{ActionsColumn.EDIT_ACTION, ActionsColumn.REMOVE_ACTION};
      }

      @Override
      public String[] getActions(RockAppConfigDto value) {
        return new String[]{ActionsColumn.EDIT_ACTION, ActionsColumn.REMOVE_ACTION};
      }
    });

    rockConfigsTable.addColumn(actionsColumn, translations.actionsLabel());

    actionsColumn.setActionHandler(new ActionHandler<RockAppConfigDto>() {
      @Override
      public void doAction(RockAppConfigDto object, String actionName) {
        if (ActionsColumn.REMOVE_ACTION.equals(actionName))
          getUiHandlers().onRockConfigRemove(object);
        else if (ActionsColumn.EDIT_ACTION.equals(actionName))
          getUiHandlers().onRockConfigEdit(object);
      }
    });

    rockConfigsTable.setEmptyTableWidget(new Label(translations.noItems()));
    rockConfigsProvider.addDataDisplay(rockConfigsTable);
    rockConfigsProvider.setArray(null);
    rockConfigsProvider.refresh();
  }

}
