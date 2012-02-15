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

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class DestinationSelectionStepPresenter extends WidgetPresenter<DestinationSelectionStepPresenter.Display> {

  public interface Display extends WidgetDisplay, WizardStepDisplay {

    void setDatasources(JsArray<DatasourceDto> datasources);

    String getSelectedDatasource();

    boolean hasTable();

    String getSelectedTable();

    void hideTables();

    void showTables();

  }

  private ImportFormat importFormat;

  JsArray<DatasourceDto> datasources;

  @Inject
  public DestinationSelectionStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  public void setImportFormat(ImportFormat importFormat) {
    this.importFormat = importFormat;
    hideShowTables();
    refreshDatasources();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
  }

  @Override
  protected void onUnbind() {
  }

  private void refreshDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> resource) {
        datasources = JsArrays.toSafeArray(resource);

        for(int i = 0; i < datasources.length(); i++) {
          DatasourceDto d = datasources.get(i);
          d.setTableArray(JsArrays.toSafeArray(d.getTableArray()));
          d.setViewArray(JsArrays.toSafeArray(d.getViewArray()));
        }

        getDisplay().setDatasources(datasources);
        // updateSelectableDatasources();
      }
    }).send();
  }

  private void updateSelectableDatasources() {
    JsArray<DatasourceDto> selectableDatasources;
    if(ImportFormat.CSV.equals(importFormat)) {
      selectableDatasources = removeDatasourcesWithoutTables(datasources);

      // OPAL-902
      selectableDatasources = removeDatasourcesWithOnlyViews(selectableDatasources);
    } else {
      selectableDatasources = datasources;
    }

    getDisplay().setDatasources(selectableDatasources);
  }

  private JsArray<DatasourceDto> removeDatasourcesWithoutTables(JsArray<DatasourceDto> datasources) {
    @SuppressWarnings("unchecked")
    JsArray<DatasourceDto> result = (JsArray<DatasourceDto>) JsArray.createArray();
    for(int i = 0; i < datasources.length(); i++) {
      DatasourceDto d = datasources.get(i);

      if(d.getTableArray().length() > 0) {
        result.push(d);
      }
    }
    return result;
  }

  private JsArray<DatasourceDto> removeDatasourcesWithOnlyViews(JsArray<DatasourceDto> datasources) {
    @SuppressWarnings("unchecked")
    JsArray<DatasourceDto> result = (JsArray<DatasourceDto>) JsArray.createArray();
    for(int i = 0; i < datasources.length(); i++) {
      DatasourceDto d = datasources.get(i);

      if(d.getTableArray().length() > d.getViewArray().length()) {
        result.push(d);
      }
    }
    return result;
  }

  private void hideShowTables() {
    if(ImportFormat.XML.equals(importFormat)) {
      getDisplay().hideTables();
    } else {
      getDisplay().showTables();
    }
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  public void refreshDisplay() {
    refreshDatasources();
  }

  @Override
  public void revealDisplay() {
  }

  public void updateImportData(ImportData importData) {
    importData.setDestinationDatasourceName(getDisplay().getSelectedDatasource());
    if(getDisplay().hasTable()) {
      importData.setDestinationTableName(getDisplay().getSelectedTable());
    } else
      importData.setDestinationTableName(null);
  }
}
