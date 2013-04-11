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
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ConstantActionsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.app.client.workbench.view.PropertiesTable;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.MenuItemAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;
import org.obiba.opal.web.model.client.opal.KeyDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

import static org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitDetailsPresenter.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitDetailsPresenter.DOWNLOAD_ACTION;

public class FunctionalUnitDetailsView extends ViewImpl implements FunctionalUnitDetailsPresenter.Display {

  @UiTemplate("FunctionalUnitDetailsView.ui.xml")
  interface FunctionalUnitDetailsViewUiBinder extends UiBinder<Widget, FunctionalUnitDetailsView> {}

  private static final FunctionalUnitDetailsViewUiBinder uiBinder = GWT.create(FunctionalUnitDetailsViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  Label noUnit;

  @UiField
  HorizontalTabLayout tabs;

  @UiField
  CellTable<KeyDto> keyPairsTable;

  @UiField
  SimplePager pager;

  @UiField
  InlineLabel noKeyPairs;

  @UiField
  FlowPanel functionalUnitDetails;

  @UiField
  PropertiesTable propertiesPanel;

  @UiField
  Label description;

  @UiField
  Label select;

  @UiField
  Label currentCountOfIdentifiers;

  @UiField
  FlowPanel toolbarPanel;

  private MenuBar toolbar;

  private MenuBar actionsMenu;

  private MenuBar addMenu;

  private MenuItem remove;

  private MenuItem downloadIds;

  private MenuItem exportIds;

  private MenuItem cryptoKey;

  private MenuItem generateIdentifiers;

  private MenuItem importIdentifiersFromData;

  private MenuItem update;

  JsArrayDataProvider<KeyDto> dataProvider = new JsArrayDataProvider<KeyDto>();

  private ActionsColumn<KeyDto> actionsColumn;

  private FunctionalUnitDto functionalUnit;

  private Label functionalUnitName;

  private MenuItem toolsItem;

  private MenuItem addItem;

  private MenuItemSeparator removeSeparator;

  private MenuItemSeparator keyPairSeparator;

  public FunctionalUnitDetailsView() {
    widget = uiBinder.createAndBindUi(this);
    initKeystoreTable();
    initActionToolbar();
  }

  private void initActionToolbar() {
    toolbarPanel.add(functionalUnitName = new Label());
    functionalUnitName.addStyleName("title");
    toolbarPanel.add(toolbar = new MenuBar());
    toolbar.setAutoOpen(true);
    toolsItem = toolbar.addItem("", actionsMenu = new MenuBar(true));
    toolsItem.addStyleName("tools");
    actionsMenu.addStyleName("tools");
    addItem = toolbar.addItem("", addMenu = new MenuBar(true));
    addItem.addStyleName("add");
  }

  private void initKeystoreTable() {
    keyPairsTable.addColumn(new TextColumn<KeyDto>() {
      @Override
      public String getValue(KeyDto keyPair) {
        return keyPair.getAlias();
      }
    }, translations.aliasLabel());

    keyPairsTable.addColumn(new TextColumn<KeyDto>() {
      @Override
      public String getValue(KeyDto keyPair) {
        return translations.keyTypeMap().get(keyPair.getKeyType().getName());
      }
    }, translations.typeLabel());

    actionsColumn = new ActionsColumn<KeyDto>(new ConstantActionsProvider<KeyDto>(DOWNLOAD_ACTION, DELETE_ACTION));
    keyPairsTable.addColumn(actionsColumn, translations.actionsLabel());
    addTablePager();
    dataProvider.addDataDisplay(keyPairsTable);
  }

  private void addTablePager() {
    keyPairsTable.setPageSize(10);
    pager.setDisplay(keyPairsTable);
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

    keyPairsTable.setVisible(kpList.length() > 0);
    pager.setVisible(kpList.length() > 0);
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
    functionalUnitName.setText(functionalUnitDto.getName());

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
  public void setRemoveFunctionalUnitCommand(Command command) {
    if(remove == null) {
      removeSeparator = actionsMenu.addSeparator(new MenuItemSeparator());
      actionsMenu.addItem(remove = new MenuItem(translations.removeLabel(), command));
    } else {
      remove.setCommand(command);
    }
  }

  @Override
  public void setDownloadIdentifiersCommand(Command command) {
    if(downloadIds == null) {
      actionsMenu.addItem(downloadIds = new MenuItem(translations.downloadUnitIdentifiers(), command));
    } else {
      downloadIds.setCommand(command);
    }
  }

  @Override
  public void setExportIdentifiersCommand(Command command) {
    if(exportIds == null) {
      actionsMenu.addItem(exportIds = new MenuItem(translations.exportUnitIdentifiersToExcel(), command));
    } else {
      exportIds.setCommand(command);
    }
  }

  @Override
  public void setUpdateFunctionalUnitCommand(Command command) {
    if(update == null) {
      toolbar.addItem(update = new MenuItem("", command)).addStyleName("edit");
    } else {
      update.setCommand(command);
    }
  }

  @Override
  public void setAddKeyPairCommand(Command command) {
    if(cryptoKey == null) {
      keyPairSeparator = addMenu.addSeparator(new MenuItemSeparator());
      addMenu.addItem(cryptoKey = new MenuItem(translations.addCryptoKey(), command));
    } else {
      cryptoKey.setCommand(command);
    }
  }

  @Override
  public void setGenerateIdentifiersCommand(Command command) {
    if(generateIdentifiers == null) {
      addMenu.addItem(generateIdentifiers = new MenuItem(translations.generateUnitIdentifiers(), command));
    } else {
      generateIdentifiers.setCommand(command);
    }
  }

  @Override
  public void setImportIdentifiersFromDataCommand(Command command) {
    if(importIdentifiersFromData == null) {
      addMenu.addItem(importIdentifiersFromData = new MenuItem(translations.importUnitIdentifiersFromData(), command));
    } else {
      importIdentifiersFromData.setCommand(command);
    }
  }

  @Override
  public void setAvailable(boolean available) {
    noUnit.setVisible(!available);
    tabs.setVisible(available);
    toolbarPanel.setVisible(available);
    propertiesPanel.setVisible(available);
  }

  @Override
  public HasAuthorization getRemoveFunctionalUnitAuthorizer() {
    return new CompositeAuthorizer(new MenuItemAuthorizer(toolsItem), new MenuItemAuthorizer(remove),
        new UIObjectAuthorizer(removeSeparator)) {
      @Override
      public void unauthorized() {
      }
    };
  }

  @Override
  public HasAuthorization getDownloadIdentifiersAuthorizer() {
    return new CompositeAuthorizer(new MenuItemAuthorizer(toolsItem), new MenuItemAuthorizer(downloadIds)) {
      @Override
      public void unauthorized() {
      }
    };
  }

  @Override
  public HasAuthorization getExportIdentifiersAuthorizer() {
    return new CompositeAuthorizer(new MenuItemAuthorizer(toolsItem), new MenuItemAuthorizer(exportIds)) {
      @Override
      public void unauthorized() {
      }
    };
  }

  @Override
  public HasAuthorization getGenerateIdentifiersAuthorizer() {
    return new CompositeAuthorizer(new MenuItemAuthorizer(addItem), new MenuItemAuthorizer(generateIdentifiers)) {
      @Override
      public void unauthorized() {
      }
    };
  }

  @Override
  public HasAuthorization getImportIdentifiersFromDataAuthorizer() {
    return new CompositeAuthorizer(new MenuItemAuthorizer(addItem), new MenuItemAuthorizer(importIdentifiersFromData)) {
      @Override
      public void unauthorized() {
      }
    };
  }

  @Override
  public HasAuthorization getAddKeyPairAuthorizer() {
    return new CompositeAuthorizer(new MenuItemAuthorizer(addItem), new MenuItemAuthorizer(cryptoKey),
        new UIObjectAuthorizer(keyPairSeparator)) {
      @Override
      public void unauthorized() {
      }
    };
  }

  @Override
  public HasAuthorization getListKeyPairsAuthorizer() {
    return new WidgetAuthorizer(tabs);
  }

  @Override
  public HasAuthorization getUpdateFunctionalUnitAuthorizer() {
    return new MenuItemAuthorizer(update);
  }

}
