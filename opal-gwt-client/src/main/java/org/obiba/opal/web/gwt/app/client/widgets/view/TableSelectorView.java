/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableSelectorPresenter.TableSelectionType;
import org.obiba.opal.web.model.client.DatasourceDto;
import org.obiba.opal.web.model.client.TableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class TableSelectorView extends DialogBox implements TableSelectorPresenter.Display {
  //
  // Constants
  //

  private static final String DIALOG_HEIGHT = "30em";

  private static final String DIALOG_WIDTH = "30em";

  private static final int VISIBLE_COUNT = 15;

  //
  // Static Variables
  //

  private static TableSelectorViewUiBinder uiBinder = GWT.create(TableSelectorViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  ListBox datasourceList;

  @UiField
  ListBox tableList;

  @UiField
  Button selectButton;

  @UiField
  Button cancelButton;

  private TableSelectionType tableSelectionType = TableSelectionType.MULTIPLE;

  //
  // Constructors
  //

  public TableSelectorView() {
    setText("Table Selector");
    setHeight(DIALOG_HEIGHT);
    setWidth(DIALOG_WIDTH);

    DockLayoutPanel content = uiBinder.createAndBindUi(this);
    content.setHeight(DIALOG_HEIGHT);
    content.setWidth(DIALOG_WIDTH);
    add(content);

    datasourceList.setVisibleItemCount(1);
    tableList.setVisibleItemCount(VISIBLE_COUNT);

    selectButton.setText("Select");
    cancelButton.setText("Cancel");

    addCancelHandler();
  }

  //
  // FileSelectorPresenter.Display Methods
  //

  @Override
  public void showDialog() {
    center();
    show();
  }

  @Override
  public void hideDialog() {
    hide();
  }

  @Override
  public void setTableSelectionType(TableSelectionType mode) {
    this.tableSelectionType = mode;
    if(mode.equals(TableSelectionType.SINGLE)) {
      tableList.setVisibleItemCount(1);
    } else {
      tableList.setVisibleItemCount(VISIBLE_COUNT);
    }
  }

  @Override
  public void setDatasources(JsArray<DatasourceDto> datasources) {
    datasourceList.clear();
    for(int i = 0; i < datasources.length(); i++) {
      datasourceList.addItem(datasources.get(i).getName());
    }
  }

  @Override
  public void setTables(JsArray<TableDto> tables) {
    tableList.clear();
    for(int i = 0; i < tables.length(); i++) {
      tableList.addItem(tables.get(i).getName());
    }
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }

  public Widget asWidget() {
    return this;
  }

  //
  // Methods
  //

  private void addCancelHandler() {
    selectButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        hideDialog();
      }
    });

    cancelButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        hideDialog();
      }
    });
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("TableSelectorView.ui.xml")
  interface TableSelectorViewUiBinder extends UiBinder<DockLayoutPanel, TableSelectorView> {
  }

  @Override
  public HasChangeHandlers getDatasourceList() {
    return datasourceList;
  }

  @Override
  public int getSelectedDatasourceIndex() {
    return datasourceList.getSelectedIndex();
  }

  @Override
  public List<Integer> getSelectedTableIndices() {
    List<Integer> selections = new ArrayList<Integer>();

    for(int i = 0; i < tableList.getItemCount(); i++) {
      if(tableList.isItemSelected(i)) {
        selections.add(i);
      }
    }

    return selections;
  }

  @Override
  public HasClickHandlers getSelectButton() {
    return selectButton;
  }

}