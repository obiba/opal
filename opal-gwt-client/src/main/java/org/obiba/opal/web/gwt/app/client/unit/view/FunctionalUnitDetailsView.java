/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.unit.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitUiHandlers;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ConstantActionsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;
import org.obiba.opal.web.model.client.opal.KeyDto;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.github.gwtbootstrap.client.ui.Tab;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import static org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitDetailsPresenter.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitDetailsPresenter.DOWNLOAD_ACTION;

public class FunctionalUnitDetailsView extends ViewWithUiHandlers<FunctionalUnitUiHandlers>
    implements FunctionalUnitDetailsPresenter.Display {

  @UiTemplate("FunctionalUnitDetailsView.ui.xml")
  interface FunctionalUnitDetailsViewUiBinder extends UiBinder<Widget, FunctionalUnitDetailsView> {}

  private static final int MAX_PAGE_SIZE = 100;

  private static final FunctionalUnitDetailsViewUiBinder uiBinder = GWT.create(FunctionalUnitDetailsViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  Label noUnit;

  @UiField
  SimplePager pager;

  @UiField
  InlineLabel noKeyPairs;

  @UiField
  FlowPanel functionalUnitDetails;

  @UiField
  Label description;

  @UiField
  Label select;

  @UiField
  Label currentCountOfIdentifiers;

  @UiField
  DropdownButton unitDropdown;

  @UiField
  NavLink updateUnit;

  @UiField
  NavLink removeUnit;

  @UiField
  DropdownButton identifiersDropDown;

  @UiField
  NavLink exportIdentifiers;

  @UiField
  NavLink exportIdentifiersMapping;

  @UiField
  NavLink generateIdentifiers;

  @UiField
  NavLink importIdentifiersFromFile;

  @UiField
  Button addCryptographicKey;

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  Table<KeyDto> propertiesTable;

  @UiField
  Tab tabProperties;

  @UiField
  Tab tabKeystore;

  JsArrayDataProvider<KeyDto> dataProvider = new JsArrayDataProvider<KeyDto>();

  private ActionsColumn<KeyDto> actionsColumn;

  private FunctionalUnitDto functionalUnit;

  public FunctionalUnitDetailsView() {
    widget = uiBinder.createAndBindUi(this);
    addTableColumns();
    initializeTabs();
  }

  private void initializeTabs() {
    tabProperties.setHeading(translations.propertiesLabel());
    tabKeystore.setHeading(translations.keystoreLabel());
  }

  private void addTableColumns() {
    pager.setVisible(false);

    propertiesTable.addColumn(new TextColumn<KeyDto>() {
      @Override
      public String getValue(KeyDto keyPair) {
        return keyPair.getAlias();
      }
    }, translations.aliasLabel());

    propertiesTable.addColumn(new TextColumn<KeyDto>() {
      @Override
      public String getValue(KeyDto keyPair) {
        return translations.keyTypeMap().get(keyPair.getKeyType().getName());
      }
    }, translations.typeLabel());

    actionsColumn = new ActionsColumn<KeyDto>(new ConstantActionsProvider<KeyDto>(DOWNLOAD_ACTION, DELETE_ACTION));
    propertiesTable.addColumn(actionsColumn, translations.actionsLabel());
    dataProvider.addDataDisplay(propertiesTable);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setKeyPairs(JsArray<KeyDto> keyPairs) {
    renderKeyPairs(keyPairs);
  }

  private void renderKeyPairs(JsArray<KeyDto> kpList) {
    dataProvider.setArray(kpList);
    pager.firstPage();
    dataProvider.refresh();
    pager.setVisible(kpList.length() > MAX_PAGE_SIZE);
    noKeyPairs.setVisible(kpList.length() == 0);
  }

  @Override
  public void setFunctionalUnitDetails(FunctionalUnitDto functionalUnit) {
    setAvailable(functionalUnit != null);
    if(functionalUnit != null) {
      renderFunctionalUnitDetails(functionalUnit);
    }
  }

  @Override
  public String getCurrentCountOfIdentifiers() {
    return currentCountOfIdentifiers.getText();
  }

  @Override
  public void setCurrentCountOfIdentifiers(String count) {
    currentCountOfIdentifiers.setText(count);
  }

  private void renderFunctionalUnitDetails(FunctionalUnitDto functionalUnitDto) {
    functionalUnitDetails.setVisible(true);
    functionalUnit = functionalUnitDto;
    description.setText(functionalUnitDto.getDescription());
    select.setText(functionalUnitDto.getSelect());
  }

  @Override
  public HasActionHandler<KeyDto> getActionColumn() {
    return actionsColumn;
  }

  @Override
  public FunctionalUnitDto getFunctionalUnitDetails() {
    return functionalUnit;
  }

  @Override
  public void setAvailable(boolean available) {
    noUnit.setVisible(!available);
    propertiesTable.setVisible(available);
  }

  @Override
  public HasAuthorization getRemoveFunctionalUnitAuthorizer() {
    return new UIObjectAuthorizer(removeUnit);
  }

  @Override
  public HasAuthorization getDownloadIdentifiersAuthorizer() {
    return new UIObjectAuthorizer(exportIdentifiers);
  }

  @Override
  public HasAuthorization getExportIdentifiersAuthorizer() {
    return new UIObjectAuthorizer(exportIdentifiersMapping);
  }

  @Override
  public HasAuthorization getGenerateIdentifiersAuthorizer() {
    return new UIObjectAuthorizer(generateIdentifiers);
  }

  @Override
  public HasAuthorization getImportIdentifiersFromDataAuthorizer() {
    return new UIObjectAuthorizer(importIdentifiersFromFile);
  }

  @Override
  public HasAuthorization getAddKeyPairAuthorizer() {
    return new UIObjectAuthorizer(addCryptographicKey);
  }

  @Override
  public HasAuthorization getListKeyPairsAuthorizer() {
    return new WidgetAuthorizer(propertiesTable);
  }

  @Override
  public HasAuthorization getUpdateFunctionalUnitAuthorizer() {
    return new UIObjectAuthorizer(updateUnit);
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("updateUnit")
  void onUpdateUnit(ClickEvent event) {
    getUiHandlers().updateUnit();
  }

  @UiHandler("removeUnit")
  void onRemoveUnit(ClickEvent event) {
    getUiHandlers().removeUnit();
  }

  @UiHandler("exportIdentifiers")
  void onExportIdentifier(ClickEvent event) {
    getUiHandlers().exportIdentifiers();
  }

  @UiHandler("exportIdentifiersMapping")
  void onExportIdentifiersMapping(ClickEvent event) {
    getUiHandlers().exportIdentifiersMapping();
  }

  @UiHandler("generateIdentifiers")
  void onGenerateIdentifiers(ClickEvent event) {
    getUiHandlers().generateIdentifiers();
  }

  @UiHandler("importIdentifiersFromFile")
  void onImportIdentifiersFromFile(ClickEvent event) {
    getUiHandlers().importIdentifiersFromFile();
  }

  @UiHandler("addCryptographicKey")
  void onAddCryptographicKey(ClickEvent event) {
    getUiHandlers().addCryptographicKey();
  }

}
