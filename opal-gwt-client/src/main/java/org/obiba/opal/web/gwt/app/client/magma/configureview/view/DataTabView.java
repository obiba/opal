/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.configureview.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.DataTabPresenter;
import org.obiba.opal.web.gwt.app.client.ui.TableChooser;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.ViewImpl;
import com.watopi.chosen.client.event.ChosenChangeEvent;

public class DataTabView extends ViewImpl implements DataTabPresenter.Display {

  interface Binder extends UiBinder<Widget, DataTabView> {}

  @UiField
  Button saveChangesButton;

  @UiField(provided = true)
  TableChooser tableChooser;

  @Inject
  public DataTabView(Binder uiBinder) {
    tableChooser = new TableChooser(true);
    initWidget(uiBinder.createAndBindUi(this));
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
    tableChooser.clear();
  }

  @Override
  public void addTableSelections(JsArray<TableDto> tables) {
    tableChooser.addTableSelections(tables);
  }

  @Override
  public void selectTables(JsArrayString tableFullNames) {
    tableChooser.selectTables(tableFullNames);
  }

  @Override
  public List<TableDto> getSelectedTables() {
    return tableChooser.getSelectedTables();
  }

  @Override
  public void setTableListListener(final DataTabPresenter.TableListListener listener) {
    tableChooser.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
      @Override
      public void onChange(ChosenChangeEvent chosenChangeEvent) {
        listener.onTableListUpdated();
      }
    });
  }

}
