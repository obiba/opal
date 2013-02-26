/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.datashield.view;

import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackageAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsPackageRColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ConstantActionsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.r.EntryDto;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class DataShieldPackageAdministrationView extends ViewImpl
    implements DataShieldPackageAdministrationPresenter.Display {

  @UiTemplate("DataShieldPackageAdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DataShieldPackageAdministrationView> {}

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Translations translations;

  private final Widget widget;

  @UiField
  Panel packagesPanel;

  @UiField
  Button addPackageButton;

  @UiField
  CellTable<RPackageDto> packagesTable;

  @UiField
  SimplePager packagesTablePager;

  private JsArrayDataProvider<RPackageDto> packagesDataProvider = new JsArrayDataProvider<RPackageDto>();

  private ActionsPackageRColumn<RPackageDto> actionsColumn;

  @Inject
  public DataShieldPackageAdministrationView(Translations translations) {
    this.translations = translations;
    widget = uiBinder.createAndBindUi(this);
    initPackagesTable();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public HandlerRegistration addPackageHandler(ClickHandler handler) {
    return addPackageButton.addClickHandler(handler);
  }

  @Override
  public void renderDataShieldPackagesRows(JsArray<RPackageDto> rows) {
    packagesDataProvider.setArray(rows);

    int size = packagesDataProvider.getList().size();
    packagesTablePager.firstPage();
    packagesTablePager.setVisible(size > 0);
    packagesTable.setVisible(true);
    packagesDataProvider.refresh();
  }

  @Override
  public HasActionHandler<RPackageDto> getDataShieldPackageActionsColumn() {
    return actionsColumn;
  }

  private void initPackagesTable() {

    addPackageTableColumns();

    //noinspection MagicNumber
    packagesTable.setPageSize(50);
    packagesTablePager.setDisplay(packagesTable);
    packagesDataProvider.addDataDisplay(packagesTable);
  }

  private void addPackageTableColumns() {
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
        return getEntryDtoValue(object, "version");
      }
    }, translations.versionLabel());

    actionsColumn = new ActionsPackageRColumn<RPackageDto>(
        new ConstantActionsProvider<RPackageDto>(ActionsPackageRColumn.VIEW_ACTION, ActionsPackageRColumn.REMOVE_ACTION,
            ActionsPackageRColumn.PUBLISH_ACTION));
    packagesTable.addColumn(actionsColumn, translations.actionsLabel());
    packagesTable.setEmptyTableWidget(new Label(translations.noDataAvailableLabel()));
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

  @Override
  public HasAuthorization getAddPackageAuthorizer() {
    return new WidgetAuthorizer(addPackageButton);
  }

  @Override
  public HasAuthorization getPackagesAuthorizer() {
    return new WidgetAuthorizer(packagesPanel);
  }

}
