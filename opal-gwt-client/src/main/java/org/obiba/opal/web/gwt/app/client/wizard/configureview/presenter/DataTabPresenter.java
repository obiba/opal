/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewUpdateEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class DataTabPresenter extends WidgetPresenter<DataTabPresenter.Display> {

  public interface Display extends WidgetDisplay {
    HandlerRegistration addSaveChangesClickHandler(ClickHandler clickHandler);

    void saveChangesEnabled(boolean enabled);

    void setTableSelector(TableListPresenter.Display tableSelector);

    void clear();
  }

  private ViewDto viewDto;

  @Inject
  private TableListPresenter tableListPresenter;

  @Inject
  public DataTabPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  protected void onBind() {
    getDisplay().saveChangesEnabled(true);

    tableListPresenter.bind();
    getDisplay().setTableSelector(tableListPresenter.getDisplay());

    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    tableListPresenter.unbind();
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  public void refreshDisplay() {
    tableListPresenter.getTables().clear();
    tableListPresenter.getDisplay().clear();
    setSelectedTables();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().addSaveChangesClickHandler(new SaveChangesClickHandler()));
  }

  class SaveChangesClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      viewDto.clearFromArray();
      viewDto.setFromArray(getSelectedTables());
      eventBus.fireEvent(new ViewUpdateEvent(viewDto));
    }
  }

  private JsArrayString getSelectedTables() {
    JsArrayString tables = JavaScriptObject.createArray().cast();
    for(TableDto tableDto : tableListPresenter.getTables()) {
      tables.push(tableDto.getDatasourceName() + "." + tableDto.getName());
    }
    return tables;
  }

  private void setSelectedTables() {
    for(int i = 0; i < viewDto.getFromArray().length(); i++) {
      String[] parts = viewDto.getFromArray().get(i).split("\\.");
      ResourceRequestBuilderFactory.<TableDto> newBuilder().forResource("/datasource/" + parts[0] + "/table/" + parts[1]).get().withCallback(new ResourceCallback<TableDto>() {
        @Override
        public void onResource(Response response, TableDto resource) {
          List<TableDto> tableDtos = new ArrayList<TableDto>(1);
          tableDtos.add(resource);
          addTables(tableDtos);
        }
      }).send();
    }
  }

  private void addTables(List<TableDto> tableDtos) {
    for(TableDto dto : tableDtos) {
      tableListPresenter.getDisplay().addTable(dto);
      tableListPresenter.getTables().add(dto);
    }
  }

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;
  }
}