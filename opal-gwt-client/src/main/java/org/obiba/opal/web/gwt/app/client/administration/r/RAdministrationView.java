/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.r;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CellTable;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.FilterHelper;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.EntryDto;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;

import java.util.ArrayList;
import java.util.List;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

/**
 *
 */
public class RAdministrationView extends ViewWithUiHandlers<RAdministrationUiHandlers>
    implements RAdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, RAdministrationView> {
  }

  private final Translations translations;

  @UiField
  Button startStopButton;

  @UiField
  Button rTestButton;

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

  @UiField
  OpalSimplePager packagesPager;
  
  @UiField
  TextBoxClearable filter;

  @UiField
  Table<RPackageDto> packagesTable;

  private List<RPackageDto> originalPackages;

  private ActionsColumn<RPackageDto> actionsColumn;

  private final ListDataProvider<RPackageDto> packagesDataProvider = new ListDataProvider<RPackageDto>();

  private Status status;

  //
  // Constructors
  //

  @Inject
  public RAdministrationView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initPackagesTable();
  }

  @UiHandler("startStopButton")
  public void onStartStop(ClickEvent event) {
    if (Status.Startable.equals(status)) {
      getUiHandlers().start();
    } else {
      getUiHandlers().stop();
    }
  }

  @UiHandler("rTestButton")
  public void onTest(ClickEvent event) {
    getUiHandlers().test();
  }

  @Override
  public void setServiceStatus(Status status) {
    this.status = status;
    switch (status) {
      case Startable:
        startStopButton.setText(translations.startLabel());
        startStopButton.setEnabled(true);
        rTestButton.setEnabled(true);
        break;
      case Stoppable:
        startStopButton.setText(translations.stopLabel());
        startStopButton.setEnabled(true);
        rTestButton.setEnabled(true);
        break;
      case Pending:
        startStopButton.setEnabled(false);
        rTestButton.setEnabled(false);
        break;
    }
  }

  @UiHandler("filter")
  public void onFilterUpdate(KeyUpEvent event) {
    renderPackageList(filterPackages(filter.getText()));
  }

  @UiHandler("refresh")
  public void onRefresh(ClickEvent event) {
    packagesPager.setPagerVisible(false);
    packagesTable.showLoadingIndicator(packagesDataProvider);
    getUiHandlers().onRefreshPackages();
  }

  @UiHandler("installPackage")
  public void onInstallPackage(ClickEvent event) {
    getUiHandlers().onInstallPackage();
  }

  @UiHandler("updateAllPackages")
  public void onUpdateAllPackages(ClickEvent event) {
    getUiHandlers().onUpdatePackages();
  }

  @UiHandler("downloadLogs")
  public void onDownloadRserveLog(ClickEvent event) {
    getUiHandlers().onDownloadRserveLog();
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if (slot == Slots.RSessions) {
      rSessions.clear();
      rSessions.add(content);
    }
    if (slot == Slots.RWorkspaces) {
      rWorkspaces.clear();
      rWorkspaces.add(content);
    }
    if (slot == Slots.Permissions) {
      permissions.clear();
      permissions.add(content);
    }
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new WidgetAuthorizer(permissionsPanel);
  }

  @Override
  public HasAuthorization getTestAuthorizer() {
    return new WidgetAuthorizer(rTestButton);
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @Override
  public void renderPackages(List<RPackageDto> packages) {
    this.originalPackages = packages;
    filter.setText("");
    renderPackageList(packages);
  }

  private void renderPackageList(List<RPackageDto> packages) {
    packagesTable.hideLoadingIndicator();
    packagesDataProvider.setList(packages);
    packagesPager.firstPage();
    packagesDataProvider.refresh();
    packagesPager.setPagerVisible(packagesDataProvider.getList().size() > packagesPager.getPageSize());
  }

  private List<RPackageDto> filterPackages(String text) {
    List<RPackageDto> packages = Lists.newArrayList();
    if (originalPackages == null) return packages;
    List<String> tokens = FilterHelper.tokenize(text);
    for (RPackageDto pkg : originalPackages) {
      String indexText = Joiner.on(" ").join(pkg.getName(), getEntryDtoValue(pkg, "title"));
      if (FilterHelper.matches(indexText, tokens)) packages.add(pkg);
    }
    return packages;
  }


  private void initPackagesTable() {
    packagesTable.addColumn(new TextColumn<RPackageDto>() {

      @Override
      public String getValue(RPackageDto object) {
        return object.getName();
      }

    }, translations.nameLabel());

    packagesTable.addColumn(new TextColumn<RPackageDto>() {
      @Override
      public String getValue(RPackageDto object) {
        return getEntryDtoValue(object, "title");
      }
    }, translations.titleLabel());

    packagesTable.addColumn(new TextColumn<RPackageDto>() {
      @Override
      public String getValue(RPackageDto object) {
        return getEntryDtoValue(object, "libpath");
      }
    }, "LibPath");

    packagesTable.addColumn(new TextColumn<RPackageDto>() {
      @Override
      public String getValue(RPackageDto object) {
        return getEntryDtoValue(object, "version");
      }
    }, translations.versionLabel());
    packagesTable.addColumn(actionsColumn = new ActionsColumn<RPackageDto>(new ActionsProvider<RPackageDto>() {

      @Override
      public String[] allActions() {
        return new String[]{REMOVE_ACTION};
      }

      @Override
      public String[] getActions(RPackageDto value) {
        return new String[]{REMOVE_ACTION};
      }
    }), translations.actionsLabel());

    actionsColumn.setActionHandler(new ActionHandler<RPackageDto>() {
      @Override
      public void doAction(RPackageDto object, String actionName) {
        getUiHandlers().onRemovePackage(object);
      }
    });

    packagesTable.setEmptyTableWidget(new Label(translations.noItems()));
    packagesPager.setDisplay(packagesTable);
    packagesDataProvider.addDataDisplay(packagesTable);
    renderPackages(new ArrayList<RPackageDto>());
  }

  private String getEntryDtoValue(RPackageDto object, String key) {
    JsArray<EntryDto> entries = JsArrays.toSafeArray(object.getDescriptionArray());

    for(int i = 0; i < entries.length(); i++) {
      if(entries.get(i).getKey().equalsIgnoreCase(key)) {
        return entries.get(i).getValue();
      }
    }
    return "";
  }

}
