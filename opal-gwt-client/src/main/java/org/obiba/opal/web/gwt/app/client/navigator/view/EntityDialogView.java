package org.obiba.opal.web.gwt.app.client.navigator.view;

import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.navigator.presenter.EntityDialogViewPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 *
 */
public class EntityDialogView extends PopupViewImpl implements EntityDialogViewPresenter.Display {

  @UiTemplate("EntityDialogView.ui.xml")
  interface EntityViewUiBinder extends UiBinder<DialogBox, EntityDialogView> {
  }

  private static final EntityViewUiBinder uiBinder = GWT.create(EntityViewUiBinder.class);

  private final Widget widget;

  @UiField
  DialogBox dialog;

  @UiField
  Label entityType;

  @UiField
  Label entityId;

  @UiField
  ListBox datasourceBox;

  @UiField
  ListBox tableBox;

  @Inject
  public EntityDialogView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    dialog.hide();
  }

  @Override
  public void showDialog() {
    dialog.center();
    dialog.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void setDatasources(List<String> datasources, @Nullable String selectedDatasource) {
    datasourceBox.clear();

    for(int i = 0; i < datasources.size(); i++) {
      String name = datasources.get(i);
      datasourceBox.addItem(name);
      if(selectedDatasource != null && selectedDatasource.equals(name)) {
        datasourceBox.setSelectedIndex(i);
      }
    }
    if(datasourceBox.getSelectedIndex() == -1) datasourceBox.setSelectedIndex(0);
  }

  @Override
  public String getSelectedDatasource() {
    int index = datasourceBox.getSelectedIndex();
    return index == -1 ? null : datasourceBox.getValue(index);
  }

  @Override
  public void setTables(List<String> tables, @Nullable String selectedTable) {
    tableBox.clear();
    for(int i = 0; i < tables.size(); i++) {
      String name = tables.get(i);
      tableBox.addItem(name);
      if(selectedTable != null && selectedTable.equals(name)) {
        tableBox.setSelectedIndex(i);
      }
    }
    if(tableBox.getSelectedIndex() == -1) tableBox.setSelectedIndex(0);
  }

  @Override
  public String getSelectedTable() {
    int index = tableBox.getSelectedIndex();
    return index == -1 ? null : tableBox.getValue(index);
  }

  @Override
  public HasChangeHandlers getTableList() {
    return tableBox;
  }

  @Override
  public HasChangeHandlers getDatasourceList() {
    return datasourceBox;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected PopupPanel asPopupPanel() {
    return dialog;
  }
}
