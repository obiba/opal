/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield.packages;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Button;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.FilterHelper;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsPackageRColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ClickableColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ConstantActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.EntryDto;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class DataShieldPackagesView extends ViewWithUiHandlers<DataShieldPackagesUiHandlers>
    implements DataShieldPackagesPresenter.Display {

  private static final int PAGE_SIZE = 10;

  interface Binder extends UiBinder<Widget, DataShieldPackagesView> {
  }

  private final Translations translations;

  private PackageClickableColumn packageNameColumn;

  @UiField
  Panel packagesPanel;

  @UiField
  Button addPackageButton;

  @UiField
  Button deleteAllPackagesButton;

  @UiField
  TextBoxClearable packagesFilter;

  @UiField
  Table<RPackageDto> packagesTable;

  @UiField
  Alert inconsistencyNotice;

  private final ListDataProvider<RPackageDto> packagesDataProvider = new ListDataProvider<RPackageDto>();

  private ActionsPackageRColumn<RPackageDto> actionsColumn;

  private List<RPackageDto> originalPackages;

  @Inject
  public DataShieldPackagesView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initPackagesTable();
  }

  @UiHandler("refresh")
  public void onRefresh(ClickEvent event) {
    inconsistencyNotice.setVisible(false);
    packagesTable.showLoadingIndicator(packagesDataProvider);
    getUiHandlers().onRefresh();
  }

  @UiHandler("deleteAllPackagesButton")
  public void onDeleteAllPackages(ClickEvent event) {
    getUiHandlers().deleteAllPackages(packagesDataProvider.getList());
  }

  @UiHandler("packagesFilter")
  public void onPackagesFilterUpdate(KeyUpEvent event) {
    renderDataShieldPackagesList(filterPackages(packagesFilter.getText()));
  }

  @Override
  public HandlerRegistration addPackageHandler(ClickHandler handler) {
    return addPackageButton.addClickHandler(handler);
  }

  @Override
  public void renderDataShieldPackages(List<RPackageDto> packages) {
    this.originalPackages = packages == null ? new ArrayList<RPackageDto>() : packages;

    Set<String> rServerNames = Sets.newHashSet();
    Map<String, Integer> pkgCounts = Maps.newHashMap();
    for (RPackageDto pkg : packages) {
      rServerNames.add(pkg.getRserver());
      String key = pkg.getName() + ":" + getEntryDtoValue(pkg, "version");
      if (pkgCounts.containsKey(key))
        pkgCounts.put(key, pkgCounts.get(key) +1);
      else
        pkgCounts.put(key, 1);
    }
    GWT.log("R servers " + rServerNames.size());
    for (Map.Entry<String, Integer> pkgCount : pkgCounts.entrySet()) {
      if (pkgCount.getValue() != rServerNames.size())
        inconsistencyNotice.setVisible(true);
    }

    packagesFilter.setText("");
    renderDataShieldPackagesList(packages);
  }

  private void renderDataShieldPackagesList(List<RPackageDto> packages) {
    packagesTable.hideLoadingIndicator();
    packagesDataProvider.setList(packages);
    packagesTable.setVisible(true);
    packagesDataProvider.refresh();
    deleteAllPackagesButton.setEnabled(!packages.isEmpty());
  }

  @Override
  public HasActionHandler<RPackageDto> getDataShieldPackageActionsColumn() {
    return actionsColumn;
  }

  private void initPackagesTable() {
    addPackageTableColumns();
    packagesTable.setPageSize(PAGE_SIZE);
    packagesDataProvider.addDataDisplay(packagesTable);
    packagesTable.showLoadingIndicator(packagesDataProvider);
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

  private void addPackageTableColumns() {
    packagesTable.addColumn(packageNameColumn = new PackageClickableColumn("name") {
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
        return getEntryDtoValue(object, "version");
      }
    }, translations.versionLabel());

    packagesTable.addColumn(new TextColumn<RPackageDto>() {
      @Override
      public String getValue(RPackageDto object) {
        return object.getRserver();
      }
    }, translations.rServerLabel());

    actionsColumn = new ActionsPackageRColumn<RPackageDto>(
        new ConstantActionsProvider<RPackageDto>(ActionsPackageRColumn.REMOVE_ACTION,
            ActionsPackageRColumn.PUBLISH_ACTION, ActionsPackageRColumn.UNPUBLISH_ACTION));
    packagesTable.addColumn(actionsColumn, translations.actionsLabel());
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

  private abstract static class PackageClickableColumn extends ClickableColumn<RPackageDto> {

    private final String name;

    private PackageClickableColumn(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

  }

  @Override
  public HasAuthorization getAddPackageAuthorizer() {
    return new WidgetAuthorizer(addPackageButton, deleteAllPackagesButton);
  }

  @Override
  public HasAuthorization getPackagesAuthorizer() {
    return new WidgetAuthorizer(packagesPanel);
  }

  @Override
  public void setPackageNameFieldUpdater(FieldUpdater<RPackageDto, String> updater) {
    packageNameColumn.setFieldUpdater(updater);
  }

  @Override
  public void setAddPackageButtonEnabled(boolean b) {
    addPackageButton.setEnabled(b);
  }

}
