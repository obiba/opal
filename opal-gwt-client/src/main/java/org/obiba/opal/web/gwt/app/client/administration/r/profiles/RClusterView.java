/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.r.profiles;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.support.FilterHelper;
import org.obiba.opal.web.gwt.app.client.support.ValueRenderingHelper;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.*;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.EntryDto;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;
import org.obiba.opal.web.model.client.opal.r.RServerClusterDto;
import org.obiba.opal.web.model.client.opal.r.RServerDto;

import java.util.ArrayList;
import java.util.List;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

/**
 *
 */
public class RClusterView extends ViewWithUiHandlers<RClusterUiHandlers>
    implements RClusterPresenter.Display {

  interface Binder extends UiBinder<Widget, RClusterView> {
  }

  private static final Translations translations = GWT.create(Translations.class);

  public static final String START_ACTION = "Start";

  public static final String STOP_ACTION = "Stop";

  public static final String LOG_ACTION = "Log";

  @UiField
  Button startStopButton;

  @UiField
  Button rTestButton;

  @UiField
  OpalSimplePager serversPager;

  @UiField
  TextBoxClearable serversFilter;

  @UiField
  Table<RServerDto> serversTable;

  @UiField
  OpalSimplePager packagesPager;

  @UiField
  TextBoxClearable packagesFilter;

  @UiField
  Table<RPackageDto> packagesTable;

  @UiField
  Button manageRServers;

  private List<RServerDto> originalServers;

  private List<RPackageDto> originalPackages;

  private ActionsColumn<RServerDto> serversActionsColumn;

  private ActionsColumn<RPackageDto> packagesActionsColumn;

  private final ListDataProvider<RServerDto> serversDataProvider = new ListDataProvider<RServerDto>();

  private final ListDataProvider<RPackageDto> packagesDataProvider = new ListDataProvider<RPackageDto>();

  private Status status;

  //
  // Constructors
  //

  @Inject
  public RClusterView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
    initServersTable();
    initPackagesTable();
    manageRServers.setHref("#" + Places.ADMINISTRATION + "/" + Places.APPS);
  }

  @UiHandler("startStopButton")
  public void onStartStop(ClickEvent event) {
    if (Status.Startable.equals(status)) {
      getUiHandlers().onStart();
    } else {
      getUiHandlers().onStop();
    }
  }

  @UiHandler("rTestButton")
  public void onTest(ClickEvent event) {
    getUiHandlers().onTest();
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

  @UiHandler("serversFilter")
  public void onServersFilterUpdate(KeyUpEvent event) {
    renderServerList(filterServers(serversFilter.getText()));
  }

  @UiHandler("refreshServers")
  public void onRefreshServers(ClickEvent event) {
    serversPager.setPagerVisible(false);
    serversTable.showLoadingIndicator(serversDataProvider);
    getUiHandlers().onRefreshCluster();
  }

  @UiHandler("packagesFilter")
  public void onPackagesFilterUpdate(KeyUpEvent event) {
    renderPackageList(filterPackages(packagesFilter.getText()));
  }

  @UiHandler("refreshPackages")
  public void onRefreshPackages(ClickEvent event) {
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
  public HasAuthorization getTestAuthorizer() {
    return new WidgetAuthorizer(rTestButton);
  }

  @Override
  public void renderCluster(RServerClusterDto cluster) {
    if (cluster == null)
      renderServers(new ArrayList<RServerDto>());
    else
      renderServers(JsArrays.toList(cluster.getServersArray()));
  }

  private void renderServers(List<RServerDto> servers) {
    this.originalServers = servers == null ? new ArrayList<RServerDto>() : servers;
    boolean running = false;
    for (RServerDto server : servers) {
      if (server.getRunning()) running = true;
    }
    setServiceStatus(running ? Status.Stoppable : Status.Startable);
    renderServerList(servers);
  }

  private void renderServerList(List<RServerDto> servers) {
    serversTable.hideLoadingIndicator();
    serversDataProvider.setList(servers);
    serversPager.firstPage();
    serversDataProvider.refresh();
    serversPager.setPagerVisible(serversDataProvider.getList().size() > serversPager.getPageSize());
  }

  private List<RServerDto> filterServers(String text) {
    List<RServerDto> servers = Lists.newArrayList();
    if (originalServers == null) return servers;
    List<String> tokens = FilterHelper.tokenize(text);
    for (RServerDto server : originalServers) {
      String indexText = Joiner.on(" ").join(server.getName(), (server.hasApp() ? server.getApp().getServer() : ""));
      if (FilterHelper.matches(indexText, tokens)) servers.add(server);
    }
    return servers;
  }

  @Override
  public void renderPackages(List<RPackageDto> packages) {
    this.originalPackages = packages == null ? new ArrayList<RPackageDto>() : packages;
    packagesFilter.setText("");
    renderPackageList(this.originalPackages);
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
      String indexText = Joiner.on(" ").join(pkg.getName(), getEntryDtoValue(pkg, "title"), pkg.getRserver());
      if (FilterHelper.matches(indexText, tokens)) packages.add(pkg);
    }
    return packages;
  }

  private void initServersTable() {
    serversTable.addColumn(new TextColumn<RServerDto>() {
      @Override
      public String getValue(RServerDto rServerDto) {
        return rServerDto.getName();
      }
    }, translations.nameLabel());

    serversTable.addColumn(new TextColumn<RServerDto>() {
      @Override
      public String getValue(RServerDto rServerDto) {
        return rServerDto.getVersion();
      }
    }, translations.rVersionLabel());

    serversTable.addColumn(new TextColumn<RServerDto>() {
      @Override
      public String getValue(RServerDto rServerDto) {
        return rServerDto.getSessionCount() + "";
      }
    }, translations.rSessionsLabel());

    serversTable.addColumn(new TextColumn<RServerDto>() {
      @Override
      public String getValue(RServerDto rServerDto) {
        if (rServerDto.getCores() <= 0)
          return "?";
        return rServerDto.getCores() + " cores, " + ValueRenderingHelper.getSizeInBytes(rServerDto.getFreeMemory() * 1000) + " free memory";
      }
    }, translations.systemLabel());

    serversTable.addColumn(new URLColumn<RServerDto>() {

      @Override
      protected String getURL(RServerDto object) {
        return object.hasApp() ? object.getApp().getServer() : "";
      }
    }, translations.urlLabel());

    serversTable.addColumn(new ServerStatusColumn(), translations.statusLabel());

    serversTable.addColumn(serversActionsColumn = new ActionsColumn<RServerDto>(new ActionsProvider<RServerDto>() {

      @Override
      public String[] allActions() {
        return new String[]{START_ACTION, STOP_ACTION, LOG_ACTION};
      }

      @Override
      public String[] getActions(RServerDto value) {
        return value.getRunning() ? new String[]{STOP_ACTION, LOG_ACTION} : new String[]{START_ACTION, LOG_ACTION};
      }
    }), translations.actionsLabel());

    serversActionsColumn.setActionHandler(new ActionHandler<RServerDto>() {
      @Override
      public void doAction(RServerDto object, String actionName) {
        if (START_ACTION.equals(actionName)) {
          getUiHandlers().onStart(object.getName());
        } else if (STOP_ACTION.equals(actionName)) {
          getUiHandlers().onStop(object.getName());
        } else if (LOG_ACTION.equals(actionName)) {
          getUiHandlers().onDownloadRserveLog(object.getName());
        }
      }
    });

    serversTable.setEmptyTableWidget(new Label(translations.noItems()));
    serversPager.setDisplay(serversTable);
    serversDataProvider.addDataDisplay(serversTable);
    renderServers(new ArrayList<RServerDto>());
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

    packagesTable.addColumn(new TextColumn<RPackageDto>() {
      @Override
      public String getValue(RPackageDto object) {
        return object.getRserver();
      }
    }, translations.rServerLabel());

    packagesTable.addColumn(packagesActionsColumn = new ActionsColumn<RPackageDto>(new ActionsProvider<RPackageDto>() {

      @Override
      public String[] allActions() {
        return new String[]{REMOVE_ACTION};
      }

      @Override
      public String[] getActions(RPackageDto value) {
        return new String[]{REMOVE_ACTION};
      }
    }), translations.actionsLabel());

    packagesActionsColumn.setActionHandler(new ActionHandler<RPackageDto>() {
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

    for (int i = 0; i < entries.length(); i++) {
      if (entries.get(i).getKey().equalsIgnoreCase(key)) {
        return entries.get(i).getValue();
      }
    }
    return "";
  }

  private static class ServerStatusColumn extends Column<RServerDto, String> {

    private ServerStatusColumn() {
      super(new StatusImageCell());
    }

    @Override
    public String getValue(RServerDto dto) {
      String status = dto.getRunning() ?
          translations.statusMap().get("RUNNING") + "::" + StatusImageCell.BULLET_GREEN
          : translations.statusMap().get("STOPPED") + "::" + StatusImageCell.BULLET_RED;
      return status;
    }
  }

}
