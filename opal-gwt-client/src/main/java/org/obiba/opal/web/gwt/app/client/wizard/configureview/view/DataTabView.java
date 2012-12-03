/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.DataTabPresenter;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import com.watopi.chosen.client.gwt.ChosenListBox;

public class DataTabView extends Composite implements DataTabPresenter.Display {

  @UiTemplate("DataTabView.ui.xml")
  interface myUiBinder extends UiBinder<Widget, DataTabView> {
  }

  private static myUiBinder uiBinder = GWT.create(myUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Button saveChangesButton;

  @UiField(provided = true)
  ChosenListBox tableChosen;

  private Map<String, TableDto> tableDtoMap = new HashMap<String, TableDto>();

  public DataTabView() {
    tableChosen = new ChosenListBox(true);
    initWidget(uiBinder.createAndBindUi(this));
    tableChosen.setPlaceholderText(translations.selectSomeTables());
    tableChosen.setSearchContains(true);
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }

  @Override
  public HandlerRegistration addSaveChangesClickHandler(ClickHandler clickHandler) {
    return saveChangesButton.addClickHandler(clickHandler);
  }

  @Override
  public void saveChangesEnabled(boolean enabled) {
    saveChangesButton.setEnabled(enabled);
  }

  @Override
  public void clear() {
    tableChosen.clear();
  }

  @Override
  public void addTableSelections(JsArray<TableDto> tables) {
    tableChosen.clear();
    tableDtoMap.clear();
    HashMap<String, List<TableDto>> datasourceMap = new LinkedHashMap<String, List<TableDto>>();
    for(TableDto table : JsArrays.toIterable(tables)) {
      if(datasourceMap.containsKey(table.getDatasourceName()) == false) {
        datasourceMap.put(table.getDatasourceName(), new ArrayList<TableDto>());
      }
      datasourceMap.get(table.getDatasourceName()).add(table);
    }

    for(String ds : datasourceMap.keySet()) {
      tableChosen.addGroup(ds);
      for(TableDto table : datasourceMap.get(ds)) {
        String fullName = table.getDatasourceName() + "." + table.getName();
        tableChosen.addItemToGroup(fullName, fullName);
        tableDtoMap.put(fullName, table);
      }
      tableChosen.update();
    }
  }

  @Override
  public void selectTables(JsArrayString tableFullNames) {
    for(String tableFullName : JsArrays.toIterable(tableFullNames)) {
      for(int i = 0; i < tableChosen.getItemCount(); i++) {
        if(tableChosen.getItemText(i).equals(tableFullName)) {
          tableChosen.setItemSelected(i, true);
          break;
        }
      }
    }
    tableChosen.update();
  }

  @Override
  public List<TableDto> getSelectedTables() {
    List<TableDto> tables = new ArrayList<TableDto>();
    for(int i = 0; i < tableChosen.getItemCount(); i++) {
      if(tableChosen.isItemSelected(i)) {
        tables.add(tableDtoMap.get(tableChosen.getValue(i)));
      }
    }
    return tables;
  }

  @Override
  public void setTableListListener(final DataTabPresenter.TableListListener listener) {
    tableChosen.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
      @Override
      public void onChange(ChosenChangeEvent chosenChangeEvent) {
        listener.onTableListUpdated();
      }
    });
  }

}
