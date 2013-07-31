/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.unit.syncidentifiers.view;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.WizardModalBox;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.unit.syncidentifiers.presenter.IdentifiersSyncPresenter;
import org.obiba.opal.web.gwt.app.client.ui.ModalViewImpl;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.WizardStep;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableIdentifiersSync;

import com.github.gwtbootstrap.client.ui.Modal;
import com.google.common.collect.ImmutableList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;

public class IdentifiersSyncView extends ModalViewImpl implements IdentifiersSyncPresenter.Display {

  interface ViewUiBinder extends UiBinder<Widget, IdentifiersSyncView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  WizardModalBox dialog;

  @UiField
  WizardStep datasourceStep;

  @UiField
  ListBox datasourceBox;

  @UiField
  WizardStep tableStep;

  @UiField(provided = true)
  Table<TableIdentifiersSync> tableList;

  private final List<TableIdentifiersSync> tableSyncs = new ArrayList<TableIdentifiersSync>();

  private ListDataProvider<TableIdentifiersSync> tableSyncsProvider;

  private WizardStepChain stepChain;

  private StepInHandler tablesHandler;

  @Inject
  public IdentifiersSyncView(EventBus eventBus) {
    super(eventBus);
    initTableList();
    widget = uiBinder.createAndBindUi(this);
    initWizardDialog();
  }

  private void initTableList() {
    TableIdentifiersSyncTable table = new TableIdentifiersSyncTable() {

      @Override
      protected List<TableIdentifiersSync> getTableSyncs() {
        return tableSyncs;
      }
    };
    table.setPageSize(100);
    tableSyncsProvider = new ListDataProvider<TableIdentifiersSync>(tableSyncs);
    tableSyncsProvider.addDataDisplay(table);
    table.setEmptyTableWidget(table.getLoadingIndicator());

    SelectionModel<TableIdentifiersSync> selectionModel = new MultiSelectionModel<TableIdentifiersSync>(
        new ProvidesKey<TableIdentifiersSync>() {

          @Override
          public Object getKey(TableIdentifiersSync item) {
            return item.getTable();
          }
        });
    table.setSelectionModel(selectionModel);

    tableList = table;
  }

  private void initWizardDialog() {
    stepChain = WizardStepChain.Builder.create(dialog)//

        .append(datasourceStep)//
        .title(translations.identifiersSyncDatasourceStep())//

        .append(tableStep)//
        .title(translations.identifiersSyncTableStep())//
        .onStepIn(new StepInHandler() {

          @Override
          public void onStepIn() {
            dialog.setFinishEnabled(false);
            tablesHandler.onStepIn();
          }
        })//

        .onPrevious().onNext().build();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected Modal asModal() {
    return dialog;
  }

  @Override
  public void show() {
    stepChain.reset();
    super.show();
  }

  @Override
  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return dialog.addCancelClickHandler(handler);
  }

  @Override
  public HandlerRegistration addCloseClickHandler(ClickHandler handler) {
    return dialog.addCloseClickHandler(handler);
  }

  @Override
  public HandlerRegistration addFinishClickHandler(ClickHandler handler) {
    return dialog.addFinishClickHandler(handler);
  }

  @Nullable
  @Override
  public String getSelectedDatasource() {
    int index = datasourceBox.getSelectedIndex();
    return index == -1 ? null : datasourceBox.getValue(index);
  }

  @Override
  public void setDatasources(JsArray<DatasourceDto> datasources) {
    datasourceBox.clear();

    for(int i = 0; i < datasources.length(); i++) {
      String name = datasources.get(i).getName();
      datasourceBox.addItem(name);
    }
    if(datasourceBox.getSelectedIndex() == -1) datasourceBox.setSelectedIndex(0);
  }

  @Override
  public void setTableIdentifiersSync(JsArray<TableIdentifiersSync> tableIdentifiersSyncs) {
    tableSyncs.clear();
    tableSyncs.addAll(JsArrays.toList(tableIdentifiersSyncs));
    tableSyncsProvider.setList(tableSyncs);
    tableSyncsProvider.refresh();

    boolean selectable = false;
    for(TableIdentifiersSync ts : tableSyncs) {
      if(ts.getCount() > 0) {
        selectable = true;
        break;
      }
    }
    dialog.setFinishEnabled(selectable);
  }

  @Override
  public void setTableIdentifiersSyncRequestHandler(StepInHandler handler) {
    tablesHandler = handler;
  }

  @Override
  public List<String> getSelectedTables() {
    ImmutableList.Builder<String> builder = ImmutableList.<String>builder();
    for(TableIdentifiersSync ts : tableSyncs) {
      if(tableList.getSelectionModel().isSelected(ts)) {
        builder.add(ts.getTable());
      }
    }
    return builder.build();
  }

  @Override
  public void setProgress(boolean progress) {
    dialog.setProgress(progress);
    dialog.setFinishEnabled(!progress);
    dialog.setCancelEnabled(!progress);
    dialog.setPreviousEnabled(!progress);
  }
}
