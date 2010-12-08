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

import static org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitDetailsPresenter.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitDetailsPresenter.DOWNLOAD_ACTION;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitDetailsPresenter.ActionHandler;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitDetailsPresenter.HasActionHandler;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;
import org.obiba.opal.web.model.client.opal.KeyPairDto;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListView;
import com.google.gwt.view.client.ListView.Delegate;

public class FunctionalUnitDetailsView extends Composite implements FunctionalUnitDetailsPresenter.Display {

  @UiTemplate("FunctionalUnitDetailsView.ui.xml")
  interface FunctionalUnitDetailsViewUiBinder extends UiBinder<Widget, FunctionalUnitDetailsView> {
  }

  private static FunctionalUnitDetailsViewUiBinder uiBinder = GWT.create(FunctionalUnitDetailsViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  @UiField
  CellTable<KeyPairDto> keyPairsTable;

  @UiField
  InlineLabel noKeyPairs;

  @UiField
  FlowPanel functionalUnitDetails;

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

  private MenuItem keyPair;

  private MenuItem generateIdentifiers;

  private MenuItem importIdentifiers;

  private MenuItem update;

  SimplePager<KeyPairDto> pager;

  @SuppressWarnings("unused")
  private HasActionHandler actionsColumn;

  private FunctionalUnitDto functionalUnit;

  private Label functionalUnitName;

  public FunctionalUnitDetailsView() {
    initWidget(uiBinder.createAndBindUi(this));
    initKeystoreTable();
    initActionToolbar();
  }

  private void initActionToolbar() {
    toolbarPanel.add(functionalUnitName = new Label());
    functionalUnitName.addStyleName("title");
    toolbarPanel.add(toolbar = new MenuBar());
    toolbar.setAutoOpen(true);
    toolbar.addItem("", actionsMenu = new MenuBar(true)).addStyleName("tools");
    actionsMenu.addStyleName("tools");
    toolbar.addItem("", addMenu = new MenuBar(true)).addStyleName("add");
  }

  private void initKeystoreTable() {
    keyPairsTable.addColumn(new TextColumn<KeyPairDto>() {
      @Override
      public String getValue(KeyPairDto keyPair) {
        return keyPair.getAlias();
      }
    }, translations.aliasLabel());

    actionsColumn = new ActionsColumn();
    keyPairsTable.addColumn((ActionsColumn) actionsColumn, translations.actionsLabel());
    addTablePager();
  }

  private void addTablePager() {
    keyPairsTable.setPageSize(10);
    pager = new SimplePager<KeyPairDto>(keyPairsTable);
    DOM.removeElementAttribute(pager.getElement(), "style");
    DOM.setStyleAttribute(pager.getElement(), "cssFloat", "right");
    keyPairsTable.setPager(pager);
    ((VerticalPanel) keyPairsTable.getParent()).insert(pager, 0);
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void setKeyPairs(final JsArray<KeyPairDto> keyPairs) {
    renderKeyPairs(keyPairs);
  }

  private void renderKeyPairs(final JsArray<KeyPairDto> kpList) {
    keyPairsTable.setDelegate(new Delegate<KeyPairDto>() {

      @Override
      public void onRangeChanged(ListView<KeyPairDto> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, JsArrays.toList(kpList, start, length));
      }
    });

    pager.firstPage();
    int pageSize = keyPairsTable.getPageSize();
    keyPairsTable.setData(0, pageSize, JsArrays.toList(kpList, 0, pageSize));
    keyPairsTable.setDataSize(kpList.length(), true);
    keyPairsTable.redraw();

    keyPairsTable.setVisible(kpList.length() > 0);
    pager.setVisible(kpList.length() > 0);
    noKeyPairs.setVisible(kpList.length() == 0);
  }

  @Override
  public void setFunctionalUnitDetails(FunctionalUnitDto functionalUnit) {
    if(functionalUnit == null) {
      functionalUnitDetails.setVisible(false);
    } else {
      renderFunctionalUnitDetails(functionalUnit);
    }
  }

  @Override
  public void setCurrentCountOfIdentifiers(String count) {
    this.currentCountOfIdentifiers.setText(count);
  }

  private void renderFunctionalUnitDetails(FunctionalUnitDto functionalUnit) {
    functionalUnitDetails.setVisible(true);
    this.functionalUnit = functionalUnit;
    select.setText(functionalUnit.getSelect());
    functionalUnitName.setText(functionalUnit.getName());

  }

  // TODO Extract the following ActionsColumn and ActionsCell cells class. These should be part of some generic
  // component. JobListView should also be refactored because it includes similar classes.
  static class ActionsColumn extends Column<KeyPairDto, KeyPairDto> implements HasActionHandler {

    public ActionsColumn() {
      super(new ActionsCell());
    }

    public KeyPairDto getValue(KeyPairDto object) {
      return object;
    }

    public void setActionHandler(ActionHandler actionHandler) {
      ((ActionsCell) getCell()).setActionHandler(actionHandler);
    }
  }

  static class ActionsCell extends AbstractCell<KeyPairDto> {

    private CompositeCell<KeyPairDto> delegateCell;

    private FieldUpdater<KeyPairDto, String> hasCellFieldUpdater;

    private ActionHandler actionHandler;

    public ActionsCell() {
      hasCellFieldUpdater = new FieldUpdater<KeyPairDto, String>() {
        public void update(int rowIndex, KeyPairDto object, String value) {
          if(actionHandler != null) {
            actionHandler.doAction(object, value);
          }
        }
      };
    }

    @Override
    public Object onBrowserEvent(Element parent, KeyPairDto value, Object viewData, NativeEvent event, ValueUpdater<KeyPairDto> valueUpdater) {
      refreshActions(value);

      return delegateCell.onBrowserEvent(parent, value, viewData, event, valueUpdater);
    }

    @Override
    public void render(KeyPairDto value, Object viewData, StringBuilder sb) {
      refreshActions(value);

      delegateCell.render(value, viewData, sb);
    }

    public void setActionHandler(ActionHandler actionHandler) {
      this.actionHandler = actionHandler;
    }

    private void refreshActions(KeyPairDto value) {
      delegateCell = createCompositeCell(DOWNLOAD_ACTION, DELETE_ACTION);
    }

    private CompositeCell<KeyPairDto> createCompositeCell(String... actionNames) {
      List<HasCell<KeyPairDto, ?>> hasCells = new ArrayList<HasCell<KeyPairDto, ?>>();

      final Cell<String> cell = new ClickableTextCell() {

        @Override
        public void render(String value, Object viewData, StringBuilder sb) {
          super.render(translations.actionMap().get(value), viewData, sb);
        }

      };

      for(final String actionName : actionNames) {
        hasCells.add(new HasCell<KeyPairDto, String>() {

          @Override
          public Cell<String> getCell() {
            return cell;
          }

          @Override
          public FieldUpdater<KeyPairDto, String> getFieldUpdater() {
            return hasCellFieldUpdater;
          }

          @Override
          public String getValue(KeyPairDto object) {
            return actionName;
          }
        });
      }

      return new CompositeCell<KeyPairDto>(hasCells);
    }
  }

  @Override
  public HasActionHandler getActionColumn() {
    return actionsColumn;
  }

  @Override
  public FunctionalUnitDto getFunctionalUnitDetails() {
    return functionalUnit;
  }

  @Override
  public void setRemoveFunctionalUnitCommand(Command command) {
    if(remove == null) {
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
      actionsMenu.addSeparator(new MenuItemSeparator());
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
    if(keyPair == null) {
      addMenu.addItem(keyPair = new MenuItem(translations.addKeyPair(), command));
    } else {
      keyPair.setCommand(command);
    }
  }

  @Override
  public void setGenerateIdentifiersCommand(Command command) {
    if(generateIdentifiers == null) {
      addMenu.addSeparator(new MenuItemSeparator());
      addMenu.addItem(generateIdentifiers = new MenuItem(translations.generateUnitIdentifiers(), command));
    } else {
      generateIdentifiers.setCommand(command);
    }
  }

  @Override
  public void setImportIdentifiersCommand(Command command) {
    if(importIdentifiers == null) {
      addMenu.addItem(importIdentifiers = new MenuItem(translations.importUnitIdentifiers(), command));
    } else {
      importIdentifiers.setCommand(command);
    }
  }

}
