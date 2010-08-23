/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.dashboard.presenter.DashboardPresenter;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.CsvDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.CsvDatasourceTableBundleDto;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class DestinationSelectionStepPresenter extends WidgetPresenter<DestinationSelectionStepPresenter.Display> {

  public interface Display extends WidgetDisplay {

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    void setDatasources(JsArray<DatasourceDto> datasources);

    String getSelectedDatasource();

    String getSelectedTable();

    void hideTables();

    void showTables();

  }

  @Inject
  private ImportData importData;

  @Inject
  private DashboardPresenter dashboardPresenter;

  @Inject
  private IdentityArchiveStepPresenter identityArchiveStepPresenter;

  @Inject
  public DestinationSelectionStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
    hideShowTables();

    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> datasources) {
        getDisplay().setDatasources(datasources);
      }
    }).send();
  }

  private void hideShowTables() {
    if(importData.getImportFormat().equals(ImportFormat.XML)) {
      getDisplay().hideTables();
    } else {
      getDisplay().showTables();
    }
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addNextClickHandler(new NextClickHandler()));
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  class NextClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      importData.setDestinationDatasourceName(getDisplay().getSelectedDatasource());
      importData.setDestinationTableName(getDisplay().getSelectedTable());

      if(importData.getImportFormat().equals(ImportFormat.CSV)) {
        createTransientCsvDatasource();
      }
      if(importData.getImportFormat().equals(ImportFormat.XML)) {
        eventBus.fireEvent(new WorkbenchChangeEvent(identityArchiveStepPresenter));
      }
    }

    private void createTransientCsvDatasource() {

      ResourceCallback<JavaScriptObject> callbackHandler = new ResourceCallback<JavaScriptObject>() {

        @Override
        public void onResource(Response response, JavaScriptObject resource) {
          if(response.getStatusCode() == 201) {
            DatasourceDto datasourceDto = (DatasourceDto) resource;
            importData.setTransientDatasourceName(datasourceDto.getName());
            eventBus.fireEvent(new WorkbenchChangeEvent(identityArchiveStepPresenter));
          } else {
            // TODO: Handle errors
            @SuppressWarnings("unused")
            ClientErrorDto clientErrorDto = (ClientErrorDto) resource;
          }
        }

      };

      DatasourceFactoryDto dto = createDatasourceFactoryDto();
      ResourceRequestBuilderFactory.newBuilder().forResource("/datasources").post().accept("application/json").withResourceBody(DatasourceFactoryDto.stringify(dto)) //
      .withCallback(callbackHandler).send();
    }

    private DatasourceFactoryDto createDatasourceFactoryDto() {

      CsvDatasourceTableBundleDto csvDatasourceTableBundleDto = CsvDatasourceTableBundleDto.create();
      csvDatasourceTableBundleDto.setName(importData.getDestinationTableName());

      @SuppressWarnings("unchecked")
      JsArray<CsvDatasourceTableBundleDto> tables = (JsArray<CsvDatasourceTableBundleDto>) JsArray.createArray();
      tables.push(csvDatasourceTableBundleDto);

      CsvDatasourceFactoryDto csvDatasourceFactoryDto = CsvDatasourceFactoryDto.create();
      csvDatasourceFactoryDto.setCharacterSet(importData.getCharacterSet());
      csvDatasourceFactoryDto.setFirstRow(importData.getRow());
      csvDatasourceFactoryDto.setQuote(importData.getQuote());
      csvDatasourceFactoryDto.setSeparator(importData.getField());
      csvDatasourceFactoryDto.setTablesArray(tables);

      DatasourceFactoryDto dto = DatasourceFactoryDto.create();
      dto.setExtension(CsvDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, csvDatasourceFactoryDto);

      return dto;
    }
  }

}
