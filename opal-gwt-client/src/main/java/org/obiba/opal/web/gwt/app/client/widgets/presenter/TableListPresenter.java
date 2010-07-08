/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.widgets.event.TableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.TableSelectionRequiredEvent;
import org.obiba.opal.web.model.client.TableDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.inject.Inject;

/**
 *
 */
public class TableListPresenter extends WidgetPresenter<TableListPresenter.Display> {

  //
  // Instance Variables
  //

  private List<TableDto> tables = new ArrayList<TableDto>();

  //
  // Constructors
  //

  @Inject
  public TableListPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Methods
  //

  public List<TableDto> getTables() {
    return tables;
  }

  private void addEventHandlers() {
    super.registerHandler(eventBus.addHandler(TableSelectionEvent.getType(), new TableSelectionEvent.Handler() {

      @Override
      public void onTableSelection(TableSelectionEvent event) {
        if(TableListPresenter.this.equals(event.getCallSource())) {
          for(TableDto selectedTable : event.getSelectedTables()) {
            boolean found = false;
            for(TableDto table : getTables()) {
              if(table.getName().equals(selectedTable.getName()) && table.getDatasourceName().equals(selectedTable.getDatasourceName())) {
                found = true;
                break;
              }
            }
            if(!found) {
              getTables().add(selectedTable);
              getDisplay().addTable(selectedTable);
            }
          }
        }
      }
    }));

    super.registerHandler(getDisplay().getRemoveWidget().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        List<Integer> selectedIndices = getDisplay().getSelectedIndices();
        int i = selectedIndices.size() - 1;
        while(i >= 0) {
          getTables().remove(selectedIndices.get(i).intValue());
          getDisplay().removeTable(selectedIndices.get(i));
          i--;
        }
        getDisplay().unselectAll();
      }
    }));

    super.registerHandler(getDisplay().getAddWidget().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new TableSelectionRequiredEvent(TableListPresenter.this));
      }
    }));

  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    HasClickHandlers getAddWidget();

    HasClickHandlers getRemoveWidget();

    void setListWidth(String width);

    void setListVisibleItemCount(int count);

    void removeTable(int index);

    void addTable(TableDto table);

    List<Integer> getSelectedIndices();

    public void unselectAll();

  }
}
