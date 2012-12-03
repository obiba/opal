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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.ui.HasCollection;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.MatchingTableEntitiesValidator;
import org.obiba.opal.web.gwt.app.client.validator.MinimumSizeCollectionValidator;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavePendingEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSaveRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class DataTabPresenter extends WidgetPresenter<DataTabPresenter.Display> {

  public interface Display extends WidgetDisplay {
    HandlerRegistration addSaveChangesClickHandler(ClickHandler clickHandler);

    void saveChangesEnabled(boolean enabled);

    void clear();

    void addTableSelections(JsArray<TableDto> tables);

    void selectTables(JsArrayString tableFullNames);

    List<TableDto> getSelectedTables();

    void setTableListListener(TableListListener listener);

  }

  public interface TableListListener {

    void onTableListUpdated();

  }

  private ViewDto viewDto;

  private Request refreshRequest;

  @Inject
  public DataTabPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

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
    getDisplay().saveChangesEnabled(false);
    getDisplay().clear();
    refreshTables();
  }

  private void refreshTables() {
    if(refreshRequest == null || refreshRequest.isPending() == false) {
      refreshRequest = ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource("/datasources/tables")
          .get().withCallback(new ResourceCallback<JsArray<TableDto>>() {
            @Override
            public void onResource(Response response, JsArray<TableDto> resource) {
              JsArray<TableDto> tables = JsArrays.toSafeArray((JsArray<TableDto>) resource);
              TableDto viewTableDto = null;
              // search for the table corresponding to the view
              if(viewDto != null) {
                for(TableDto table : JsArrays.toIterable(tables)) {
                  if(viewDto.getDatasourceName().equals(table.getDatasourceName()) && viewDto.getName()
                      .equals(table.getName())) {
                    viewTableDto = table;
                    break;
                  }
                }
              }

              getDisplay().addTableSelections(filterTables(tables, viewTableDto));
              if(viewDto != null) {
                getDisplay().selectTables(viewDto.getFromArray());
              }
              refreshRequest = null;
            }

            /**
             * Remove from selection the view itself and the tables of different entity types.
             * @param tables
             * @param viewTableDto
             * @return
             */
            private JsArray<TableDto> filterTables(JsArray<TableDto> tables, TableDto viewTableDto) {
              JsArray<TableDto> filteredTables = tables;
              if(viewTableDto != null) {
                filteredTables = JsArrays.create();
                for(TableDto table : JsArrays.toIterable(tables)) {
                  if(table.equals(viewTableDto) == false && table.getEntityType()
                      .equals(viewTableDto.getEntityType())) {
                    filteredTables.push(table);
                  }
                }
              }
              return filteredTables;
            }

          }).send();
    }
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  private void addEventHandlers() {
    super.registerHandler(
        eventBus.addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredEventHandler()));
    super.registerHandler(getDisplay().addSaveChangesClickHandler(new SaveChangesClickHandler()));
    super.registerHandler(eventBus.addHandler(ViewSavedEvent.getType(), new ViewSavedHandler()));
    getDisplay().setTableListListener(new FormChangedHandler());
  }

  class ViewConfigurationRequiredEventHandler implements ViewConfigurationRequiredEvent.Handler {

    @Override
    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      DataTabPresenter.this.setViewDto(event.getView());
    }
  }

  class SaveChangesClickHandler implements ClickHandler {

    private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

    public SaveChangesClickHandler() {
      super();
      HasCollection<TableDto> tablesField = new HasCollection<TableDto>() {
        public Collection<TableDto> getCollection() {
          return getDisplay().getSelectedTables();
        }
      };
      validators.add(new MinimumSizeCollectionValidator<TableDto>(tablesField, 1, "TableSelectionRequired"));
      validators.add(new MatchingTableEntitiesValidator(tablesField));
    }

    public void onClick(ClickEvent event) {
      String errorMessageKey = validate();
      if(errorMessageKey != null) {
        eventBus.fireEvent(NotificationEvent.newBuilder().error(errorMessageKey).build());
        return;
      }
      viewDto.clearFromArray();
      viewDto.setFromArray(getSelectedTables());
      eventBus.fireEvent(new ViewSaveRequiredEvent(viewDto));
    }

    String validate() {
      for(FieldValidator validator : validators) {
        String errorMessageKey = validator.validate();
        if(errorMessageKey != null) {
          return errorMessageKey;
        }
      }
      return null;
    }
  }

  class FormChangedHandler implements TableListListener {

    @Override
    public void onTableListUpdated() {
      List<TableDto> tables = getDisplay().getSelectedTables();

      boolean changed = false;
      if(tables.size() != viewDto.getFromArray().length()) {
        changed = true;
      } else {
        List<String> fromTables = JsArrays.toList(viewDto.getFromArray());
        for(TableDto table : tables) {
          if(fromTables.contains(table.getDatasourceName() + "." + table.getName()) == false) {
            changed = true;
            break;
          }
        }
      }
      getDisplay().saveChangesEnabled(changed);
      eventBus.fireEvent(new ViewSavePendingEvent(changed));
    }
  }

  class ViewSavedHandler implements ViewSavedEvent.Handler {

    @Override
    public void onViewSaved(ViewSavedEvent event) {
      getDisplay().saveChangesEnabled(false);
    }

  }

  private JsArrayString getSelectedTables() {
    JsArrayString tables = JavaScriptObject.createArray().cast();
    for(TableDto tableDto : getDisplay().getSelectedTables()) {
      tables.push(tableDto.getDatasourceName() + "." + tableDto.getName());
    }
    return tables;
  }

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;
    viewDto.setFromArray(JsArrays.toSafeArray(viewDto.getFromArray()));
  }
}